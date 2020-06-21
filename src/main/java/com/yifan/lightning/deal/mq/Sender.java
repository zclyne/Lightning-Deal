package com.yifan.lightning.deal.mq;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    public void send(String content) {
        CorrelationData correlationId = new CorrelationData(UUID.randomUUID().toString());
        // 构造要发送的消息
        // MyMessage message = new MyMessage(correlationId.getId(), content);
        // rabbitTemplate.convertAndSend(RabbitmqConfig.topicExchangeName, "foo.bar.yifan", message, correlationId);
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        System.out.println("Successfully received a callback!");
        System.out.println("callback id = " + correlationData);
        System.out.println("ack = " + ack);
        System.out.println("cause = " + cause);
    }

}
