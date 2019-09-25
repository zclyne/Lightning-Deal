package com.yifan.lightning.deal.response;

// 通用的http请求返回对象，包含status和data
public class CommonReturnType {

    // status表明对应请求的返回处理结果，success或fail
    private String status;

    // 若status==success，则data为前端需要的json数据
    // 若status==fail，则data为通用的错误码格式
    private Object data;

    // 通用创建方法
    // 如果参数只有result，则默认为success
    public static CommonReturnType create(Object result) {
        return CommonReturnType.create(result, "success");
    }

    public static CommonReturnType create(Object result, String status) {
        CommonReturnType type = new CommonReturnType();
        type.setStatus(status);
        type.setData(result);
        return type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
