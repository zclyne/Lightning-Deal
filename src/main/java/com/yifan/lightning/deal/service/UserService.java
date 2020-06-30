package com.yifan.lightning.deal.service;

import com.yifan.lightning.deal.error.BusinessException;
import com.yifan.lightning.deal.service.model.UserModel;

import java.util.List;

public interface UserService {

    // 列出所有用户
    List<UserModel> listAllUsers();

    // 通过用户id获取用户对象
    UserModel getUserById(Integer id);

    // 从redis缓存中获取用户信息
    UserModel getUserByIdInCache(Integer id);

    // 用户注册
    int register(UserModel userModel) throws BusinessException;

    // 用户登录
    /*
    telphone: 用户注册手机
    password: 用户加密后的密码
     */
    UserModel validateLogin(String telphone, String encryptPassword) throws BusinessException;
}
