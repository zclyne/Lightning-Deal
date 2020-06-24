package com.yifan.lightning.deal.mq;

import com.yifan.lightning.deal.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class Receiver {

    @RabbitListener(queues = RabbitMQConfig.queueName, ackMode = "MANUAL")
    public void receiveMessage(MqMessage message) {
        System.out.println("Received <" + message + ">");
    }

}
