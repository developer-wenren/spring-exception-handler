package com.one.learn.exceptionhandler.handler;

import com.one.learn.exceptionhandler.dto.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * ArithmeticException 异常处理类，使用 @RestControllerAdvice 实现
 *
 * @author One
 * @date 2019/05/18
 */
@RestControllerAdvice
public class RestExceptionHandler {
    @ExceptionHandler({ArithmeticException.class})
    public Result handlerException(Exception e) {
        return new Result<>(400, e.getMessage(), null);
    }
}
