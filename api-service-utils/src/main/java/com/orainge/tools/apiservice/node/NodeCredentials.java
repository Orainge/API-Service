package com.orainge.tools.apiservice.node;

import lombok.Data;

/**
 * 节点通信-认证凭据类
 *
 * @author orainge
 * @date 2021/1/15
 */
@Data
public class NodeCredentials {
    /**
     * 接收端 ID
     */
    private String id;

    /**
     * 预共享密钥
     */
    private String securityKey;
}
