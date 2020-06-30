package com.yifan.lightning.deal.service.impl;

import com.yifan.lightning.deal.service.NotificationService;
import com.yifan.lightning.deal.service.model.NotificationModel;
import com.yifan.lightning.deal.service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void sendNotificationToAllUsers(String content, UserModel sender) {
        // TODO: send the notification to the topic '/topic/notification'
        NotificationModel notificationModel = new NotificationModel();
        notificationModel.setContent(content);
        notificationModel.setSender(sender);
        simpMessagingTemplate.convertAndSend("/topic/notification", notificationModel);
    }
}
