package com.orainge.tools.apiservice.receiver.config;

import com.orainge.tools.apiservice.node.NodeCredentials;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 接收端配置类
 *
 * @author orainge
 * @date 2020/10/17
 */
@Component
@ConfigurationProperties(prefix = "api.receiver")
@Data
@Slf4j
public class ReceiverConfig {
    private boolean enable = false;
    private NodeCredentials credentials = null;
    private String userAgent = null;
    private List<ReceiverApiList> api = null;

    private final Map<String, Map<String, Object>> receiverApiMap = new HashMap<>();

    private final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

    @PostConstruct
    public void init() {
        if (!enable) {
            log.warn("[API 接收端] - 接收端已禁用，将不会接受任何转发请求");
            return;
        }

        log.info("[API 接收端] - 正在检查 URL 配置是否冲突");

        // 对配置的转发 URL 进行冲突检测
        for (ReceiverConfig.ReceiverApiList apiListItem : api) {
            for (ReceiverConfig.ReceiverApi apiItem : apiListItem.getUrls()) {
                String url = apiItem.getUrl();

                // 对已有的 URL 进行检测
                for (Map.Entry<String, Map<String, Object>> entry : receiverApiMap.entrySet()) {
                    String existsUrl = entry.getKey();

                    // 比较现在配置的 URL 和已配置的 URL 是否冲突
                    if (ANT_PATH_MATCHER.match(url, existsUrl) || ANT_PATH_MATCHER.match(existsUrl, url)) {
                        // URL 冲突
                        RuntimeException exception = new RuntimeException("转发组[" + apiListItem.getName() + "] 的 URL[" + url +
                                "] 与转发组[" + ((ReceiverApiList) entry.getValue().get("receiverApiList")).getName() + "] 的 URL[" + existsUrl +
                                "] 冲突，请检查配置文件");
                        log.error("[API 转发组] - URL 冲突", exception);
                        throw exception;
                    }
                }

                // 没有冲突
                // 将请求方式转换为大写（Request.getMethod()默认为大写）
                apiItem.setMethod(apiItem.getMethod().stream().map(String::toUpperCase).collect(Collectors.toList()));

                // 添加 Map
                Map<String, Object> item = new HashMap<>();
                item.put("receiverApiList", apiListItem);
                item.put("receiverApi", apiItem);
                receiverApiMap.put(url, item);
            }
        }

        log.info("[API 接收端] - URL 配置无冲突");
        log.info("[API 接收端] - 接收端已启用");
    }

    public Map<String, Object> getReceiverApi(String requestUrl) {
        for (Map.Entry<String, Map<String, Object>> entry : receiverApiMap.entrySet()) {
            if (ANT_PATH_MATCHER.match(entry.getKey(), requestUrl)) {
//                String a = ANT_PATH_MATCHER.combine(entry.getKey(), requestUrl);
                // 有匹配的 URL，返回结果
                return entry.getValue();
            }
        }

        // 没有匹配结果，返回 null
        return null;
    }

    @Data
    public static class ReceiverApiList {
        private String name;
        private String forwarder;
        private List<ReceiverApi> urls;
    }

    @Data
    public static class ReceiverApi {
        private String url;
        private String prefix = "";
        private String host;
        private boolean urlEncode = true;
        private List<String> method;
    }
}