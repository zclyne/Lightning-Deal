package com.yifan.lightning.deal.mq;

import com.alibaba.fastjson.JSON;
import com.yifan.lightning.deal.error.BusinessException;
import com.yifan.lightning.deal.service.OrderService;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@Component
public class MqProducer {

    private DefaultMQProducer producer;

    private TransactionMQProducer transactionMQProducer;

    // 从application.properties中注入rocketmq配置
    @Value("${mq.nameserver.addr}")
    private String nameAddr;

    @Value("${mq.topicname}")
    private String topicName;

    @Autowired
    private OrderService orderService;

    @PostConstruct
    public void init() throws MQClientException {
        // mq producer初始化
        // producer的group没有意义，可以随便选取
        producer = new DefaultMQProducer("producer_group");
        producer.setNamesrvAddr(nameAddr);
        producer.start();

        transactionMQProducer = new TransactionMQProducer("transaction_producer_group");
        transactionMQProducer.setNamesrvAddr(nameAddr);
        transactionMQProducer.start();

        transactionMQProducer.setTransactionListener(new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message message, Object o) {
                // 真正要做的事放在这里：创建订单
                Integer itemId = (Integer) ((Map) o).get("itemId");
                Integer promoId = (Integer) ((Map) o).get("promoId");
                Integer userId = (Integer) ((Map) o).get("userId");
                Integer amount = (Integer) ((Map) o).get("amount");
                try {
                    orderService.createOrder(userId, itemId, promoId, amount);
                } catch (BusinessException e) {
                    e.printStackTrace();
                    // 如果有异常，则事务需要回滚，返回ROLLBACK
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
                return LocalTransactionState.COMMIT_MESSAGE; // 如果订单创建成功，则发送commit消息
            }

            // 如果在executeLocalTransaction方法中，创建订单出现故障或等待时间太长而始终不返回，消息中间件
            // 就会调用下面这个checkLocalTransaction方法
            // 在创建订单的方法中添加操作流水数据，使得可以通过checkLocalTransaction来判断当前的订单状态
            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
                // 根据是否扣减库存成功，来判断要返回COMMIT还是ROLLBACK还是继续UNKNOW
                String jsonString = new String(messageExt.getBody());
                Map<String, Object> map = JSON.parseObject(jsonString, Map.class);
                Integer itemId = (Integer) map.get("itemId");
                Integer amount = (Integer) map.get("amount");
                // 根据从消息中取出的itemId和amount到redis中判断是否扣减成功
                return null;
            }
        });

    }

    // 事务型同步库存扣减消息
    public boolean transactionAsyncReduceStock(Integer userId, Integer itemId, Integer promoId, Integer amount) {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("itemId", itemId);
        bodyMap.put("amount", amount);

        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("itemId", itemId);
        argsMap.put("amount", amount);
        argsMap.put("userId", userId);
        argsMap.put("promoId", promoId);

        // 生成并投放事务型消息
        TransactionSendResult sendResult = null;
        Message message = new Message(topicName, "increase", JSON.toJSON(bodyMap).toString().getBytes(Charset.forName("UTF-8")));
        try {
            // sendMessageInTransaction与普通send的区别：
            // 事务型消息有2个阶段，在消息被投递给broker后一开始是prepare阶段，此时该消息不能被consumer所消费
            // 而是在producer本地执行executeLocalTransaction方法中的内容
            // argsMap将被传递到executeLocalTransaction方法的Object参数上
            sendResult = transactionMQProducer.sendMessageInTransaction(message, argsMap);

        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        }
        if (sendResult.getLocalTransactionState() == LocalTransactionState.ROLLBACK_MESSAGE) { // 若消息发送失败，rollback
            return false;
        } else if (sendResult.getLocalTransactionState() == LocalTransactionState.COMMIT_MESSAGE) {
            return true;
        } else {
            return false;
        }
    }

    // 同步库存扣减消息
    public boolean asyncReduceStock(Integer itemId, Integer amount) {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("itemId", itemId);
        bodyMap.put("amount", amount);
        // 生成并投放消息
        Message message = new Message(topicName, "increase", JSON.toJSON(bodyMap).toString().getBytes(Charset.forName("UTF-8")));
        try {
            producer.send(message);
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        } catch (RemotingException e) {
            e.printStackTrace();
            return false;
        } catch (MQBrokerException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
