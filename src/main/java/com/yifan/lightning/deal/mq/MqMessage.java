package com.yifan.lightning.deal.mq;

import java.util.Map;

public class MqMessage {

    private String id;
    private Map<String, Object> map;

    public MqMessage() {
    }

    public MqMessage(String id, Map<String, Object> map) {
        this.id = id;
        this.map = map;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }
}
