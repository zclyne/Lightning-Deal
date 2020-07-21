package com.yifan.lightning.deal.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;

// 用户购物车中的商品
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserItemModel {

    private Integer id;

    private Integer userId;

    private Integer itemId;

    private Integer amount;

    private String itemName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
}
