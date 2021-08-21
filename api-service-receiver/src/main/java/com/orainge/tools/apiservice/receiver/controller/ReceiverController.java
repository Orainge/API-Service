package com.orainge.tools.apiservice.receiver.controller;

import com.orainge.tools.apiservice.receiver.service.ReceiverService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 接受请求 Controller
 *
 * @author orainge
 * @date 2020/12/10
 */
@Controller
public class ReceiverController {
    @Resource
    private ReceiverService receiverService;

    @RequestMapping("/**")
    public void forward(HttpServletRequest request,
                        HttpServletResponse response,
                        @RequestParam(required = false) Map<String, String> requestParam) {
        receiverService.forward(request, response, requestParam);
    }
}
