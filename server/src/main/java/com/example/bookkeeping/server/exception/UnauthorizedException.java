package com.example.bookkeeping.server.exception;

/**
 * 未登录/Token无效异常， 统一返回 401
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }

}
