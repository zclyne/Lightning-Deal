package com.yifan.lightning.deal.controller;

import com.yifan.lightning.deal.controller.viewobject.UserVO;
import com.yifan.lightning.deal.error.BusinessException;
import com.yifan.lightning.deal.error.EnumBusinessError;
import com.yifan.lightning.deal.response.CommonReturnType;
import com.yifan.lightning.deal.service.UserService;
import com.yifan.lightning.deal.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("user")
@RequestMapping("/user")
// 通过让UserController继承BaseController的方法，来让UserController获得全局的异常处理方法
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @RequestMapping("/get")
    // 调用service服务获取对应id的用户对象，并返回给前端
    public CommonReturnType getUser(@RequestParam(name="id") Integer id) throws BusinessException {
        UserModel userModel = userService.getUserById(id);
        // 若获取的对应用户信息不存在
        if (userModel == null) {
            throw new BusinessException(EnumBusinessError.USER_NOT_EXIST);
        }
        // 将核心领域模型用户对象转化为供UI使用的VO
        UserVO userVO = convertFromModel(userModel);
        // 将userVO包装到CommonReturnType中，并返回通用对象
        return CommonReturnType.create(userVO);
    }

    private UserVO convertFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel, userVO);
        return userVO;
    }

}
