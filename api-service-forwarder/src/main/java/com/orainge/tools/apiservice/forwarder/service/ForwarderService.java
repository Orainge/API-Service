package com.orainge.tools.apiservice.forwarder.service;

import com.orainge.tools.apiservice.vo.Result;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletResponse;

/**
 * 接收端请求接口
 *
 * @author orainge
 * @date 2020/12/10
 */
public interface ForwarderService {
    /**
     * 接收来自接收端的转发请求
     */
    Result exchange(HttpServletResponse response, MultiValueMap<String, String> header, String body);
}