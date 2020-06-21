package com.yifan.lightning.deal.mq;

import com.alibaba.fastjson.JSON;
import com.yifan.lightning.deal.dao.ItemStockDOMapper;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

// @Component
public class MqConsumer {

    private DefaultMQPushConsumer consumer;

    // 从application.properties中注入rocketmq配置
    @Value("${mq.nameserver.addr}")
    private String nameAddr;

    @Value("${mq.topicname}")
    private String topicName;

    @Autowired
    private ItemStockDOMapper itemStockDOMapper;

    @PostConstruct
    public void init() throws MQClientException {
        // consumer的初始化
        consumer = new DefaultMQPushConsumer("stock_consumer_group");
        consumer.setNamesrvAddr(nameAddr);
        consumer.subscribe(topicName, "*"); // 说明consumer订阅的是哪一个topic，并订阅所有消息

        // 在有消息发来时，实现真正的数据库减库存逻辑
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                Message msg = list.get(0);
                // 从消息中获取数据。放入时使用的是map类型，所以取出后要转换回map
                String jsonString = new String(msg.getBody());
                Map<String, Object> map = JSON.parseObject(jsonString, Map.class);
                Integer itemId = (Integer) map.get("itemId");
                Integer amount = (Integer) map.get("amount");
                // 数据库减库存
                itemStockDOMapper.decreaseStock(itemId, amount);

                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS; // 若返回SUCCESS，则认为消息被成功消费，下次不会再投放
            }
        });

        consumer.start();
    }

}
