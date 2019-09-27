package com.yifan.lightning.deal.error;

// 用枚举类处理业务错误，实现通用错误接口
public enum EnumBusinessError implements CommonError {
    // 通用错误类型10001，防止各个不同模块对入参错误都要定义各自的错误码
    // 对于具体不同模块的入参错误，通过setErrMsg()方法来改变errMsg
    PARAMETER_VALIDATION_ERROR(10001, "参数不合法"),
    // 未知错误10002
    UNKNOWN_ERROR(10002, "未知错误"),
    // 全局错误码在实际开发中很重要
    // 20000开头为用户信息相关错误定义
    USER_NOT_EXIST(20001, "用户不存在"),
    USER_LOGIN_FAIL(20002, "用户手机号或密码不正确"),
    USER_NOT_LOGIN(20003, "用户未登录"),
    // 30000开头为交易信息错误定义
    STOCK_NOT_ENOUGH(30001, "库存不足")
    // 若后续有新的错误，只需在下面新增
    ;

    private EnumBusinessError(int errCode, String errMsg) {
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    private int errCode;
    private String errMsg;

    @Override
    public int getErrCode() {
        return this.errCode;
    }

    @Override
    public String getErrMsg() {
        return this.errMsg;
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.errMsg = errMsg;
        return this;
    }
}
