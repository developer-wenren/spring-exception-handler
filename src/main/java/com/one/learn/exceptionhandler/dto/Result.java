package com.one.learn.exceptionhandler.dto;

/**
 * 响应客户端的数据传输对象
 *
 * @author One
 * @date 2019/05/18
 */
public class Result<T> {
    private String message;
    private int code;
    private T data;

    public Result() {
    }

    public Result(int code, String message, T data) {
        this.message = message;
        this.code = code;
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Result{" +
                "message='" + message + '\'' +
                ", code='" + code + '\'' +
                ", data=" + data +
                '}';
    }
}
