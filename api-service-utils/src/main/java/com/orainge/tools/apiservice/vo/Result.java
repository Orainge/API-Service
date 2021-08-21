package com.orainge.tools.apiservice.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

/**
 * 返回结果类
 *
 * @author orainge
 * @date 2021/1/2
 */
@Accessors(chain = true)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result {
    private Integer code;
    private String message;
    private Object data;

    public static Result build() {
        return new Result();
    }

    public static Result build(HttpStatus status) {
        Result result = new Result();
        result.setCode(status.value());
        result.setMessage(status.getReasonPhrase());
        return result;
    }

    public static Result ok() {
        return build(HttpStatus.OK);
    }

    public static Result error() {
        return build(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static Result forbidden() {
        return build(HttpStatus.FORBIDDEN);
    }

    public static Result notFound() {
        return build(HttpStatus.NOT_FOUND);
    }

    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}