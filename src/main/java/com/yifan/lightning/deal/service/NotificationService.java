package com.yifan.lightning.deal.service;

import com.yifan.lightning.deal.service.model.NotificationModel;
import com.yifan.lightning.deal.service.model.UserModel;

public interface NotificationService {

    void sendNotificationToAllUsers(String content, UserModel sender);

}
