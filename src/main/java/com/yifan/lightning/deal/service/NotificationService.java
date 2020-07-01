package com.yifan.lightning.deal.service;

import com.yifan.lightning.deal.service.model.NotificationModel;
import com.yifan.lightning.deal.service.model.UserModel;

import java.util.List;

public interface NotificationService {

    void sendNotificationToAllUsers(String content, UserModel sender);

    List<NotificationModel> listAllNotifications();

}
