package com.yifan.lightning.deal.controller;

import com.yifan.lightning.deal.error.BusinessException;
import com.yifan.lightning.deal.error.EnumBusinessError;
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
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.*;

@RestController
@RequestMapping("/orders")
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

    @Autowired
    private Sender sender;

    private ExecutorService executorService;

    // 在类构建完成后，初始化executorService
    @PostConstruct
    public void init() {
        // 创建一个有20个可工作线程的线程池
        executorService = Executors.newFixedThreadPool(20);
    }

    // 生成秒杀令牌
    @PostMapping("/token")
    public CommonReturnType generateToken(@RequestParam(name = "itemId") Integer itemId,
                                          @RequestParam(name = "promoId") Integer promoId,
                                          Authentication authentication) throws BusinessException {
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
    @PostMapping
    public CommonReturnType createOrder(@RequestBody OrderModel orderModel,
                                        @RequestParam(name = "promoToken", required = false) String promoToken,
                                        Authentication authentication) throws BusinessException {

        // 获取用户
        UserModel userModel = (UserModel) authentication.getPrincipal();

        // 若存在秒杀活动，校验秒杀令牌是否正确
        Integer promoId = orderModel.getPromoId();
        Integer itemId = orderModel.getItemId();
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
                Integer amount = orderModel.getAmount();
                String stockLogId = itemService.initStockLog(itemId, amount);

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

    @GetMapping
    public CommonReturnType listOrdersForUser(Authentication authentication) {
        UserModel userModel = (UserModel) authentication.getPrincipal();
        List<OrderModel> orderModels = orderService.listOrdersByUserId(userModel.getId());
        return CommonReturnType.create(orderModels);
    }

    @GetMapping("/{orderId}")
    public CommonReturnType getOrder(@PathVariable("orderId") String orderId) {
        OrderModel orderModel = orderService.getOrderById(orderId);
        return CommonReturnType.create(orderModel);
    }

}
