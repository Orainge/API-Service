package com.orainge.tools.apiservice.forwarder.config;

import com.orainge.tools.apiservice.config.RestTemplateConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.*;

/**
 * 转发端自定义的 RestTemplate 客户端
 *
 * @author orainge
 * @date 2020/10/17
 */
@Configuration
public class ForwarderRestTemplateClientConfig extends RestTemplateConfig {
    /**
     * 重设 RestTemplate，请求结果为 4xx 5xx 时不抛出异常
     */
    @Bean("defaultRestTemplate")
    public RestTemplate buildRestTemplate() {
        RestTemplate restTemplate = super.buildRestTemplate();

        // 设置错误处理器为空
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) {
            }
        });

        return restTemplate;
    }
}
