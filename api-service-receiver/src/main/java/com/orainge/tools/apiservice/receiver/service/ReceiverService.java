package com.orainge.tools.apiservice.receiver.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 接受请求接口
 *
 * @author orainge
 * @date 2020/12/10
 */
public interface ReceiverService {
    void forward(HttpServletRequest request, HttpServletResponse response, Map<String, String> requestParam);
}