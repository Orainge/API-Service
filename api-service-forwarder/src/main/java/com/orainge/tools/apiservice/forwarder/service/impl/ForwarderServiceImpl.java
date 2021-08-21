package com.orainge.tools.apiservice.forwarder.service.impl;

import com.orainge.tools.apiservice.forwarder.util.ForwarderClient;
import com.orainge.tools.apiservice.forwarder.config.ForwarderConfig;
import com.orainge.tools.apiservice.node.NodeCredentials;
import com.orainge.tools.apiservice.node.NodeMessage;
import com.orainge.tools.apiservice.node.NodeResultStatus;
import com.orainge.tools.apiservice.util.JSONUtil;
import com.orainge.tools.apiservice.vo.Result;
import com.orainge.tools.apiservice.forwarder.service.ForwarderService;
import com.orainge.tools.apiservice.forwarder.util.ForwarderAuthUtil;
import com.orainge.tools.apiservice.util.encryption.EncryptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * 接收端请求 Controller
 *
 * @author orainge
 * @date 2020/12/10
 */
@Service
@Slf4j
public class ForwarderServiceImpl implements ForwarderService {
    @Resource
    private EncryptionUtil encryptionUtil;

    @Resource
    private ForwarderClient forwarderClient;

    @Resource
    private ForwarderConfig forwarderConfig;

    @Resource
    private JSONUtil jsonUtil;

    @Resource
    private ForwarderAuthUtil nodeAuthenticationUtil;

    @Value("${http-client.show-log}")
    private boolean showLog;

    /**
     * 接收来自接收端的转发请求
     */
    @SuppressWarnings({"unchecked"})
    public Result exchange(HttpServletResponse response, MultiValueMap<String, String> header, String body) {
        if (!forwarderConfig.isEnable()) {
            // 如果该转发端未启用，则返回 404
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        // 判断请求是否为授权的请求
        NodeCredentials credentials = nodeAuthenticationUtil.getCredentials(header);
        if (credentials == null) {
            return NodeResultStatus.NOT_AUTHORIZED_NOTE.toResult();
        }

        // 准备密钥
        String encryptKey = nodeAuthenticationUtil.getEncryptionKey(credentials);

        // 解密请求体
        body = encryptionUtil.decrypt(body, encryptKey);
        if (body == null) {
            return NodeResultStatus.INCORRECT_REQUEST.toResult();
        }

        NodeMessage message;
        try {
            message = jsonUtil.parseObject(body, NodeMessage.class);
            if (message == null) {
                throw new NullPointerException("message");
            }
        } catch (Exception e) {
            log.error("[API 转发端] - 转发请求体不合法", e);
            return NodeResultStatus.INCORRECT_REQUEST.toResult();
        }

        if (showLog) {
            log.info("[API 转发端] - 收到转发请求[{}]: {}", credentials.getId(), jsonUtil.toJSONString(message));
        }

        // 转发请求
        ResponseEntity<byte[]> forwardResult = forwarderClient.forward(message);
        if (forwardResult == null) {
            log.error("[API 转发端] - 请求错误: 转发请求结果为空");
            return NodeResultStatus.REVERSE_PROXY_ERROR.toResult();
        }

        // 将转发结果加密
        String result = encryptionUtil.encrypt(jsonUtil.toJSONString(forwardResult), encryptKey);

        if (result == null) {
            log.error("[API 转发端] - 请求错误: 加密请求结果失败");
            return NodeResultStatus.REVERSE_PROXY_ERROR.toResult();
        } else {
            return NodeResultStatus.SUCCESS.toResult().setData(result);
        }
    }
}