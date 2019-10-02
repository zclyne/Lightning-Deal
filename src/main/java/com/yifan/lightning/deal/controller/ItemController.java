package com.yifan.lightning.deal.controller;

import com.yifan.lightning.deal.controller.viewobject.ItemVO;
import com.yifan.lightning.deal.error.BusinessException;
import com.yifan.lightning.deal.response.CommonReturnType;
import com.yifan.lightning.deal.service.CacheService;
import com.yifan.lightning.deal.service.ItemService;
import com.yifan.lightning.deal.service.model.ItemModel;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController("item")
@RequestMapping("/item")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
public class ItemController extends BaseController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CacheService cacheService;

    // 创建商品接口
    @RequestMapping(value = "/create", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType createItem(@RequestParam(name = "title") String title,
                                       @RequestParam(name = "description") String description,
                                       @RequestParam(name = "price") BigDecimal price,
                                       @RequestParam(name = "stock") Integer stock,
                                       @RequestParam(name = "imgUrl") String imgUrl) throws BusinessException {
        // 封装service请求，用来创建商品
        ItemModel itemModel = new ItemModel();
        itemModel.setTitle(title);
        itemModel.setDescription(description);
        itemModel.setStock(stock);
        itemModel.setPrice(price);
        itemModel.setImgUrl(imgUrl);

        ItemModel itemModelForReturn = itemService.createItem(itemModel);
        // 把创建的商品信息返回给前端
        ItemVO itemVO = this.convertVOFromModel(itemModelForReturn);

        return CommonReturnType.create(itemVO);
    }

    // 商品详情页浏览
    @RequestMapping(value = "/get", method = {RequestMethod.GET})
    public CommonReturnType getItem(@RequestParam(name = "id") Integer id) {
        // 此处使用多级缓存，顺序为本地热点缓存 -> redis缓存 -> 数据库
        ItemModel itemModel = null;

        // 取本地缓存
        itemModel = (ItemModel) cacheService.getFromCommonCache("item_" + id);
        if (itemModel == null) { // 商品不存在于本地缓存中，查redis缓存
            // 根据商品id到redis中获取
            itemModel = (ItemModel) redisTemplate.opsForValue().get("item_" + id);
            if (itemModel == null) { // 商品不存在于redis中，访问数据库
                itemModel = itemService.getItemById(id);
                // 把itemModel存入redis中
                redisTemplate.opsForValue().set("item_" + id, itemModel);
                // 设置失效时间为10分钟
                redisTemplate.expire("item_" + id, 10, TimeUnit.MINUTES);
            }
            // 将商品设置到本地缓存中
            cacheService.setCommonCache("item_" + id, itemModel);
        }

        ItemVO itemVO = convertVOFromModel(itemModel);
        return CommonReturnType.create(itemVO);
    }

    // 商品列表页面浏览
    @RequestMapping(value = "/list", method = {RequestMethod.GET})
    public CommonReturnType listItem() {
        List<ItemModel> itemModelList = itemService.listItem();

        // 使用stream API将list内的itemModel转化为itemVO
        List<ItemVO> itemVOList = itemModelList.stream().map(itemModel -> {
            ItemVO itemVO = this.convertVOFromModel(itemModel);
            return itemVO;
        }).collect(Collectors.toList());
        return CommonReturnType.create(itemVOList);
    }

    private ItemVO convertVOFromModel(ItemModel itemModel) {
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel, itemVO);
        if (itemModel.getPromoModel() != null) { // 商品有正在进行或即将开始的秒杀活动
            itemVO.setPromoStatus(itemModel.getPromoModel().getStatus());
            itemVO.setPromoId(itemModel.getPromoModel().getId());
            itemVO.setPromoPrice(itemModel.getPromoModel().getPromoItemPrice());
            itemVO.setStartDate(itemModel.getPromoModel().getStartDate().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
            itemVO.setEndDate(itemModel.getPromoModel().getEndDate().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
        } else { // 没有秒杀活动
            itemVO.setPromoStatus(3);
        }
        return itemVO;
    }

}
