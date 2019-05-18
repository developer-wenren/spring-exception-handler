package com.one.learn.exceptionhandler.handler;

import com.one.learn.exceptionhandler.dto.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 通用异常的统一处理类
 *
 * @author One
 * @date 2019/05/18
 */
@ControllerAdvice
public class NormalExceptionHandler {
    @ExceptionHandler()
    public ResponseEntity handleException(Exception e) {
        System.out.println("NormalExceptionHandler handle exception " + e.getClass().getSimpleName());
        return ResponseEntity.ok(new Result<>(400, e.getMessage(), null));
    }
}
