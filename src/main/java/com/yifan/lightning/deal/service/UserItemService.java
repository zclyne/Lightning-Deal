package com.yifan.lightning.deal.service;

import com.yifan.lightning.deal.service.model.UserItemModel;

import java.util.List;

public interface UserItemService {

    Integer addItem(Integer userId, Integer itemId, Integer amount);

    List<UserItemModel> listItemByUserId(Integer userId);

    UserItemModel selectItemByUserIdAndItemId(Integer userId, Integer itemId);

    Integer updateByPrimaryKeySelective(UserItemModel userItemModel);

    Integer deleteItemsByUserIdAndItemId(Integer userId, List<Integer> itemIds);

}
