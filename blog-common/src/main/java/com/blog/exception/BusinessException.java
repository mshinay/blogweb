package com.blog.exception;

import com.blog.result.Result;

public class BusinessException extends BaseException {

    public BusinessException(String message) {
        this(Result.BUSINESS_ERROR, 400, message);
    }

    public BusinessException(Integer code, Integer httpStatus, String message) {
        super(code, httpStatus, message);
    }

    public static BusinessException notFound(String message) {
        return new BusinessException(Result.NOT_FOUND, 404, message);
    }

    public static BusinessException conflict(String message) {
        return new BusinessException(Result.CONFLICT, 409, message);
    }
}
