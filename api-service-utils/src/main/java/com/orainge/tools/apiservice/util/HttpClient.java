package com.orainge.tools.apiservice.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.util.*;

/**
 * 请求客户端
 *
 * @author orainge
 * @date 2021/1/5
 */
@Slf4j
@Component
@ConditionalOnMissingBean(HttpClient.class)
public class HttpClient {
    @Resource
    @Qualifier("defaultRestTemplate")
    private RestTemplate restTemplate;

    @Resource
    private JSONUtil jsonUtil;

    @Value("${http-client.show-log: false}")
    private boolean showLog;

    /**
     * 执行请求
     */
    public String exchange(String url,
                           Map<String, String> paramsMap,
                           Object body,
                           HttpMethod method,
                           MultiValueMap<String, String> headerMap,
                           boolean isUrlEncode) {
        ResponseEntity<String> response = exchange(url, paramsMap, body, method, headerMap, isUrlEncode, String.class);
        return Objects.isNull(response) ? null : response.getBody();
    }

    /**
     * 执行请求
     **/
    public <T> ResponseEntity<T> exchange(String url,
                                          Map<String, String> paramsMap,
                                          Object body,
                                          HttpMethod method,
                                          MultiValueMap<String, String> headerMap,
                                          boolean isUrlEncode,
                                          Class<T> clazz) {
        if (StringUtils.isEmpty(url)) {
            return null;
        }

        try {
            // 添加 Headers
            HttpHeaders headers = new HttpHeaders();
            headers.addAll(headerMap);

            // 创建请求实体类
            HttpEntity<String> request = Objects.isNull(body) ? new HttpEntity<>(headers) : new HttpEntity<>(jsonUtil.toJSONString(body), headers);

            String exchangeUrl = url;
            String showUrl = url;

            // 创建请求参数
            Map<String, Object> requestParamsMap = new LinkedHashMap<>();
            if (paramsMap != null && !paramsMap.isEmpty()) {
                if (isUrlEncode) {
                    // 使用 UrlEncode 方法创建请求 URL
                    exchangeUrl = UriComponentsBuilder
                            .fromHttpUrl(url)
                            .queryParams(new LinkedMultiValueMap<String, String>() {{
                                paramsMap.forEach(this::add);
                            }}).toUriString();
                    showUrl = exchangeUrl;
                } else {
                    // 不使用 UrlEncode 方法创建请求 URL
                    StringBuilder urlBuilder = new StringBuilder(url + "?");

                    int i = 0;
                    for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
                        i++;
                        String paramKey = UUID.randomUUID().toString().substring(0, 4);
                        urlBuilder.append(entry.getKey());
                        urlBuilder.append("={");
                        urlBuilder.append(paramKey);
                        urlBuilder.append("}");
                        requestParamsMap.put(paramKey, entry.getValue());
                        if (i != paramsMap.size()) {
                            urlBuilder.append("&");
                        }
                    }

                    exchangeUrl = urlBuilder.toString();

                    if (showLog) {
                        showUrl = exchangeUrl;
                        for (Map.Entry<String, Object> entry : requestParamsMap.entrySet()) {
                            showUrl = showUrl.replaceAll("\\{[" + entry.getKey() + "^}]*\\}", entry.getValue().toString());
                        }
                    }
                }

            }

            String requestId = null;

            if (showLog) {
                requestId = UUID.randomUUID().toString().substring(0, 8);
                log.debug("[Http 客户端] - 请求 [{}]: {} {}", requestId, method.toString(), showUrl);
            }

            ResponseEntity<T> responseEntity = restTemplate.exchange(exchangeUrl, method, request, clazz, requestParamsMap);

            if (showLog) {
                log.debug("[Http 客户端] - 请求 [{}] 结果: {}", requestId, responseEntity.getBody());
            }

            return responseEntity;
        } catch (Exception e) {
            if (showLog) {
                log.error("[Http 客户端] - 请求出错 [{} {}], 错误原因: {}", method.toString(), url, e.getMessage());
            }
            return null;
        }
    }
}
