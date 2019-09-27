package com.yifan.lightning.deal.service.model;

import java.math.BigDecimal;

// 用户下单的交易模型
public class OrderModel {

    // 订单号使用String，因为会包含日期等信息
    private String id;

    // 下单的用户的id
    private Integer userId;

    // 购买的商品id
    private Integer itemId;

    // 若非空，则表示是以秒杀方式下单
    private Integer promoId;

    // 购买商品的单价，指购买时的商品价格
    // 当商品本身的价格发生变化时，订单内的商品购买时价格不会变化
    // 若promoId非空，则itemPrice是秒杀活动时的商品价格
    private BigDecimal itemPrice;

    // 购买数量
    private Integer amount;

    // 订单总价
    // 若promoId非空，则orderPrice为秒杀活动时下单的订单总价
    private BigDecimal orderPrice;

    public Integer getPromoId() {
        return promoId;
    }

    public void setPromoId(Integer promoId) {
        this.promoId = promoId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public BigDecimal getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(BigDecimal orderPrice) {
        this.orderPrice = orderPrice;
    }

    public BigDecimal getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(BigDecimal itemPrice) {
        this.itemPrice = itemPrice;
    }
}
