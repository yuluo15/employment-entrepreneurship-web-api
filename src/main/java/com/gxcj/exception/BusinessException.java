package com.gxcj.exception;

import lombok.Getter;

/**
 * 业务异常类
 * 用于业务逻辑中抛出的异常，会被全局异常处理器捕获并返回给前端
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final String message;
    
    public BusinessException(String message) {
        super(message);
        this.message = message;
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }
}
