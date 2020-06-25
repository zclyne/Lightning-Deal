package com.yifan.lightning.deal.mq;

import com.rabbitmq.client.Channel;
import com.yifan.lightning.deal.config.RabbitMQConfig;
import com.yifan.lightning.deal.dao.ItemStockDOMapper;
import com.yifan.lightning.deal.dao.StockLogDOMapper;
import com.yifan.lightning.deal.dataobject.StockLogDO;
import com.yifan.lightning.deal.error.BusinessException;
import com.yifan.lightning.deal.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class Receiver {
    @Autowired
    ItemStockDOMapper itemStockDOMapper;

    @Autowired
    OrderService orderService;

    @Autowired
    StockLogDOMapper stockLogDOMapper;

    @RabbitListener(queues = RabbitMQConfig.queueName)
    public void receiveMessage(MqMessage message) {
        System.out.println("Received <" + message + ">");
        Map<String, Object> map = message.getMap();
        Integer itemId = (Integer) map.get("itemId");
        Integer amount = (Integer) map.get("amount");
        String stockLogId = (String) map.get("stockLogId");
        Integer userId = (Integer) map.get("userId");
        Integer promoId = (Integer) map.get("promoId");
        // 创建订单
        try {
            orderService.createOrder(userId, itemId, promoId, amount, stockLogId);
        } catch (BusinessException e) {
            e.printStackTrace();
            // 如果有异常，则事务需要回滚，返回ROLLBACK
            // 设置对应stockLog为回滚状态
            StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
            stockLogDO.setStatus(3); // 3表示需要回滚
            stockLogDOMapper.updateByPrimaryKeySelective(stockLogDO);
        }
        // 数据库减库存
        int afffectedRowNumber = itemStockDOMapper.decreaseStock(itemId, amount);
        if (afffectedRowNumber != 1) { // 减库存结果不正确
            // 设置对应stockLog为回滚状态
            StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
            stockLogDO.setStatus(3); // 3表示需要回滚
            stockLogDOMapper.updateByPrimaryKeySelective(stockLogDO);
        }
    }

}
