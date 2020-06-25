package com.yifan.lightning.deal.mq;

import com.yifan.lightning.deal.config.RabbitMQConfig;
import com.yifan.lightning.deal.dao.StockLogDOMapper;
import com.yifan.lightning.deal.dataobject.StockLogDO;
import com.yifan.lightning.deal.error.BusinessException;
import com.yifan.lightning.deal.service.OrderService;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class Sender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);
    }

    public void createOrderAndDecreseStock(Integer userId, Integer itemId, Integer promoId, Integer amount, String stockLogId) {

        Map<String, Object> map = new HashMap<>();
        map.put("itemId", itemId);
        map.put("amount", amount);
        map.put("stockLogId", stockLogId);
        map.put("userId", userId);
        map.put("promoId", promoId);
        MqMessage message = new MqMessage(UUID.randomUUID().toString(), map);
        send(message);
    }

    public void send(MqMessage message) {
        CorrelationData correlationId = new CorrelationData((String) message.getMap().get("stackLogId"));
        rabbitTemplate.convertAndSend(RabbitMQConfig.topicExchangeName, "lightning.deal", message, correlationId);
    }

}
