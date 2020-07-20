package com.yifan.lightning.deal.service.impl;

import com.yifan.lightning.deal.constant.DatabaseConst;
import com.yifan.lightning.deal.dao.ItemDOMapper;
import com.yifan.lightning.deal.dao.ItemStockDOMapper;
import com.yifan.lightning.deal.dao.StockLogDOMapper;
import com.yifan.lightning.deal.dataobject.ItemDO;
import com.yifan.lightning.deal.dataobject.ItemStockDO;
import com.yifan.lightning.deal.dataobject.StockLogDO;
import com.yifan.lightning.deal.error.BusinessException;
import com.yifan.lightning.deal.error.EnumBusinessError;
import com.yifan.lightning.deal.service.ItemService;
import com.yifan.lightning.deal.service.PromoService;
import com.yifan.lightning.deal.service.model.ItemModel;
import com.yifan.lightning.deal.service.model.PromoModel;
import com.yifan.lightning.deal.validator.ValidationResult;
import com.yifan.lightning.deal.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private ItemDOMapper itemDOMapper;

    @Autowired
    private ItemStockDOMapper itemStockDOMapper;

    @Autowired
    private StockLogDOMapper stockLogDOMapper;

    @Autowired
    private PromoService promoService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {
        // 校验入参
        ValidationResult result = validator.validate(itemModel);
        if (result.isHasErrors()) {
            throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, result.getErrMsg());
        }

        // 转化itemmodel -> dataobject，同时处理库存对象
        ItemDO itemDO = this.convertItemDOFromItemModel(itemModel);

        // 写入数据库
        itemDOMapper.insertSelective(itemDO);
        // 此时，MyBatis已经把自增得到的id回填到了itemDO的id上，将其存入itemModel以供stock使用
        itemModel.setId(itemDO.getId());
        ItemStockDO itemStockDO = this.convertItemStockDOFromItemModel(itemModel);
        itemStockDOMapper.insertSelective(itemStockDO);

        // 返回创建完成的对象
        return this.getItemById(itemModel.getId());
    }

    @Override
    public List<ItemModel> listItem() {
        List<ItemDO> itemDOList = itemDOMapper.listItem();
        // 把list中的每一个itemDO映射为一个itemModel
        // 这里使用了Java8的stream API
        List<ItemModel> itemModelList = itemDOList.stream().map(itemDO -> {
            ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel = this.convertModelFromDataObject(itemDO, itemStockDO);
            return itemModel;
        }).collect(Collectors.toList());
        return itemModelList;
    }

    @Override
    @Cacheable(key = "#id")
    public ItemModel getItemById(Integer id) {
        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);
        if (itemDO == null) {
            return null;
        }
        // 获得库存数量
        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());

        // 将DO转化为model
        ItemModel itemModel = convertModelFromDataObject(itemDO, itemStockDO);

        // 获取秒杀活动商品信息
        PromoModel promoModel = promoService.getPromoByItemId(itemModel.getId());
        if (promoModel != null && promoModel.getStatus().intValue() != 3) { // 商品存在正在进行或还未开始的秒杀活动
            itemModel.setPromoModel(promoModel);
        }

        return itemModel;
    }

    // 从redis缓存中获取商品及其秒杀活动信息
//    @Override
//    public ItemModel getItemByIdInCache(Integer id) {
//        ItemModel itemModel = (ItemModel) redisTemplate.opsForValue().get("item_validate_" + id);
//        if (itemModel == null) { // 若redis缓存中不存在该商品信息，查数据库
//            itemModel = this.getItemById(id);
//            redisTemplate.opsForValue().set("item_validate_" + id, itemModel);
//            // 设置有效时间为10分钟
//            redisTemplate.expire("item_validate_" + id, 10, TimeUnit.MINUTES);
//        }
//        return itemModel;
//    }

    // 减库存操作必须保证原子性，所以要加上@Transactional
    // 该操作只需要修改item_stock表而不影响item_info表，体现出了先前把stock单独建表的好处
    @Override
    @Transactional
    public boolean decreaseStock(Integer itemId, Integer amount) throws BusinessException {
        // 从redis缓存中减库存，redis的减法就是increment方法且第二个参数为负数，返回的result为剩下的数字
        long result = redisTemplate.opsForValue().increment("promo_item_stock_" + itemId, amount.intValue() * (-1));
        if (result > 0) { // 所剩的库存数仍然大于0，更新库存成功
            return true;
        } else if (result == 0) { // 更新成功，但是此时商品售罄
            // 打上售罄标识
            redisTemplate.opsForValue().set("promo_item_stock_invalid_" + itemId, "true");
            return true;
        } else { // 更新库存失败，把库存补回去
            increaseStock(itemId, amount);
            return false;
        }
    }

    // 库存回滚，把redis中的库存加回去，固定返回true
    @Override
    public boolean increaseStock(Integer itemId, Integer amount) {
        redisTemplate.opsForValue().increment("promo_item_stock_" + itemId, amount.intValue());
        return true;
    }

    // 商品销量增加，需要保证原子性，加@Transactional
    @Override
    @Transactional
    public void increaseSales(Integer itemId, Integer amount) throws BusinessException {
        itemDOMapper.increaseSales(itemId, amount);
    }

    // 库存流水初始化状态，在下单之前调用
    @Override
    @Transactional
    public String initStockLog(Integer itemId, Integer amount) {
        StockLogDO stockLogDO = new StockLogDO();
        stockLogDO.setItemId(itemId);
        stockLogDO.setAmount(amount);
        // 生成主键id
        stockLogDO.setStockLogId(UUID.randomUUID().toString().replace("-", ""));
        stockLogDO.setStatus(DatabaseConst.STOCK_LOG_STATUS_INIT);
        // 存入数据库
        stockLogDOMapper.insertSelective(stockLogDO);

        return stockLogDO.getStockLogId();
    }

    private ItemDO convertItemDOFromItemModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemDO itemDO = new ItemDO();
        BeanUtils.copyProperties(itemModel, itemDO);
        // itemModel中的price是BigDecimal，而itemDO中的price是Double，因此不会被BeanUtils所copy
        itemDO.setPrice(itemModel.getPrice().doubleValue());
        return itemDO;
    }

    private ItemStockDO convertItemStockDOFromItemModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setItemId(itemModel.getId());
        itemStockDO.setStock(itemModel.getStock());
        return itemStockDO;
    }

    private ItemModel convertModelFromDataObject(ItemDO itemDO, ItemStockDO itemStockDO) {
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDO, itemModel);
        // 同样的，把DO中的Double的price转化为model中的BigDecimal的price
        itemModel.setPrice(new BigDecimal(itemDO.getPrice()));
        itemModel.setStock(itemStockDO.getStock());
        return itemModel;
    }

}
