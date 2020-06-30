package com.yifan.lightning.deal.service.model;

import java.sql.Timestamp;

/**
 * 通知类NotificationModel和消息类ChatMessageModel的基本类，由于通知和聊天消息有很大一部分重合的内容
 * 因此抽象出这样一个基类
 */
public class BaseMessageModel {

    private Integer id;

    private UserModel sender;

    private String content;

    private Timestamp date;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UserModel getSender() {
        return sender;
    }

    public void setSender(UserModel sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

}
