package com.orainge.tools.apiservice.node;

import com.orainge.tools.apiservice.vo.Result;
import org.springframework.http.HttpStatus;

/**
 * 结果枚举类
 *
 * @author orainge
 * @date 2021/2/10
 */
public enum NodeResultStatus {
    /**
     * 请求成功
     */
    SUCCESS(HttpStatus.OK.value(), "请求成功"),
    /**
     * 转发错误
     */
    ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "转发错误"),
    /**
     * 请求路径不匹配
     */
    URL_NOT_MATCH(HttpStatus.NOT_FOUND.value(), ""),
    /**
     * 未授权的请求方式
     */
    UNAUTHORIZED_REQUEST_METHOD(HttpStatus.FORBIDDEN.value(), "未授权的请求方式"),
    /**
     * 反向代理错误
     */
    REVERSE_PROXY_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "反向代理错误"),
    /**
     * 未授权的节点访问
     */
    NOT_AUTHORIZED_NOTE(HttpStatus.FORBIDDEN.value(), "未授权的访问"),
    /**
     * 请求错误(密文解密失败)
     */
    INCORRECT_REQUEST(HttpStatus.INTERNAL_SERVER_ERROR.value(), "请求错误");

    private final int code;
    private final String message;

    NodeResultStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Result toResult() {
        return Result.build()
                .setCode(code)
                .setMessage(message);
    }

    @Override
    public String toString() {
        return Result.build()
                .setCode(code)
                .setMessage(message)
                .toString();
    }
}