package com.one.learn.exceptionhandler.handler;

import com.one.learn.exceptionhandler.dto.Result;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * 自定义异常处理响应对象
 *
 * @author One
 * @date 2019/05/18
 */
@ControllerAdvice
public class CustomWebResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        switch (status) {
            case METHOD_NOT_ALLOWED:
                return getMethodNotAllowedResponse(request);
            default:
                return ResponseEntity.ok(new Result<>(status.value(), status.getReasonPhrase(), null));
        }
    }

    public ResponseEntity getMethodNotAllowedResponse(WebRequest request) {
        String uri = "";
        if (request instanceof ServletWebRequest) {
            uri = ((ServletWebRequest) request).getRequest().getRequestURI();
        }
        Result<Object> result = new Result<>();
        result.setCode(HttpStatus.METHOD_NOT_ALLOWED.value());
        result.setMessage(uri + " 请求方式不正确");
        return ResponseEntity.ok(result);
    }
}
