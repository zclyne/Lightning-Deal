package com.yifan.lightning.deal.controller.websocket;

import com.yifan.lightning.deal.controller.viewobject.NotificationVO;
import com.yifan.lightning.deal.response.CommonReturnType;
import com.yifan.lightning.deal.service.NotificationService;
import com.yifan.lightning.deal.service.model.NotificationModel;
import com.yifan.lightning.deal.service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    NotificationService notificationService;

    @PostMapping("/broadcast")
    public CommonReturnType broadcastNewNotification(@RequestBody String content,
                                                     Authentication authentication) {
        UserModel sender = (UserModel) authentication.getPrincipal();
        notificationService.sendNotificationToAllUsers(content, sender);
        return CommonReturnType.create("Success");
    }

    @GetMapping("/list")
    public CommonReturnType listNotifications() {
        List<NotificationModel> notificationModels = notificationService.listAllNotifications();
        List<NotificationVO> notificationVOs = new ArrayList<>();
        notificationModels.forEach(notificationModel -> {
            notificationVOs.add(convertFromNotificationModel(notificationModel));
        });
        return CommonReturnType.create(notificationVOs);
    }

    private NotificationVO convertFromNotificationModel(NotificationModel notificationModel) {
        if (notificationModel == null) {
            return null;
        }
        NotificationVO notificationVO = new NotificationVO();
        notificationVO.setContent(notificationModel.getContent());
        notificationVO.setSenderName(notificationModel.getSender().getUsername());
        notificationVO.setTimestamp(notificationModel.getTimestamp());
        return notificationVO;
    }

}
