package com.yifan.lightning.deal.service.impl;

import com.yifan.lightning.deal.dao.ItemDOMapper;
import com.yifan.lightning.deal.dao.UserItemDOMapper;
import com.yifan.lightning.deal.dataobject.ItemDO;
import com.yifan.lightning.deal.dataobject.UserItemDO;
import com.yifan.lightning.deal.service.UserItemService;
import com.yifan.lightning.deal.service.model.UserItemModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserItemServiceImpl implements UserItemService {

    @Autowired
    UserItemDOMapper userItemDOMapper;

    @Autowired
    ItemDOMapper itemDOMapper;

    @Override
    public Integer addItem(Integer userId, Integer itemId, Integer amount) {
        UserItemDO userItemDO = new UserItemDO();
        userItemDO.setItemId(itemId);
        userItemDO.setUserId(userId);
        userItemDO.setAmount(amount);
        return userItemDOMapper.insert(userItemDO);
    }

    @Override
    public List<UserItemModel> listItemByUserId(Integer userId) {
        List<UserItemDO> userItemDOList = userItemDOMapper.listByUserId(userId);
        List<UserItemModel> result = new ArrayList<>();
        for (UserItemDO userItemDO : userItemDOList) {
            result.add(convertFromUserItemDO(userItemDO));
        }
        return result;
    }

    @Override
    public UserItemModel selectItemByUserIdAndItemId(Integer userId, Integer itemId) {
        UserItemDO userItemDO = userItemDOMapper.selectByUserIdAndItemId(userId, itemId);
        return convertFromUserItemDO(userItemDO);
    }

    @Override
    public Integer updateByPrimaryKeySelective(UserItemModel userItemModel) {
        UserItemDO userItemDO = new UserItemDO();
        userItemDO.setId(userItemModel.getId());
        userItemDO.setUserId(userItemModel.getUserId());
        userItemDO.setItemId(userItemModel.getItemId());
        userItemDO.setAmount(userItemModel.getAmount());
        return userItemDOMapper.updateByPrimaryKeySelective(userItemDO);
    }

    @Override
    public Integer deleteItemsByUserIdAndItemId(Integer userId, List<Integer> itemIds) {
        int result = 0;
        for (Integer itemId : itemIds) {
            result += userItemDOMapper.deleteByUserIdAndItemId(userId, itemId);
        }
        return result;
    }

    private UserItemModel convertFromUserItemDO(UserItemDO userItemDO) {
        if (userItemDO == null) {
            return null;
        }
        UserItemModel userItemModel = new UserItemModel();
        BeanUtils.copyProperties(userItemDO, userItemModel);
        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(userItemDO.getItemId());
        userItemModel.setItemName(itemDO.getTitle());
        return userItemModel;
    }

}
