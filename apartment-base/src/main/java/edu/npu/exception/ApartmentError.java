package edu.npu.exception;

/**
 * @author : [wangminan]
 * @description : [自定义异常常量]
 */
public enum ApartmentError {
    UNKNOWN_ERROR("执行过程异常，请重试。"),
    PARAMS_ERROR("非法参数"),
    OBJECT_NULL("对象为空"),
    QUERY_NULL("查询结果为空"),
    REQUEST_NULL("请求参数为空");

    private final String msg;

    public String getErrMessage() {
        return msg;
    }

    ApartmentError(String msg) {
        this.msg = msg;
    }
}
