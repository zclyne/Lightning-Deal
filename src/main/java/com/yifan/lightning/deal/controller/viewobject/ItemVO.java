package com.yifan.lightning.deal.controller.viewobject;

import java.math.BigDecimal;

public class ItemVO {

    private Integer id;

    // 名称
    private String title;

    // 价格
    private BigDecimal price;

    // 库存
    private Integer stock;

    // 描述
    private String description;

    // 销量
    private Integer sales;

    // 描述图片url
    private String imgUrl;

    // 秒杀活动的信息
    // 记录商品是否在秒杀活动中，及对应的状态
    // 1: 活动待开始；2: 活动进行中；3: 没有活动
    private Integer promoStatus;

    // 秒杀活动价格
    private BigDecimal promoPrice;

    // 秒杀活动id
    private Integer promoId;

    // 秒杀活动开始时间
    private String startDate;

    // 秒杀活动结束时间
    private String endDate;

    public Integer getPromoStatus() {
        return promoStatus;
    }

    public void setPromoStatus(Integer promoStatus) {
        this.promoStatus = promoStatus;
    }

    public BigDecimal getPromoPrice() {
        return promoPrice;
    }

    public void setPromoPrice(BigDecimal promoPrice) {
        this.promoPrice = promoPrice;
    }

    public Integer getPromoId() {
        return promoId;
    }

    public void setPromoId(Integer promoId) {
        this.promoId = promoId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
