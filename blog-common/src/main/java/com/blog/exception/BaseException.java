package com.blog.exception;

import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {

    private final Integer code;
    private final Integer httpStatus;

    protected BaseException(Integer code, Integer httpStatus, String message) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }
}
