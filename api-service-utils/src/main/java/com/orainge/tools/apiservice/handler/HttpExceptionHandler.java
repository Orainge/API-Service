package com.orainge.tools.apiservice.handler;

import com.orainge.tools.apiservice.vo.Result;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局统一异常处理
 *
 * @author orainge
 * @date 2021/1/21
 */
@RestControllerAdvice
@ConditionalOnMissingBean(HttpExceptionHandler.class)
public class HttpExceptionHandler {
    @ExceptionHandler(Exception.class)
    public Result defaultException(Exception e) {
        return Result.error().setMessage(e.getMessage());
    }
}
