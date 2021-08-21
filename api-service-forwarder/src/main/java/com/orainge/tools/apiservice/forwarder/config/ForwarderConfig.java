package com.orainge.tools.apiservice.forwarder.config;

import com.orainge.tools.apiservice.node.NodeCredentials;
import com.orainge.tools.apiservice.util.JSONUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;

/**
 * 转发节点的配置文件
 *
 * @author orainge
 * @date 2020/10/17
 */
@Component
@ConfigurationProperties(prefix = "api.forwarder")
@Data
@Slf4j
public class ForwarderConfig {
    @Resource
    private JSONUtil jsonUtil;

    private boolean enable = false;
    private List<NodeCredentials> receiver = new LinkedList<>();

    private static final Map<String, NodeCredentials> credentialsMap = new HashMap<>();

    @PostConstruct
    public void init() {
        if (!enable) {
            log.warn("[API 转发端] - 转发端已禁用，将不会接受任何转发请求");
            return;
        }

        // 初始化 Map
        for (NodeCredentials item : receiver) {
            String id = item.getId();
            if (credentialsMap.containsKey(id)) {
                throw new RuntimeException("[API 转发端] - 接收端 ID 重复 [" + id + "], 请检查配置文件");
            }
            credentialsMap.put(id, item);
        }

        log.info("[API 转发端] - 允许的 ID 列表: {}", jsonUtil.toJSONString(receiver));
        log.info("[API 转发端] - 转发端已启用");
    }

    public NodeCredentials getCredentials(String id) {
        return credentialsMap.get(id);
    }
}