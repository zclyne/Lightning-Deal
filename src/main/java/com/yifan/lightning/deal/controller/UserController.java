package com.yifan.lightning.deal.controller;

import com.alibaba.druid.util.StringUtils;
import com.yifan.lightning.deal.controller.viewobject.UserVO;
import com.yifan.lightning.deal.error.BusinessException;
import com.yifan.lightning.deal.error.EnumBusinessError;
import com.yifan.lightning.deal.response.CommonReturnType;
import com.yifan.lightning.deal.service.UserService;
import com.yifan.lightning.deal.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController("user")
@RequestMapping("/user")
// @CrossOrigin允许跨域访问，在header中加上Access-Control-Allow-Origin
// 但是默认的@CrossOrigin无法做到session共享，必须要加上括号内的这两个参数
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
// 通过让UserController继承BaseController的方法，来让UserController获得全局的异常处理方法
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    // 当前用户的http请求
    @Autowired
    private HttpServletRequest httpServletRequest;

    // redis template，是嵌入在spring boot中的redis的bean
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    AuthenticationManager authenticationManager;

    // 用户注册接口
    @PostMapping("/register")
    public CommonReturnType register(@RequestParam(name = "telphone") String telphone,
                                     @RequestParam(name = "otpCode") String otpCode,
                                     @RequestParam(name = "username") String username,
                                     @RequestParam(name = "age") Integer age,
                                     @RequestParam(name = "gender") Integer gender,
                                     @RequestParam(name = "password") String password,
                                     HttpServletRequest request) throws BusinessException {
        // 验证手机号和对应的otpCode相符合
//        String inSessionOtpCode = (String) this.httpServletRequest.getSession().getAttribute(telphone);
//        if (!StringUtils.equals(otpCode, inSessionOtpCode)) { // 使用alibaba druid库中的判断String相等的方法，不需要自己判断null
//            throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, "短信验证码错误");
//        }
        // 用户注册
        UserModel userModel = new UserModel();
        userModel.setUsername(username);
        userModel.setGender(new Byte(String.valueOf(gender.intValue())));
        userModel.setAge(age);
        userModel.setTelphone(telphone);
        userModel.setPassword(new BCryptPasswordEncoder().encode(password));

        int result = userService.register(userModel);
        if (result == 1) { // 注册成功
            // 注册后自动登录
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            authenticateUserAndSetSession(username, password, authorities, request);
            return CommonReturnType.create("Successfully registered a new user!");
        } else {
            return CommonReturnType.create("Failed to register!", "fail");
        }
    }

    // 设定已认证的user，并设置session
    private void authenticateUserAndSetSession(String username, String password, Collection<? extends GrantedAuthority> authorities, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password, authorities);

        // generate session if one doesn't exist
        request.getSession();

        token.setDetails(new WebAuthenticationDetails(request));
        try {
            Authentication authenticatedUser = authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 用户获取otp短信接口
    @PostMapping("/getotp")
    public CommonReturnType getOtp(@RequestParam(name = "telphone") String telphone) {
        // 按照一定规则生成otp验证码
        Random random = new Random();
        int randomInt = random.nextInt(89999);
        randomInt += 10000;
        String otpCode = String.valueOf(randomInt);
        // 将otp验证码同对应用户的手机号关联
        // 在企业应用中，由于服务器是分布式的，所以会通过使用redis保存key-value的方法关联用户的手机号和otp
        // 其中，key为手机号，value为otp
        // 此处暂时先使用session来处理
        httpServletRequest.getSession().setAttribute(telphone, otpCode);

        // 将otp验证码通过短信发送给用户，暂时先省略
        System.out.println("telphone = " + telphone + ", otpCode = " + otpCode);

        return CommonReturnType.create(null);
    }

    // 调用service服务获取对应id的用户对象，并返回给前端
    @RequestMapping("/get")
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
