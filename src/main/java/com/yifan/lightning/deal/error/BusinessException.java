package com.yifan.lightning.deal.error;

// 包装器业务异常类实现
// 将由Spring的Handler统一处理
public class BusinessException extends Exception implements CommonError {

    private CommonError commonError;

    // 直接接受EnumBusinessError作为参数，构造业务异常
    public BusinessException(CommonError commonError) {
        super(); // 这里必须调用super，因为exception自身也有一些初始化操作
        this.commonError = commonError;
    }

    // 接受自定义errMsg的方式，构造业务异常
    public BusinessException(CommonError commonError, String errMsg) {
        super();
        this.commonError = commonError;
        this.commonError.setErrMsg(errMsg);
    }

    @Override
    public int getErrCode() {
        return this.commonError.getErrCode();
    }

    @Override
    public String getErrMsg() {
        return this.commonError.getErrMsg();
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.commonError.setErrMsg(errMsg);
        return this;
    }
}
