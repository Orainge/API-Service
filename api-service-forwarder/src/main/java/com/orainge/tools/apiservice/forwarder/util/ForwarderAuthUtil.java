package com.orainge.tools.apiservice.forwarder.util;

import com.orainge.tools.apiservice.forwarder.config.ForwarderConfig;
import com.orainge.tools.apiservice.node.NodeCode;
import com.orainge.tools.apiservice.node.NodeCredentials;
import com.orainge.tools.apiservice.util.encryption.TOTPUtilImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * 节点加密工具类
 *
 * @author orainge
 * @date 2020/11/8
 */
@Component
public class ForwarderAuthUtil {
    @Resource
    private ForwarderConfig forwarderConfig;

    @Resource
    private TOTPUtilImpl totpUtil;

    /**
     * 获取接收端凭据
     *
     * @param headerMap 请求头 Map
     * @return null: 来自非授权的请求; 非 null: 接收端的凭据
     */
    public NodeCredentials getCredentials(MultiValueMap<String, String> headerMap) {
        String nodeId = headerMap.getFirst(NodeCode.NODE_ID_HEADER_NAME);
        String nodeKey = headerMap.getFirst(NodeCode.NODE_KEY_HEADER_NAME);
        if (StringUtils.isEmpty(nodeId) || StringUtils.isEmpty(nodeKey)) {
            // 没有节点 ID 或 动态验证码
            return null;
        }

        // 从配置文件获取凭据
        NodeCredentials nodeCredentials = forwarderConfig.getCredentials(nodeId);

        // 找不到该请求端 ID 对应的凭据，返回 null
        if (nodeCredentials == null) {
            return null;
        }

        // 校验请求的动态验证码是否符合要求
        String credentialsId = nodeCredentials.getId();
        String securityKey = nodeCredentials.getSecurityKey();
        if (totpUtil.verify(credentialsId, securityKey, nodeKey)) {
            // 动态验证码正确
            return nodeCredentials;
        } else {
            // 动态验证码不正确
            return null;
        }
    }

    /**
     * 获取加密密钥
     *
     * @param credentials 接收端凭据
     */
    public String getEncryptionKey(NodeCredentials credentials) {
        return credentials.getSecurityKey();
    }
}