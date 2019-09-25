package com.yifan.lightning.deal.service.impl;

import com.yifan.lightning.deal.dao.UserDOMapper;
import com.yifan.lightning.deal.dao.UserPasswordDOMapper;
import com.yifan.lightning.deal.dataobject.UserDO;
import com.yifan.lightning.deal.dataobject.UserPasswordDO;
import com.yifan.lightning.deal.service.UserService;
import com.yifan.lightning.deal.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// service层必须返回model对象而不能直接返回do，do只是对数据库表的直接映射
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDOMapper userDOMapper;
    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Override
    public UserModel getUserById(Integer id) {
        // 调用userDOMapper获取到对应的用户Data Object
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        if (userDO == null) { // 用户不存在
            return null;
        }
        // 调用userPasswordDOMapper获取到该用户对应的密码
        UserPasswordDO  userPasswordDO = userPasswordDOMapper.selectByUserId(id);

        return convertFromDataObject(userDO, userPasswordDO);
    }

    private UserModel convertFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO) {
        if (userDO == null) {
            return null;
        }
        UserModel userModel = new UserModel();
        // 把userDO中的属性复制到userModel对应属性
        BeanUtils.copyProperties(userDO, userModel);
        // 把密码赋给userModel
        if (userPasswordDO != null) {
            userModel.setEncryptPassword(userPasswordDO.getEncryptPassword());
        }

        return userModel;
    }
}
