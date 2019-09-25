package com.yifan.lightning.deal.error;

// 通用错误接口
public interface CommonError {
    int getErrCode();
    String getErrMsg();
    CommonError setErrMsg(String errMsg);
}
