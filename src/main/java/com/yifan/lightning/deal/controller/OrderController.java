package com.yifan.lightning.deal.controller;

import com.yifan.lightning.deal.error.BusinessException;
import com.yifan.lightning.deal.error.EnumBusinessError;
import com.yifan.lightning.deal.response.CommonReturnType;
import com.yifan.lightning.deal.service.OrderService;
import com.yifan.lightning.deal.service.model.OrderModel;
import com.yifan.lightning.deal.service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/order")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
public class OrderController extends BaseController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    // 下单请求接口
    @RequestMapping(value = "/createorder", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType createOrder(@RequestParam(name = "itemId") Integer itemId,
                                        @RequestParam(name = "promoId", required = false) Integer promoId,
                                        @RequestParam(name = "amount") Integer amount) throws BusinessException {

        // 获取用户的登录信息
        Boolean isLogin = (Boolean) this.httpServletRequest.getSession().getAttribute("IS_LOGIN");
        if (isLogin == null || !isLogin) {
            throw new BusinessException(EnumBusinessError.USER_NOT_LOGIN);
        }
        UserModel userModel = (UserModel) this.httpServletRequest.getSession().getAttribute("LOGIN_USER");

        // 创建订单
        OrderModel orderModel = orderService.createOrder(userModel.getId(), itemId, promoId, amount);

        return CommonReturnType.create(orderModel);

    }

}
