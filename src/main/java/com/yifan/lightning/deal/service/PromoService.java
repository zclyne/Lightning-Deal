package com.yifan.lightning.deal.service;

import com.yifan.lightning.deal.error.BusinessException;
import com.yifan.lightning.deal.service.model.PromoModel;

public interface PromoService {

    // 根据itemId获取即将进行的或正在进行的秒杀活动
    PromoModel getPromoByItemId(Integer itemId);

    // 活动发布
    void publicPromo(Integer promoId);

    // 生成秒杀用的令牌
    String generateSecondKillToken(Integer promoId, Integer itemId, Integer userId);

}
