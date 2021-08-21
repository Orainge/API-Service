package com.orainge.tools.apiservice.node;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

/**
 * 节点通信-消息类
 *
 * @author orainge
 * @date 2021/1/23
 */
@Data
@Accessors(chain = true)
public class NodeMessage {
    /**
     * 请求地址 URI (跟在域名后面的部分)
     */
    private String requestURI;
    /**
     * 请求头
     */
    private LinkedMultiValueMap<String, String> headers;
    /**
     * 请求参数
     */
    private Map<String, String> paramsMap;
    /**
     * 请求体
     */
    private byte[] body;
    /**
     * 请求方式
     */
    private String method;
    /**
     * 转发目的地址
     */
    private String routeUrl;
    /**
     * 转发地址过滤前缀
     */
    private String prefix;
    /**
     * 是否需要进行 urlEncode 编码
     */
    private boolean urlEncode = true;
}
