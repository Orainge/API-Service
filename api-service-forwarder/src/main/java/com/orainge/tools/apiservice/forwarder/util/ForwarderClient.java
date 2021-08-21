package com.orainge.tools.apiservice.forwarder.util;

import com.orainge.tools.apiservice.node.NodeMessage;
import com.orainge.tools.apiservice.node.NodeResultStatus;
import com.orainge.tools.apiservice.util.HttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * 反向代理客户端
 *
 * @author orainge
 * @date 2020/11/8
 */
@Component
@Slf4j
public class ForwarderClient {
    @Resource
    private HttpClient httpClient;

    /**
     * 转发请求
     */
    public ResponseEntity<byte[]> forward(NodeMessage message) {
        String routeUrl = message.getRouteUrl();
        if (StringUtils.isEmpty(routeUrl)) {
            throw new RuntimeException("转发 URL 为空");
        }

        try {
            // 创建 URL
            String processUrl = message.getRequestURI();

            String prefix = message.getPrefix();
            if (!StringUtils.isEmpty(prefix)) {
                processUrl = processUrl.replaceAll(prefix, "");
            }

            if (processUrl.indexOf("/") != 0) {
                processUrl = "/" + processUrl;
            }

            return httpClient.exchange(
                    routeUrl + processUrl,
                    message.getParamsMap(),
                    message.getBody(),
                    HttpMethod.resolve(message.getMethod()),
                    message.getHeaders(),
                    message.isUrlEncode(),
                    byte[].class
            );
        } catch (Exception e) {
            log.error("[API 转发端] - 反向代理客户端请求错误", e);
            return null;
        }
    }
}