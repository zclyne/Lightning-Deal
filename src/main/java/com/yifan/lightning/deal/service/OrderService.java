package com.yifan.lightning.deal.service;

import com.yifan.lightning.deal.error.BusinessException;
import com.yifan.lightning.deal.service.model.OrderModel;

import java.util.List;

public interface OrderService {
    // 方法1. 通过前端url上传入秒杀活动id，下单接口内校验对应id是否属于对应商品且活动已开始
    // 方法2. 直接在下单接口内判断对应的商品是否存在秒杀活动，若存在进行中的秒杀活动，则以秒杀价格下单
    // 此处选择方法1
    OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount, String stockLogId) throws BusinessException;

    OrderModel getOrderById(String orderId);

    List<OrderModel> listOrdersByUserId(Integer userId);
}
