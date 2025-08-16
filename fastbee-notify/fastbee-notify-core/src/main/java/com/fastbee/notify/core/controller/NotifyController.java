package com.fastbee.notify.core.controller;

import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.notify.core.service.NotifySendService;
import com.fastbee.notify.core.vo.SendParams;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author fastb
 * @version 1.0
 * @description: 通知控制器
 * @date 2023-12-15 11:44
 */
@Api(tags = "通知发送")
@RestController
@RequestMapping("/notify")
public class NotifyController {

    @Resource
    private NotifySendService notifySendService;

    /**
     * @description: 通知测试发送接口
     * @param: sendParams 发送参数
     * @return: com.fastbee.common.core.domain.AjaxResult
     */
    @PreAuthorize("@ss.hasPermi('notify:template:send')")
    @PostMapping("/send")
    @ApiOperation("通知模版测试发送接口")
    public AjaxResult send(@RequestBody SendParams sendParams){
        return notifySendService.send(sendParams);
    }

    /**
     * @description: 短信登录获取验证码
     * @param: phoneNumber 手机号
     * @return: com.fastbee.common.core.domain.AjaxResult
     */
    @GetMapping("/smsLoginCaptcha")
    @ApiOperation("短信登录获取验证码")
    public AjaxResult smsLoginCaptcha(String phoneNumber){
        return notifySendService.smsLoginCaptcha(phoneNumber);
    }

    /**
     * 企业微信验证url有效性
     * @param msgSignature
     * @param: timestamp
     * @param: nonce
     * @param: echostr
     * @param: response
     * @return void
     */
    @ApiOperation("企业微信验证url有效性")
    @GetMapping("/weComVerifyUrl")
    public void weComVerifyUrl(@RequestParam(value = "msg_signature") String msgSignature,
                                     @RequestParam(value = "timestamp") String timestamp,
                                     @RequestParam(value = "nonce") String nonce,
                                     @RequestParam(value = "echostr") String echostr,
                                     HttpServletResponse response) {
        String msg = notifySendService.weComVerifyUrl(msgSignature, timestamp, nonce, echostr);
        try {
            response.getWriter().print(msg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @description: 短信注册获取验证码
     * @param: phoneNumber 手机号
     * @return: com.fastbee.common.core.domain.AjaxResult
     */
    @GetMapping("/smsRegisterCaptcha")
    @ApiOperation("短信登录获取验证码")
    public AjaxResult smsRegisterCaptcha(String phoneNumber){
        return notifySendService.smsRegisterCaptcha(phoneNumber);
    }

}
