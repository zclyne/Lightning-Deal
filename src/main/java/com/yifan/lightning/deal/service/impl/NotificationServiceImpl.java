package com.yifan.lightning.deal.service.impl;

import com.yifan.lightning.deal.controller.viewobject.NotificationVO;
import com.yifan.lightning.deal.dao.NotificationDOMapper;
import com.yifan.lightning.deal.dataobject.NotificationDO;
import com.yifan.lightning.deal.service.NotificationService;
import com.yifan.lightning.deal.service.UserService;
import com.yifan.lightning.deal.service.model.NotificationModel;
import com.yifan.lightning.deal.service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private NotificationDOMapper notificationDOMapper;

    @Autowired
    private UserService userService;

    @Override
    public void sendNotificationToAllUsers(String content, UserModel sender) {
        // save the notification to the database
        NotificationDO notificationDO = new NotificationDO();
        notificationDO.setContent(content);
        notificationDO.setSenderId(sender.getId());
        notificationDO.setTimestamp(new Timestamp(System.currentTimeMillis()));
        notificationDOMapper.insert(notificationDO);

        // broadcast the notification to all users through websocket
        NotificationVO notificationVO = convertToVOFromNotificationDO(notificationDO);
        simpMessagingTemplate.convertAndSend("/topic/notification", notificationVO);
    }

    @Override
    public List<NotificationModel> listAllNotifications() {
        List<NotificationDO> notificationDOs = notificationDOMapper.listNotifications();
        List<NotificationModel> notificationModels = new ArrayList<>();
        notificationDOs.forEach(notificationDO -> {
            notificationModels.add(convertFromNotificationDO(notificationDO));
        });
        return notificationModels;
    }

    private NotificationModel convertFromNotificationDO(NotificationDO notificationDO) {
        if (notificationDO == null) {
            return null;
        }
        NotificationModel notificationModel = new NotificationModel();
        notificationModel.setId(notificationDO.getId());
        notificationModel.setSender(userService.getUserById(notificationDO.getSenderId()));
        notificationModel.setContent(notificationDO.getContent());
        notificationModel.setTimestamp(notificationDO.getTimestamp());
        return notificationModel;
    }

    private NotificationVO convertToVOFromNotificationDO(NotificationDO notificationDO) {
        if (notificationDO == null) {
            return null;
        }
        UserModel sender = userService.getUserById(notificationDO.getSenderId());
        NotificationVO notificationVO = new NotificationVO();
        notificationVO.setContent(notificationDO.getContent());
        notificationVO.setSenderName(sender.getUsername());
        notificationVO.setTimestamp(notificationDO.getTimestamp());
        return notificationVO;
    }

}
