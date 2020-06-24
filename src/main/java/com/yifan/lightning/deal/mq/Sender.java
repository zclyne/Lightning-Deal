package com.yifan.lightning.deal.mq;

import com.yifan.lightning.deal.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class Sender implements RabbitTemplate.ConfirmCallback {

    private RabbitTemplate rabbitTemplate;

    @Autowired
    public Sender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        // 设置MySender的实例为callback
        rabbitTemplate.setConfirmCallback(this);
    }

    public void decreaseStock(Integer userId, Integer itemId, Integer promoId, Integer amount, String stockLogId) {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("itemId", itemId);
        bodyMap.put("amount", amount);
        // 把库存流水id放在bodyMap中传入checkLocalTransaction方法，用于在该方法中检查状态
        bodyMap.put("stockLogId", stockLogId);

        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("itemId", itemId);
        argsMap.put("amount", amount);
        argsMap.put("userId", userId);
        argsMap.put("promoId", promoId);
    }

    public void send(MqMessage message) {
        CorrelationData correlationId = new CorrelationData(UUID.randomUUID().toString());
        // 构造要发送的消息
        // MyMessage message = new MyMessage(correlationId.getId(), content);
        rabbitTemplate.convertAndSend(RabbitMQConfig.topicExchangeName, "foo.bar.yifan", message, correlationId);
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        System.out.println("Successfully received a callback!");
        System.out.println("callback id = " + correlationData);
        System.out.println("ack = " + ack);
        System.out.println("cause = " + cause);
    }

}
