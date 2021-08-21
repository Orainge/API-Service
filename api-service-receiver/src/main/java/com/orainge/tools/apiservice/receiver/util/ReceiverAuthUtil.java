package com.orainge.tools.apiservice.receiver.util;

import com.orainge.tools.apiservice.node.NodeCode;
import com.orainge.tools.apiservice.receiver.config.ReceiverConfig;
import com.orainge.tools.apiservice.util.encryption.TOTPUtilImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.annotation.Resource;

/**
 * 节点加密工具类
 *
 * @author orainge
 * @date 2020/11/8
 */
@Component
public class ReceiverAuthUtil {
    @Resource
    private ReceiverConfig receiverConfig;

    @Resource
    private TOTPUtilImpl totpUtil;

    /**
     * 创建带凭据的请求头
     */
    public MultiValueMap<String, String> getHeaderWithCredentials() {
        MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
        String receiverId = receiverConfig.getCredentials().getId();
        headerMap.add(NodeCode.NODE_ID_HEADER_NAME, receiverId);
        headerMap.add(NodeCode.NODE_KEY_HEADER_NAME,
                totpUtil.generate(receiverId, receiverConfig.getCredentials().getSecurityKey())
        );
        return headerMap;
    }

    /**
     * 获取加密密钥
     */
    public String getEncryptionKey() {
        return receiverConfig.getCredentials().getSecurityKey();
    }
}