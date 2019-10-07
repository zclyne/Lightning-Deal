package com.yifan.lightning.deal.service.impl;

import com.yifan.lightning.deal.dao.PromoDOMapper;
import com.yifan.lightning.deal.dataobject.PromoDO;
import com.yifan.lightning.deal.service.ItemService;
import com.yifan.lightning.deal.service.PromoService;
import com.yifan.lightning.deal.service.model.ItemModel;
import com.yifan.lightning.deal.service.model.PromoModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    private PromoDOMapper promoDOMapper;

    @Autowired
    private ItemService itemService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public PromoModel getPromoByItemId(Integer itemId) {
        // 获取商品对应的秒杀活动
        PromoDO promoDO = promoDOMapper.selectByItemId(itemId);
        if (promoDO == null) { // 该商品不存在对应秒杀活动
            return null;
        }

        // 把do转化为model
        PromoModel promoModel = this.convertFromDataObject(promoDO);

        // 判断秒杀活动是否即将开始或正在进行
        if (promoModel.getStartDate().isAfterNow()) { // 秒杀活动还未开始
            promoModel.setStatus(1);
        } else if (promoModel.getEndDate().isBeforeNow()) { // 秒杀活动已结束
            promoModel.setStatus(3);
        } else { // 秒杀活动正在进行
            promoModel.setStatus(2);
        }

        return promoModel;
    }

    @Override
    public void publicPromo(Integer promoId) {
        // 通过活动id获取活动
        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);
        if (promoDO.getItemId() == null || promoDO.getItemId().intValue() == 0) { // 活动没有对应的商品，直接返回
            return;
        }
        ItemModel itemModel = itemService.getItemById(promoDO.getItemId());
        // 将库存同步到redis内
        redisTemplate.opsForValue().set("promo_item_stock_" + itemModel.getId(), itemModel.getStock());
    }

    private PromoModel convertFromDataObject(PromoDO promoDO) {
        if (promoDO == null) {
            return null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDO, promoModel);
        promoModel.setPromoItemPrice(new BigDecimal(promoDO.getPromoItemPrice()));
        promoModel.setStartDate(new DateTime(promoDO.getStartDate()));
        promoModel.setEndDate(new DateTime(promoDO.getEndDate()));

        return promoModel;
    }

}
