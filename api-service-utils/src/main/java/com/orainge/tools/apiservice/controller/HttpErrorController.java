package com.orainge.tools.apiservice.controller;

import com.orainge.tools.apiservice.vo.Result;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * 异常处理 Controller
 *
 * @author orainge
 * @date 2021/1/2
 */
@RestController
@ConditionalOnMissingBean(HttpErrorController.class)
@AutoConfigureBefore(ErrorMvcAutoConfiguration.class)
public class HttpErrorController implements ErrorController {
    private final String errorPath = "/error";

    @ResponseBody
    @RequestMapping(path = errorPath)
    public Result error(HttpServletResponse response) {
        int statusCode = response.getStatus();

        // 如果是 404，直接返回空内容
        if (statusCode == HttpStatus.NOT_FOUND.value()) {
            return null;
        }

        return Result.build()
                .setCode(statusCode)
                .setMessage(HttpStatus.valueOf(statusCode).getReasonPhrase());
    }

    @Override
    public String getErrorPath() {
        return errorPath;
    }
}