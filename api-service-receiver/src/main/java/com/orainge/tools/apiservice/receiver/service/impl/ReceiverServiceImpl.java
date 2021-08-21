package com.orainge.tools.apiservice.receiver.service.impl;

import com.orainge.tools.apiservice.node.NodeMessage;
import com.orainge.tools.apiservice.node.NodeResultStatus;
import com.orainge.tools.apiservice.receiver.util.ReceiverClient;
import com.orainge.tools.apiservice.receiver.service.ReceiverService;
import com.orainge.tools.apiservice.receiver.config.ReceiverConfig;
import com.orainge.tools.apiservice.util.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 接受请求服务类
 *
 * @author orainge
 * @date 2020/12/10
 */
@Slf4j
@Service
public class ReceiverServiceImpl implements ReceiverService {
    @Resource
    private JSONUtil jsonUtil;

    @Resource
    private ReceiverClient receiverClient;

    @Resource
    private ReceiverConfig receiverConfig;

    @SuppressWarnings("all")
    public void forward(HttpServletRequest request, HttpServletResponse response, Map<String, String> paramsMap) {
        if (!receiverConfig.isEnable()) {
            // 如果该转发节点未启用，返回 404
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }

        String url = request.getRequestURI(); // 请求路径
        String method = request.getMethod(); // 请求方式

        try {
            Map<String, Object> apiItem = receiverConfig.getReceiverApi(url);
            if (apiItem == null) {
                // 没有匹配的 URL，返回错误
                setErrorResponse(response, NodeResultStatus.URL_NOT_MATCH);
                return;
            }

            // 检查请求方式是否被许可
            boolean isMethodPermitted = false;
            for (String methodItem : ((ReceiverConfig.ReceiverApi) apiItem.get("receiverApi")).getMethod()) {
                if (method.equals(methodItem)) {
                    // 该请求方式在配置文件中
                    isMethodPermitted = true;
                    break;
                }
            }
            if (!isMethodPermitted) {
                setErrorResponse(response, NodeResultStatus.UNAUTHORIZED_REQUEST_METHOD);
                return;
            }

            ReceiverConfig.ReceiverApi api = (ReceiverConfig.ReceiverApi) apiItem.get("receiverApi");
            ReceiverConfig.ReceiverApiList apiList = (ReceiverConfig.ReceiverApiList) apiItem.get("receiverApiList");

            // 转换 body
            byte[] body;
            try {
                body = StreamUtils.copyToByteArray(request.getInputStream());
            } catch (Exception e) {
                log.error("[API 接收端] - 请求体转换错误 [{}]: {}", url, e.getMessage());
                setErrorResponse(response, NodeResultStatus.ERROR);
                return;
            }

            // 封装 headers
            LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            for (String name : Collections.list(request.getHeaderNames())) {
                for (String value : Collections.list(request.getHeaders(name))) {
                    headers.add(name, value);
                }
            }

            // 转发请求
            Map<String, Object> resultMap = receiverClient.forward(
                    apiList.getForwarder(),
                    new NodeMessage()
                            .setParamsMap(paramsMap)
                            .setRequestURI(url)
                            .setHeaders(headers)
                            .setBody(body)
                            .setMethod(method)
                            .setRouteUrl(api.getHost())
                            .setPrefix(api.getPrefix())
                            .setUrlEncode(api.isUrlEncode())
            );

            if (resultMap == null) {
                // 如果请求结果为空，则返回错误信息
                // 日志已经在上一层输出了
                setErrorResponse(response, NodeResultStatus.ERROR);
            } else {
                try {
                    // 如果请求结果正常，则设置请求信息
                    response.setStatus((Integer) resultMap.get("statusCodeValue"));

                    // 设置 header
                    ((LinkedHashMap<String, List<String>>) resultMap.get("headers")).forEach((header, headerValues) -> {
                        headerValues.forEach(headerValue -> {
                            response.setHeader(header, headerValue);
                        });
                    });

                    // 这里的 body 是 byte[] 经过 Base64 编码得到的，因此需要解码
                    String bodyBase64 = (String) resultMap.get("body");
                    if (!StringUtils.isEmpty(bodyBase64)) {
                        writeBody(response, Base64.getDecoder().decode(bodyBase64));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("[API 接收端] - 解密请求结果错误 [" + url + "]", e);
                    setErrorResponse(response, NodeResultStatus.ERROR);
                }
            }
        } catch (Exception e) {
            log.error("[API 接收端] - 请求错误 [" + url + "]", e);
            setErrorResponse(response, NodeResultStatus.ERROR);
        }
    }

    private void setErrorResponse(HttpServletResponse response, NodeResultStatus nodeResultStatus) {
        response.setHeader("Content-Type", "application/json;charset=utf-8");
        response.setStatus(nodeResultStatus.getCode());
        writeBody(response, jsonUtil.toJSONString(nodeResultStatus.toResult()).getBytes(StandardCharsets.UTF_8));
    }

    private void writeBody(HttpServletResponse response, byte[] body) {
        try {
            OutputStream stream = response.getOutputStream();
            stream.write(body);
            stream.close();
        } catch (IOException ignored) {
        }
    }
}