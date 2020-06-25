package com.yifan.lightning.deal.controller;

import com.yifan.lightning.deal.error.BusinessException;
import com.yifan.lightning.deal.error.EnumBusinessError;
import com.yifan.lightning.deal.mq.MqProducer;
import com.yifan.lightning.deal.mq.Sender;
import com.yifan.lightning.deal.response.CommonReturnType;
import com.yifan.lightning.deal.service.ItemService;
import com.yifan.lightning.deal.service.OrderService;
import com.yifan.lightning.deal.service.PromoService;
import com.yifan.lightning.deal.service.UserService;
import com.yifan.lightning.deal.service.model.OrderModel;
import com.yifan.lightning.deal.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
<<<<<<< HEAD
import org.springframework.security.core.userdetails.UserDetails;
=======
>>>>>>> feature/rabbitmq
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.*;

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
    private UserService userService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RedisTemplate redisTemplate;

<<<<<<< HEAD
    // @Autowired
    private MqProducer mqProducer;
=======
    @Autowired
    private Sender sender;
>>>>>>> feature/rabbitmq

    private ExecutorService executorService;

    // 在类构建完成后，初始化executorService
    @PostConstruct
    public void init() {
        // 创建一个有20个可工作线程的线程池
        executorService = Executors.newFixedThreadPool(20);
    }

    // 生成秒杀令牌
    @PostMapping("/generatetoken")
    public CommonReturnType generateToken(@RequestParam(name = "itemId") Integer itemId,
                                          @RequestParam(name = "promoId") Integer promoId,
                                          Authentication authentication) throws BusinessException {
        // token方式获取用户信息
        // 先从request的路径中获取token，也可以在方法的参数中加一个token
//        String token = httpServletRequest.getParameterMap().get("token")[0];
//        if (StringUtils.isEmpty(token)) { // 用户未登录
//            throw new BusinessException(EnumBusinessError.USER_NOT_LOGIN);
//        }
        // 用户已登录，从redis中以token为键，获取对应的userModel
        // UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);

        // 获取用户
        UserModel userModel = (UserModel) authentication.getPrincipal();

        if (userModel == null) { // 会话过期
            throw new BusinessException(EnumBusinessError.USER_NOT_LOGIN);
        }
        // 获取秒杀访问令牌
        String promoToken = promoService.generateSecondKillToken(promoId, itemId, userModel.getId());
        if (promoToken == null) { // 没有秒杀活动，则以正常方式下单
            // throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, "生成令牌失败");
            return CommonReturnType.create(null);
        }

        // 返回对应的结果
        return CommonReturnType.create(promoToken);
    }

    // 下单请求接口
    @PostMapping("/createorder")
    public CommonReturnType createOrder(@RequestParam(name = "itemId") Integer itemId,
                                        @RequestParam(name = "promoId", required = false) Integer promoId,
                                        @RequestParam(name = "promoToken", required = false) String promoToken,
                                        @RequestParam(name = "amount") Integer amount,
                                        Authentication authentication) throws BusinessException {

        // 从session中获取用户信息
        // 获取用户的登录信息
//        Boolean isLogin = (Boolean) this.httpServletRequest.getSession().getAttribute("IS_LOGIN");
//        if (isLogin == null || !isLogin) {
//            throw new BusinessException(EnumBusinessError.USER_NOT_LOGIN);
//        }
//        UserModel userModel = (UserModel) this.httpServletRequest.getSession().getAttribute("LOGIN_USER");

        // token方式获取用户信息
        // 先从request的路径中获取token，也可以在方法的参数中加一个token
//        String token = httpServletRequest.getParameterMap().get("token")[0];
//        if (StringUtils.isEmpty(token)) { // 用户未登录
//            throw new BusinessException(EnumBusinessError.USER_NOT_LOGIN);
//        }

        // 获取用户
        UserModel userModel = (UserModel) authentication.getPrincipal();

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

        // 同步调用线程池的submit方法，在异步线程队列中完成库存流水、创建订单和还令牌操作
        // 拥塞窗口大小为20，用来队列化泄洪
        Future<Object> future = executorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {

                // 加入库存流水init状态
                String stockLogId = itemService.initStockLog(itemId, amount);

                // 通过事务型消息创建订单
                // OrderModel orderModel = orderService.createOrder(userModel.getId(), itemId, promoId, amount);
                // 参数中要传入库存流水id，用户消息队列来定期检查消息状态
//                if (!mqProducer.transactionAsyncReduceStock(userModel.getId(), itemId, promoId, amount, stockLogId)) {
//                    // 如果下单失败，抛出异常
//                    throw new BusinessException(EnumBusinessError.UNKNOWN_ERROR, "下单失败");
//                }
                sender.createOrderAndDecreseStock(userModel.getId(), itemId, promoId, amount, stockLogId);

                if (promoId != null) { // 若存在秒杀活动，下单完成后，把令牌还回去
                    redisTemplate.opsForValue().increment("promo_door_count_" + promoId, 1);
                }

                return null;
            }
        });

        // 等待future完成
        try {
            future.get();
        } catch (InterruptedException e) {
            throw new BusinessException(EnumBusinessError.UNKNOWN_ERROR);
        } catch (ExecutionException e) {
            throw new BusinessException(EnumBusinessError.UNKNOWN_ERROR);
        }

        return CommonReturnType.create(null);

    }

}
