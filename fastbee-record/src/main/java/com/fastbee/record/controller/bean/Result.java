package com.fastbee.record.controller.bean;

public class Result<T> {

    public Result() {
    }

    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    private int code;
    private String msg;
    private T data;


    public static <T> Result<T> success(T t, String msg) {
        return new Result<>(ErrorCode.SUCCESS.getCode(), msg, t);
    }

    public static <T> Result<T> success(T t) {
        return success(t, ErrorCode.SUCCESS.getMsg());
    }

    public static <T> Result<T> fail(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    public static <T> Result<T> fail(ErrorCode errorCode) {
        return fail(errorCode.getCode(), errorCode.getMsg());
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
