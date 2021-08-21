package com.orainge.tools.apiservice.receiver.util;

import com.orainge.tools.apiservice.util.HttpClient;
import com.orainge.tools.apiservice.node.NodeMessage;
import com.orainge.tools.apiservice.node.NodeResultStatus;
import com.orainge.tools.apiservice.util.JSONUtil;
import com.orainge.tools.apiservice.util.encryption.EncryptionUtil;
import com.orainge.tools.apiservice.vo.Result;
import com.orainge.tools.apiservice.receiver.config.ReceiverConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 接收端的客户端
 *
 * @author orainge
 * @date 2021/1/23
 */
@Slf4j
@Component
public class ReceiverClient {
    @Resource
    private EncryptionUtil encryptionUtil;

    @Resource
    private HttpClient httpClient;

    @Resource
    private JSONUtil jsonUtil;

    @Resource
    private ReceiverAuthUtil nodeAuthUtil;

    @Resource
    private ReceiverConfig receiverConfig;

    @Value("${http-client.show-log}")
    private boolean showLog;

    /**
     * 将请求转交给 Forwarder 并获取请求结果
     *
     * @param forwarder 需要提交的转发节点
     * @param message   交换的信息体
     * @return 请求结果
     */
    @SuppressWarnings("all")
    public Map<String, Object> forward(String forwarder, NodeMessage message) {
        try {
            if (showLog) {
                log.info("[API 接收端] - 收到转发请求: {}", jsonUtil.toJSONString(message));
            }

            // 执行加密操作
            String exchangeBody = encryptionUtil.encrypt(
                    jsonUtil.toJSONString(message),
                    nodeAuthUtil.getEncryptionKey()
            );

            if (exchangeBody == null) {
                throw new RuntimeException("加密 [" + message.getRequestURI() + "] 访问数据失败");
            }

            // 创建带凭据的请求头
            MultiValueMap<String, String> headerMap = nodeAuthUtil.getHeaderWithCredentials();

            // 修改客户端 User-Agent
            String userAgent = receiverConfig.getUserAgent();
            if (StringUtils.isEmpty(userAgent)) {
                headerMap.add("User-Agent", receiverConfig.getUserAgent());
            }

            // 连接节点进行转发
            String result = httpClient.exchange(
                    forwarder + "/exchange",
                    null,
                    exchangeBody,
                    HttpMethod.POST,
                    headerMap,
                    true
            );

            // 获取请求结果
            Result requestResult = jsonUtil.parseObject(result, Result.class);

            if (requestResult == null) {
                throw new RuntimeException("请求 [" + message.getRequestURI() + "] 错误: 请求结果为空");
            }

            if (!new Integer(NodeResultStatus.SUCCESS.getCode()).equals(requestResult.getCode())) {
                throw new RuntimeException("请求 [" + message.getRequestURI() + "] 错误: " + requestResult.getMessage());
            }

            // 执行解密操作并返回结果
            try {
                return jsonUtil.parseObject(encryptionUtil.decrypt(requestResult.getData().toString(), nodeAuthUtil.getEncryptionKey()), Map.class);
            } catch (Exception e) {
                throw new RuntimeException("请求 [" + message.getRequestURI() + "] 错误: 解密失败: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("[API 接收端] - 转发错误 [{}]: {}", jsonUtil.toJSONString(forwarder), e.getMessage());
            return null;
        }
    }
}