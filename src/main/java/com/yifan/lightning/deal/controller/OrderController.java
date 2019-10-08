package com.yifan.lightning.deal.controller;

import com.yifan.lightning.deal.error.BusinessException;
import com.yifan.lightning.deal.error.EnumBusinessError;
import com.yifan.lightning.deal.mq.MqProducer;
import com.yifan.lightning.deal.response.CommonReturnType;
import com.yifan.lightning.deal.service.ItemService;
import com.yifan.lightning.deal.service.OrderService;
import com.yifan.lightning.deal.service.PromoService;
import com.yifan.lightning.deal.service.model.OrderModel;
import com.yifan.lightning.deal.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/order")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
public class OrderController extends BaseController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private PromoService promoService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MqProducer mqProducer;

    // 生成秒杀令牌
    @RequestMapping(value = "/generatetoken", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType generateToken(@RequestParam(name = "itemId") Integer itemId,
                                        @RequestParam(name = "promoId") Integer promoId) throws BusinessException {
        // token方式获取用户信息
        // 先从request的路径中获取token，也可以在方法的参数中加一个token
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)) { // 用户未登录
            throw new BusinessException(EnumBusinessError.USER_NOT_LOGIN);
        }
        // 用户已登录，从redis中以token为键，获取对应的userModel
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if (userModel == null) { // 会话过期
            throw new BusinessException(EnumBusinessError.USER_NOT_LOGIN);
        }
        // 获取秒杀访问令牌
        String promoToken = promoService.generateSecondKillToken(promoId, itemId, userModel.getId());
        if (promoToken == null) {
            throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, "生成令牌失败");
        }

        // 返回对应的结果
        return CommonReturnType.create(promoToken);
    }

    // 下单请求接口
    @RequestMapping(value = "/createorder", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType createOrder(@RequestParam(name = "itemId") Integer itemId,
                                        @RequestParam(name = "promoId", required = false) Integer promoId,
                                        @RequestParam(name = "promoToken", required = false) String promoToken,
                                        @RequestParam(name = "amount") Integer amount) throws BusinessException {

        // 从session中获取用户信息
        // 获取用户的登录信息
//        Boolean isLogin = (Boolean) this.httpServletRequest.getSession().getAttribute("IS_LOGIN");
//        if (isLogin == null || !isLogin) {
//            throw new BusinessException(EnumBusinessError.USER_NOT_LOGIN);
//        }
//        UserModel userModel = (UserModel) this.httpServletRequest.getSession().getAttribute("LOGIN_USER");

        // token方式获取用户信息
        // 先从request的路径中获取token，也可以在方法的参数中加一个token
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)) { // 用户未登录
            throw new BusinessException(EnumBusinessError.USER_NOT_LOGIN);
        }
        // 用户已登录，从redis中以token为键，获取对应的userModel
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if (userModel == null) { // 会话过期
            throw new BusinessException(EnumBusinessError.USER_NOT_LOGIN);
        }

        // 若存在秒杀活动，校验秒杀令牌是否正确
        if (promoId != null) {
            String inRedisPromoToken = (String) redisTemplate.opsForValue().get("promo_token_" + promoId + "_userId_" + userModel.getId() + "_itemId_" + itemId);
            if (inRedisPromoToken == null || !StringUtils.equals(promoToken, inRedisPromoToken)) {
                throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, "秒杀令牌校验失败");
            }
        }

        // 判断商品是否售罄，若售罄标识存在，则直接返回下单失败
        if (redisTemplate.hasKey("promo_item_stock_invalid_" + itemId)) {
            throw new BusinessException(EnumBusinessError.STOCK_NOT_ENOUGH);
        }

        // 加入库存流水init状态
        String stockLogId = itemService.initStockLog(itemId, amount);

        // 通过事务型消息创建订单
        // OrderModel orderModel = orderService.createOrder(userModel.getId(), itemId, promoId, amount);
        // 参数中要传入库存流水id，用户消息队列来定期检查消息状态
        if (!mqProducer.transactionAsyncReduceStock(userModel.getId(), itemId, promoId, amount, stockLogId)) {
            // 如果下单失败，抛出异常
            throw new BusinessException(EnumBusinessError.UNKNOWN_ERROR, "下单失败");
        }

        return CommonReturnType.create(null);

    }

}
