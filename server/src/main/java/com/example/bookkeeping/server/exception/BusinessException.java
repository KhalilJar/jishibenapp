package com.example.bookkeeping.server.exception;

/**
 * 业务异常，统一返回400
 * eg：用户名已存在，记录不存在，无权操作
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
