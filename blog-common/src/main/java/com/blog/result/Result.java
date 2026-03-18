package com.blog.result;

import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int SUCCESS = 1;
    public static final int BUSINESS_ERROR = 0;
    public static final int VALIDATION_ERROR = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int CONFLICT = 409;
    public static final int INTERNAL_ERROR = 500;

    private Integer code;//状态码 1:成功 0:失败
    private String msg;//结果消息
    private T data;//返回数据

    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.code = SUCCESS;
        return result;
    }

    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.code = SUCCESS;
        return result;
    }

    public static <T> Result<T> error(String msg) {
        return error(BUSINESS_ERROR, msg);
    }

    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<T>();
        result.msg = msg;
        result.code = code;
        return result;
    }
}
