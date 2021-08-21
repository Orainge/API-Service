package com.orainge.tools.apiservice.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * JSON 工具类
 *
 * @author orainge
 * @date 2021/4/21
 */
@Component
@ConditionalOnMissingBean(JSONUtil.class)
public class JSONUtil {
    @Resource
    private ObjectMapper objectMapperBean;

    private static ObjectMapper objectMapper = null;

    @PostConstruct
    public void init() {
        objectMapper = objectMapperBean;
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
    }

    public String toJSONString(Object obj) {
        try {
            return obj == null ? null : objectMapper.writeValueAsString(obj);
        } catch (Exception ignore) {
            return null;
        }
    }

    public <T> T parseObject(String text, Class<T> clazz) {
        try {
            return StringUtils.isEmpty(text) || clazz == null ? null : objectMapper.readValue(text, clazz);
        } catch (Exception ignore) {
            return null;
        }
    }
}
