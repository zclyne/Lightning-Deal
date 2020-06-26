package com.yifan.lightning.deal.service.impl;

import com.yifan.lightning.deal.dao.OrderDOMapper;
import com.yifan.lightning.deal.dao.SequenceDOMapper;
import com.yifan.lightning.deal.dao.StockLogDOMapper;
import com.yifan.lightning.deal.dataobject.OrderDO;
import com.yifan.lightning.deal.dataobject.SequenceDO;
import com.yifan.lightning.deal.dataobject.StockLogDO;
import com.yifan.lightning.deal.error.BusinessException;
import com.yifan.lightning.deal.error.EnumBusinessError;
import com.yifan.lightning.deal.service.ItemService;
import com.yifan.lightning.deal.service.OrderService;
import com.yifan.lightning.deal.service.UserService;
import com.yifan.lightning.deal.service.model.ItemModel;
import com.yifan.lightning.deal.service.model.OrderModel;
import com.yifan.lightning.deal.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDOMapper orderDOMapper;

    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Autowired
    private StockLogDOMapper stockLogDOMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount, String stockLogId) throws BusinessException {
        // 校验下单状态：购买数量是否正确、秒杀活动信息是否正确
//        // UserModel userModel = userService.getUserById(userId);
//        // 从redis中获取用户信息
//        UserModel userModel = userService.getUserByIdInCache(userId);
//        if (userModel == null) {
//            throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, "用户不存在");
//        }

        // 从redis中获取商品信息
        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        if (itemModel == null) {
            throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, "商品不存在");
        }

        // 检查redis中是否包含商品的库存信息，若不包含，则把库存量存入redis中
        if (!redisTemplate.hasKey("promo_item_stock" + itemModel.getId())) {
            redisTemplate.opsForValue().set("promo_item_stock_" + itemModel.getId(), itemModel.getStock());
        }

        // 校验购买数量
        if (amount <= 0 || amount > 99) {
            throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, "购买数量不合法");
        }

//        if (promoId != null) { // 存在秒杀活动
//            // 校验秒杀活动和商品是否对应
//            if (promoId.intValue() != itemModel.getPromoModel().getId()) {
//                throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, "秒杀活动信息不正确");
//            } else if (itemModel.getPromoModel().getStatus() != 2) { // 校验秒杀活动是否正在进行中
//                throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, "秒杀活动还未开始");
//            }
//        }

        // 落单减库存。另一种方法是支付减库存，但有可能出现超卖现象，所以此处使用落单减库存
        boolean result = itemService.decreaseStock(itemId, amount);
        if (!result) { // 减库存失败，抛出库存不足的异常
            throw new BusinessException(EnumBusinessError.STOCK_NOT_ENOUGH);
        }
        // 生成对应orderModel
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);
        // 根据是否存在秒杀活动id来判断设置商品价格为秒杀活动价格还是普通价格
        if (promoId != null) {
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        } else {
            orderModel.setItemPrice(itemModel.getPrice());
        }
        // 设置秒杀活动id
        orderModel.setPromoId(promoId);
        // 用单价*数量的方式计算订单总价
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));
        // 生成订单号
        orderModel.setId(generateOrderNo());
        // 订单入库
        OrderDO orderDO = convertFromOrderModel(orderModel);
        orderDOMapper.insert(orderDO);

        // 商品销量增加
        itemService.increaseSales(itemId, amount);

        // 设置库存流水状态为成功
        StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
        if (stockLogDO == null) { // 这种情况理论上不存在
            throw new BusinessException(EnumBusinessError.UNKNOWN_ERROR);
        }
        stockLogDO.setStatus(2); // 状态2表示下单扣减库存成功
        stockLogDOMapper.updateByPrimaryKeySelective(stockLogDO);

        // 因为已经使用了RocketMQ的事务型消息，所以这里不再需要处理关于数据库库存的任何内容
//        // spring boot的transaction事务在最后一起commit，而commit过程仍有可能因为网络不通畅的原因失败
//        // 所以要把数据库的库存操作放在commit之后
//        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
//            @Override
//            public void afterCommit() {
//                // 在返回前端之前，异步发送减库存消息到数据库
//                boolean mqResult = itemService.asyncDecreaseStock(itemId, amount);
//                // 因为是在commit之后发送的异步消息，所以不能失败
////                if (!mqResult) { // 异步发送消息失败，回滚库存
////                    itemService.increaseStock(itemId, amount);
////                    throw new BusinessException(EnumBusinessError.MQ_SEND_FAIL);
////                }
//            }
//        });

        // 返回前端
        return orderModel;
    }

    private OrderDO convertFromOrderModel(OrderModel orderModel) {
        if (orderModel == null) {
            return null;
        }
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel, orderDO);
        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        return orderDO;
    }

    // 该方法的调用原本就在一个事务中，这里必须使用REQUIRES_NEW来开启一个新的事务
    // 这是因为即使外层创建订单的事务失败了，这里的sequence也不应该被回滚，而是应该被存到数据库中，以保证全局一致性
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected String generateOrderNo() {
        // 订单号有16位
        StringBuilder stringBuilder = new StringBuilder();
        // 前8位为时间信息，包含年月日
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        stringBuilder.append(nowDate);
        // 中间6位为自增序列
        // 获取的当前sequence
        int sequence = 0;
        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
        sequence = sequenceDO.getCurrentValue();
        // 把sequence的值+对应步长，然后存回数据库
        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue() + sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);
        // 把int类型的sequence转为String，并填充到6位，不足的位数填充0
        String sequenceStr = String.valueOf(sequence);
        for (int i = 0; i < 6 - sequenceStr.length(); i++) {
            stringBuilder.append("0");
        }
        stringBuilder.append(sequenceStr);

        // 最后2位为分库分表位，比如可以用userId % 100，方便以后对数据库做水平拆分
        // 这里暂时使用00
        stringBuilder.append("00");

        // 返回结果
        return stringBuilder.toString();
    }

}
