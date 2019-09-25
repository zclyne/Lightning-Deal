package com.yifan.lightning.deal.service;

import com.yifan.lightning.deal.service.model.UserModel;

public interface UserService {
    // 通过用户id获取用户对象
    UserModel getUserById(Integer id);
}
