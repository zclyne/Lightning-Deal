package com.yifan.lightning.deal.service.impl;

import com.yifan.lightning.deal.dao.UserDOMapper;
import com.yifan.lightning.deal.dao.UserPasswordDOMapper;
import com.yifan.lightning.deal.dataobject.UserDO;
import com.yifan.lightning.deal.dataobject.UserPasswordDO;
import com.yifan.lightning.deal.error.BusinessException;
import com.yifan.lightning.deal.error.EnumBusinessError;
import com.yifan.lightning.deal.service.UserService;
import com.yifan.lightning.deal.service.model.UserModel;
import com.yifan.lightning.deal.validator.ValidationResult;
import com.yifan.lightning.deal.validator.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

// service层必须返回model对象而不能直接返回do，do只是对数据库表的直接映射
@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    @Autowired
    private UserDOMapper userDOMapper;

    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        UserDO userDO = userDOMapper.selectByUsername(s);
        if (userDO == null) {
            return null;
        }
        int userId = userDO.getId();
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userId);
        return convertFromDataObject(userDO, userPasswordDO);
    }

    @Override
    public List<UserModel> listAllUsers() {
        List<UserDO> userDOs = userDOMapper.listAllUsers();
        List<UserModel> result = new ArrayList<>();
        userDOs.stream().forEach(userDO -> {
            UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
            UserModel userModel = convertFromDataObject(userDO, userPasswordDO);
            result.add(userModel);
        });
        return result;
    }

    @Override
    public UserModel getUserById(Integer id) {
        // 调用userDOMapper获取到对应的用户Data Object
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        if (userDO == null) { // 用户不存在
            return null;
        }
        // 调用userPasswordDOMapper获取到该用户对应的密码
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(id);

        return convertFromDataObject(userDO, userPasswordDO);
    }

    @Override
    public UserModel getUserByIdInCache(Integer id) {
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get("user_validate_" + id);
        if (userModel == null) { // 用户信息不在redis中
            userModel = this.getUserById(id);
            redisTemplate.opsForValue().set("user_validate_" + id, userModel);
            redisTemplate.expire("user_validate_" + id, 10, TimeUnit.MINUTES);
        }
        return userModel;
    }

    @Override
    @Transactional
    public int register(UserModel userModel) throws BusinessException {
        if (userModel == null) {
            throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        // 使用validator对userModel做校验
        ValidationResult result = validator.validate(userModel);
        if (result.isHasErrors()) {
            throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, result.getErrMsg());
        }

        UserDO userDO = convertFromModel(userModel);
        // 把新的user插入到数据库中
        // 这里使用insertSelective而不是insert，这是为了对空字段使用数据库提供的默认值，而不会被null覆盖
        try {
            userDOMapper.insertSelective(userDO);
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, "该手机号已被注册！");
        }
        // 插入完成后，MyBatis已经把主键回填到了userDO的id中，取出该id并放入userModel
        userModel.setId(userDO.getId());
        // 把新的user的密码插入到数据库中，同样使用selective
        UserPasswordDO userPasswordDO = convertPasswordFromModel(userModel);
        return userPasswordDOMapper.insertSelective(userPasswordDO);
    }

    private UserDO convertFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserDO userDO = new UserDO();
        userDO.setAge(userModel.getAge());
        userDO.setGender(userModel.getGender());
        userDO.setRole(userModel.getRole());
        userDO.setName(userModel.getUsername());
        userDO.setTelphone(userModel.getTelphone());
        return userDO;
    }

    private UserPasswordDO convertPasswordFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setEncryptPassword(userModel.getPassword());
        userPasswordDO.setUserId(userModel.getId());
        return userPasswordDO;
    }

    private UserModel convertFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO) {
        if (userDO == null) {
            return null;
        }
        UserModel userModel = new UserModel();
        // 把userDO中的属性复制到userModel对应属性
        BeanUtils.copyProperties(userDO, userModel);
        userModel.setUsername(userDO.getName()); // 由于属性名不同，所以需要手动设置username
        // 把密码赋给userModel
        if (userPasswordDO != null) {
            userModel.setPassword(userPasswordDO.getEncryptPassword());
        }

        return userModel;
    }

    @Override
    public UserModel validateLogin(String telphone, String encryptPassword) throws BusinessException {
        // 通过用户的手机获取用户信息
        UserDO userDO = userDOMapper.selectByTelphone(telphone);
        if (userDO == null) {
            throw new BusinessException(EnumBusinessError.USER_LOGIN_FAIL);
        }
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        UserModel userModel = convertFromDataObject(userDO, userPasswordDO);

        // 比对用户信息内加密的密码是否和传输进来的密码相匹配
        if (!StringUtils.equals(encryptPassword, userModel.getPassword())) {
            throw new BusinessException(EnumBusinessError.USER_LOGIN_FAIL);
        }

        return userModel;
    }
}
