package com.orainge.tools.apiservice.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * RestTemplate 配置文件，兼容 http 和 https
 *
 * @author orainge
 * @date 2021/1/10
 */
@Configuration
@ConditionalOnMissingBean({RestTemplateConfig.class})
@Data
public class RestTemplateConfig {
    @Value("${http-client.connect-timeout: 20}")
    private Integer connectTimeout;

    @Value("${http-client.read-timeout: 120}")
    private Integer readTimeout;

    /**
     * 默认的 RestTemplate Bean 构造方法
     */
    @Bean("defaultRestTemplate")
    @ConditionalOnMissingBean({RestTemplate.class})
    public RestTemplate buildRestTemplate() {
        return buildRestTemplate(null, null);
    }

    public RestTemplate buildRestTemplate(RestTemplate restTemplate, ClientHttpRequestFactory factory) {
        if (factory == null) {
            // 使用默认工厂类
            SimpleClientHttpRequestFactory defaultFactory = skipSSLVerifyFactory();
            defaultFactory.setConnectTimeout(1000 * connectTimeout);
            defaultFactory.setReadTimeout(1000 * readTimeout);
            factory = defaultFactory;
        }

        // 创建一个自定义 SSL 验证的工厂，跳过 SSL 验证
        if (restTemplate == null) {
            restTemplate = new RestTemplate();
        }

        restTemplate.setRequestFactory(factory);
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8)); // 支持中文编码
        return restTemplate;
    }

    /**
     * 跳过 SSL 验证的工厂
     */
    public SimpleClientHttpRequestFactory skipSSLVerifyFactory() {
        return new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod)
                    throws IOException {
                if (connection instanceof HttpsURLConnection) {
                    ((HttpsURLConnection) connection).setHostnameVerifier((s, sslSession) -> true);
                    try {
                        SSLContext context = SSLContext.getInstance("TLS");
                        context.init(null, new TrustManager[]{new X509TrustManager() {
                                    @Override
                                    public X509Certificate[] getAcceptedIssuers() {
                                        return new X509Certificate[0];
                                    }

                                    @Override
                                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                                    }

                                    @Override
                                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                                    }
                                }},
                                new SecureRandom());
                        ((HttpsURLConnection) connection).setSSLSocketFactory(context.getSocketFactory());
                    } catch (Exception ignore) {
                    }
                }
                super.prepareConnection(connection, httpMethod);
            }
        };
    }

    /**
     * 带 SSL 验证的工厂
     */
    public HttpComponentsClientHttpRequestFactory defaultFactory() {
        return new HttpComponentsClientHttpRequestFactory();
    }
}