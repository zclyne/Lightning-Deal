package com.yifan.lightning.deal.service;

import com.yifan.lightning.deal.error.BusinessException;
import com.yifan.lightning.deal.service.model.ItemModel;

import java.util.List;

public interface ItemService {

    // 创建商品
    ItemModel createItem(ItemModel itemModel) throws BusinessException;

    // 商品列表浏览
    List<ItemModel> listItem();

    // 商品详情浏览
    ItemModel getItemById(Integer id);

    // item及promo model缓存模型
    ItemModel getItemByIdInCache(Integer id);

    // 减库存
    boolean decreaseStock(Integer itemId, Integer amount) throws BusinessException;

    // 库存回滚
    boolean increaseStock(Integer itemId, Integer amount);

    // 商品销量增加
    void increaseSales(Integer itemId, Integer amount) throws BusinessException;

    // 初始化库存流水，返回值是库存流水id
    String initStockLog(Integer itemId, Integer amount);

}
