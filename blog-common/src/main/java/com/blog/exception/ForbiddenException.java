package com.blog.exception;

import com.blog.result.Result;

public class ForbiddenException extends BaseException {

    public ForbiddenException(String message) {
        super(Result.FORBIDDEN, 403, message);
    }
}
