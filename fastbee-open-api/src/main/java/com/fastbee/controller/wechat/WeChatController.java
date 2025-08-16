package com.fastbee.controller.wechat;

import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.MessageUtils;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.common.utils.sign.SignUtils;
import com.fastbee.common.wechat.WeChatLoginBody;
import com.fastbee.common.wechat.WeChatLoginResult;
import com.fastbee.iot.wechat.WeChatService;
import com.fastbee.iot.wechat.vo.WxBindReqVO;
import com.fastbee.iot.wechat.vo.WxCancelBindReqVO;
import com.fastbee.iot.wechat.WeChatCallbackService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static com.fastbee.common.constant.Constants.LANGUAGE;

/**
 * 微信相关控制器
 * @author fastb
 * @date 2023-07-31 11:29
 */
@Api(tags = "微信相关模块")
@Slf4j
@RestController
@RequestMapping("/wechat")
public class WeChatController {

    @Resource
    private WeChatService weChatService;
    @Resource
    private WeChatCallbackService weChatCallbackService;

    /**
     * 移动应用微信登录
     * @param weChatLoginBody 微信登录参数
     * @return 登录结果
     */
    @ApiOperation("移动应用微信登录")
    @PostMapping("/mobileLogin")
    public AjaxResult mobileLogin(HttpServletRequest request, @RequestBody WeChatLoginBody weChatLoginBody) {
        return AjaxResult.success(weChatService.mobileLogin(weChatLoginBody, request.getHeader(LANGUAGE)));
    }

    /**
     * 小程序微信登录
     * @param weChatLoginBody 微信登录参数
     * @return 登录结果
     */
    @ApiOperation("小程序微信登录")
    @PostMapping("/miniLogin")
    public AjaxResult miniLogin(HttpServletRequest request, @RequestBody WeChatLoginBody weChatLoginBody) {
        WeChatLoginResult weChatLoginResult = weChatService.miniLogin(weChatLoginBody, request.getHeader(LANGUAGE));
        return AjaxResult.success(weChatLoginResult);
    }

    /**
     * 小程序、移动应用微信绑定
     * @param wxBindReqVO 微信绑定传参类型
     * @return 结果
     */
    @ApiOperation("小程序、移动应用微信绑定")
    @PostMapping("/bind")
    public AjaxResult bind(@RequestBody WxBindReqVO wxBindReqVO) {
        if (StringUtils.isEmpty(wxBindReqVO.getSourceClient())) {
            throw new ServiceException(MessageUtils.message("wechat.verify.type.null"));
        }
        return weChatService.bind(wxBindReqVO);
    }

    /**
     * 取消所有微信绑定
     * @param wxCancelBindReqVO 微信解绑传参类型
     * @return 结果
     */
    @ApiOperation("取消所有微信绑定")
    @PostMapping("/cancelBind")
    public AjaxResult cancelBind(@RequestBody WxCancelBindReqVO wxCancelBindReqVO) {
        if (wxCancelBindReqVO.getVerifyType() == null) {
            throw new ServiceException(MessageUtils.message("wechat.verify.type.null"));
        }
        return weChatService.cancelBind(wxCancelBindReqVO);
    }

    /**
     * 网站应用获取微信绑定二维码信息
     * @return 二维码信息
     */
    @ApiOperation("网站应用获取微信绑定二维码信息")
    @GetMapping("/getWxBindQr")
    public AjaxResult getWxBindQr(HttpServletRequest httpServletRequest) {
        // 返回二维码信息
        return weChatService.getWxBindQr(httpServletRequest);
    }

    /**
     * 网站应用内微信扫码绑定回调接口
     * @param code 用户凭证
     * @param state 时间戳
     * @param httpServletRequest 请求信息
     * @param httpServletResponse 响应信息
     */
    @ApiOperation("网站应用内微信扫码绑定回调地址")
    @GetMapping("/wxBind/callback")
    public void wxBindCallback(String code, String state, String wxBindId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        //回调接口
        httpServletResponse.sendRedirect(weChatService.wxBindCallback(code, state, wxBindId, httpServletRequest, httpServletResponse));
    }

    /**
     * 网站应用获取微信绑定结果信息
     * @param wxBindMsgId 微信绑定结果信息id
     * @return msg
     */
    @ApiOperation("网站应用获取微信绑定结果信息")
    @GetMapping("/getWxBindMsg")
    public AjaxResult getWxBindMsg(String wxBindMsgId) {
        if (StringUtils.isEmpty(wxBindMsgId)) {
            return AjaxResult.error(MessageUtils.message("wechat.bind.message.id.null"));
        }
        // 返回二维码信息
        return weChatService.getWxBindMsg(wxBindMsgId);
    }

    @ApiOperation("微信公众号验证url")
    @GetMapping("/publicAccount/callback")
    public void checkUrl(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 微信加密签名
        String signature = request.getParameter("signature");
        // 时间戳
        String timestamp = request.getParameter("timestamp");
        // 随机数
        String nonce = request.getParameter("nonce");
        // 随机字符串
        String echostr = request.getParameter("echostr");
        // token
        String token = "fastbee";
        boolean check = SignUtils.checkSignature(token, signature, timestamp, nonce);
        PrintWriter writer = response.getWriter();
        if (check) {
            writer.print(echostr);
        }
        writer.close();
    }

    @ApiOperation("微信公众号回调")
    @PostMapping("/publicAccount/callback")
    public void publicAccountCallback(@RequestBody final String xmlStr, HttpServletResponse response){
        log.info("微信回调接口调用入参：{}", xmlStr);
        final String responseStr = weChatCallbackService.handleCallback(xmlStr);
        log.info("微信回调接口调用返回：入参xmlStr：{}，responseStr:{}", xmlStr, responseStr);
        if (StringUtils.isEmpty(responseStr) || "success".equals(responseStr)) {
            return;
        }
        try {
            response.setHeader("Content-type", "textml;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().write(responseStr);
        } catch (final IOException e) {
            log.error("微信回调回复信息回写失败：xmlStr：{}，responseStr：{}，e：{}", xmlStr, responseStr, e);
        }
    }

}
