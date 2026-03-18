package com.blog.exception;

import com.blog.result.Result;

public class UnauthorizedException extends BaseException {

    public UnauthorizedException(String message) {
        super(Result.UNAUTHORIZED, 401, message);
    }
}
