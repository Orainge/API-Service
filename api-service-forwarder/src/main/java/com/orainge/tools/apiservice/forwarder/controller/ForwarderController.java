package com.orainge.tools.apiservice.forwarder.controller;

import com.orainge.tools.apiservice.vo.Result;
import com.orainge.tools.apiservice.forwarder.service.ForwarderService;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * 接收端请求 Controller
 *
 * @author orainge
 * @date 2020/12/10
 */
@RestController
public class ForwarderController {
    @Resource
    private ForwarderService forwarderService;

    /**
     * 接收来自接收端的转发请求
     */
    @PostMapping("/exchange")
    public @ResponseBody
    Result exchange(HttpServletResponse response,
                    @RequestHeader(required = false) MultiValueMap<String, String> header,
                    @RequestBody(required = false) String body) {
        if (StringUtils.isEmpty(body)) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }
        return forwarderService.exchange(response, header, body);
    }

    /**
     * 只允许 POST 方式进行数据交换，其它方式统一返回成 404
     */
    @RequestMapping("/exchange")
    public void exchange(HttpServletResponse response) {
        response.setStatus(HttpStatus.NOT_FOUND.value());
    }
}
