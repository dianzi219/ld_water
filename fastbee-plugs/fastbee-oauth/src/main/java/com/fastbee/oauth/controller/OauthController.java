package com.fastbee.oauth.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.common.core.text.KeyValue;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.MessageUtils;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.common.utils.json.JsonUtils;
import com.fastbee.oauth.domain.OauthAccessToken;
import com.fastbee.oauth.domain.OauthApprovals;
import com.fastbee.oauth.domain.OauthClientDetails;
import com.fastbee.oauth.domain.OauthCode;
import com.fastbee.oauth.enums.OAuth2GrantTypeEnum;
import com.fastbee.oauth.service.IOauthApprovalsService;
import com.fastbee.oauth.service.IOauthClientDetailsService;
import com.fastbee.oauth.service.IOauthCodeService;
import com.fastbee.oauth.service.OauthAccessTokenService;
import com.fastbee.oauth.utils.HttpUtils;
import com.fastbee.oauth.utils.OAuth2Utils;
import com.fastbee.oauth.vo.OAuth2OpenAccessTokenRespVO;
import com.fastbee.oauth.vo.OAuth2OpenAuthorizeInfoRespVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.fastbee.common.core.domain.AjaxResult.success;
import static com.fastbee.common.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static com.fastbee.common.exception.ServiceExceptionUtil.exception0;
import static com.fastbee.common.utils.SecurityUtils.getUserId;
import static com.fastbee.common.utils.collection.CollectionUtils.convertList;

/**
 * @author fastb
 * @version 1.0
 * @description: OAuth2.0 授权接口
 * @date 2024-03-20 11:29
 */
@RestController
@RequestMapping("/oauth2")
@Slf4j
public class OauthController {

    @Resource
    private IOauthClientDetailsService oauthClientDetailsService;
    @Resource
    private IOauthApprovalsService oAuthApproveService;
    @Resource
    private OauthAccessTokenService oauthAccessTokenService;
    @Resource
    private IOauthCodeService oauthCodeService;

    @GetMapping("/authorize")
    public AjaxResult authorize(@RequestParam("clientId") String clientId) {
        // 0. 校验用户已经登录。通过 Spring Security 实现

        // 1. 获得 Client 客户端的信息
        OauthClientDetails oauthClientDetails = oauthClientDetailsService.validOAuthClientFromCache(clientId);
        // 2. 获得用户已经授权的信息
        List<OauthApprovals> approves = oAuthApproveService.getApproveList(getUserId(), clientId);
        // 拼接返回
        return success(this.convert(oauthClientDetails, approves));
    }

    private OAuth2OpenAuthorizeInfoRespVO convert(OauthClientDetails oauthClientDetails, List<OauthApprovals> approves) {
        // 构建 scopes
        List<String> strings = StringUtils.str2List(oauthClientDetails.getScope(), ",", true, true);
        List<KeyValue<String, Boolean>> scopes = new ArrayList<>(strings.size());
        Map<String, OauthApprovals> approveMap = approves.stream().collect(Collectors.toMap(OauthApprovals::getScope, Function.identity()));
        for (String scope : strings) {
            OauthApprovals oauthApprovals = approveMap.get(scope);
            scopes.add(new KeyValue<>(scope, oauthApprovals != null ? "true".equals(oauthApprovals.getStatus()) : false));
        }
        // 拼接返回
        return new OAuth2OpenAuthorizeInfoRespVO(
                new OAuth2OpenAuthorizeInfoRespVO.Client(oauthClientDetails.getClientId(), oauthClientDetails.getIcon()), scopes);
    }

    @PostMapping("/authorize")
    public AjaxResult authorize(@RequestParam("response_type") String responseType,
                                @RequestParam("client_id") String clientId,
                                @RequestParam(value = "scope", required = false) String scope,
                                @RequestParam("redirect_uri") String redirectUri,
                                @RequestParam(value = "auto_approve") Boolean autoApprove,
                                @RequestParam(value = "state", required = false) String state) throws IOException {
        log.warn("oauth2.0认证");
        Map<String, Boolean> scopes = JsonUtils.parseObject(scope, Map.class);
        scopes = ObjectUtil.defaultIfNull(scopes, Collections.emptyMap());
        // 0. 校验用户已经登录。通过 Spring Security 实现

        // 1.1 校验 responseType 是否满足 code 或者 token 值
        OAuth2GrantTypeEnum grantTypeEnum = getGrantTypeEnum(responseType);
        // 1.2 校验 redirectUri 重定向域名是否合法 + 校验 scope 是否在 Client 授权范围内
        OauthClientDetails client = oauthClientDetailsService.validOAuthClientFromCache(clientId, null,
                grantTypeEnum.getGrantType(), scopes.keySet(), redirectUri);

        // 2.1 假设 approved 为 null，说明是场景一
        if (Boolean.TRUE.equals(autoApprove)) {
            // 如果无法自动授权通过，则返回空 url，前端不进行跳转
            if (!oAuthApproveService.checkForPreApproval(getUserId(), clientId, scopes.keySet())) {
                return success(null);
            }
        } else { // 2.2 假设 approved 非 null，说明是场景二
            // 如果计算后不通过，则跳转一个错误链接
            if (!oAuthApproveService.updateAfterApproval(getUserId(), clientId, scopes)) {
                return success(OAuth2Utils.buildUnsuccessfulRedirect(redirectUri, responseType, state,
                        "access_denied", MessageUtils.message("user.access.denied")));
            }
        }

        // 3.1 如果是 code 授权码模式，则发放 code 授权码，并重定向
        List<String> approveScopes = convertList(scopes.entrySet(), Map.Entry::getKey, Map.Entry::getValue);
        if (grantTypeEnum == OAuth2GrantTypeEnum.AUTHORIZATION_CODE) {
            String redirect = getAuthorizationCodeRedirect(getUserId(), client, approveScopes, redirectUri, state);
            return success(MessageUtils.message("authorization.success"), redirect);
        }
        return success();
        // 3.2 如果是 token 则是 implicit 简化模式，则发送 accessToken 访问令牌，并重定向
//        return success(getImplicitGrantRedirect(getLoginUserId(), client, approveScopes, redirectUri, state));
    }

    private String getAuthorizationCodeRedirect(Long userId, OauthClientDetails client,
                                                List<String> scopes, String redirectUri, String state) {
        // 1. 创建 code 授权码
        String authorizationCode = generateCode();
        OauthCode oauthCode = new OauthCode();
        oauthCode.setCode(authorizationCode);
        oauthCode.setUserId(userId);
        oauthCodeService.insertOauthCode(oauthCode);
//        String authorizationCode = oauthCodeService.grantAuthorizationCodeForCode(userId, client.getClientId(), scopes,
//                redirectUri, state);
        // 2. 拼接重定向的 URL
        return OAuth2Utils.buildAuthorizationCodeRedirectUri(redirectUri, authorizationCode, state);
    }

    private static OAuth2GrantTypeEnum getGrantTypeEnum(String responseType) {
        if (StrUtil.equals(responseType, "code")) {
            return OAuth2GrantTypeEnum.AUTHORIZATION_CODE;
        }
        if (StrUtil.equalsAny(responseType, "token")) {
            return OAuth2GrantTypeEnum.IMPLICIT;
        }
        throw exception0(BAD_REQUEST.getCode(), MessageUtils.message("oauth.response.type.not.valid"));
    }

    @PostMapping("/token")
    public ResponseEntity<OAuth2OpenAccessTokenRespVO> postAccessToken(HttpServletRequest request,
                                                                       @RequestParam("grant_type") String grantType,
                                                                       @RequestParam(value = "code", required = false) String code, // 授权码模式
                                                                       @RequestParam(value = "redirect_uri", required = false) String redirectUri, // 授权码模式
                                                                       @RequestParam(value = "state", required = false) String state, // 授权码模式
                                                                       @RequestParam(value = "username", required = false) String username, // 密码模式
                                                                       @RequestParam(value = "password", required = false) String password, // 密码模式
                                                                       @RequestParam(value = "scope", required = false) String scope, // 密码模式
                                                                       @RequestParam(value = "refresh_token", required = false) String refreshToken) { // 刷新模式
//        log.error("小度请求token,入参：{},{},{},{},{},{},{},{}", grantType, code, redirectUri, state, username, password, scope, refreshToken);
        List<String> scopes = OAuth2Utils.buildScopes(scope);
        // todo 小度传过来的参数重复了，这里先暂时处理一下
        if (grantType.contains(",")) {
            grantType = grantType.substring(grantType.indexOf(",") + 1);
        }
        if (code.contains(",")) {
            code = code.substring(code.indexOf(",") + 1);
        }
        if (redirectUri.contains(",")) {
            redirectUri = redirectUri.substring(redirectUri.indexOf(",") + 1);
        }
        // 1.1 校验授权类型
        OAuth2GrantTypeEnum grantTypeEnum = OAuth2GrantTypeEnum.getByGranType(grantType);
        if (grantTypeEnum == null) {
            throw new ServiceException(MessageUtils.message("oauth.grant.type.null") + ":" + grantType + ";" + code + ";" + redirectUri);
        }
        if (grantTypeEnum == OAuth2GrantTypeEnum.IMPLICIT) {
            throw new ServiceException(MessageUtils.message("oauth.grant.type.implicit.not.support"));
        }

        // 1.2 校验客户端
        String[] clientIdAndSecret = obtainBasicAuthorization(request);
        OauthClientDetails client = oauthClientDetailsService.validOAuthClientFromCache(clientIdAndSecret[0], clientIdAndSecret[1],
                grantType, scopes, redirectUri);

        // 2. 根据授权模式，获取访问令牌
        OauthAccessToken oauthAccessToken;
        switch (grantTypeEnum) {
            case AUTHORIZATION_CODE:
                oauthAccessToken = oauthAccessTokenService.grantAuthorizationCodeForAccessToken(client, code, redirectUri, state);
                break;
//            case PASSWORD:
//                accessTokenDO = oauth2GrantService.grantPassword(username, password, client.getClientId(), scopes);
//                break;
//            case CLIENT_CREDENTIALS:
//                accessTokenDO = oauth2GrantService.grantClientCredentials(client.getClientId(), scopes);
//                break;
//            case REFRESH_TOKEN:
//                accessTokenDO = oauth2GrantService.grantRefreshToken(refreshToken, client.getClientId());
//                break;
            default:
                throw new IllegalArgumentException(MessageUtils.message("oauth.grant.type.null")+ "：" + grantType);
        }
        Assert.notNull(oauthAccessToken, MessageUtils.message("oauth.access.token.null")); // 防御性检查
        OAuth2OpenAccessTokenRespVO oAuth2OpenAccessTokenRespVO = this.convertAccessToken(oauthAccessToken);
        ResponseEntity<OAuth2OpenAccessTokenRespVO> response = getResponse(oAuth2OpenAccessTokenRespVO);
//        log.error("小度请求token成功：{}", JSON.toJSONString(response));
        return response;
    }

    private ResponseEntity<OAuth2OpenAccessTokenRespVO> getResponse(OAuth2OpenAccessTokenRespVO accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cache-Control", "no-store");
        headers.set("Pragma", "no-cache");
        headers.set("Content-Type", "application/json;charset=UTF-8");
        return new ResponseEntity<>(accessToken, headers, HttpStatus.OK);
    }

    private OAuth2OpenAccessTokenRespVO convertAccessToken(OauthAccessToken oauthAccessToken) {
        OAuth2OpenAccessTokenRespVO respVO = new OAuth2OpenAccessTokenRespVO();
        respVO.setAccessToken(oauthAccessToken.getTokenId());
        respVO.setRefreshToken(oauthAccessToken.getRefreshToken());
        respVO.setTokenType("bearer");
        respVO.setExpiresIn(OAuth2Utils.getExpiresIn(oauthAccessToken.getExpiresTime()));
//        respVO.setScope(OAuth2Utils.buildScopeStr(bean.getScopes()));
        return respVO;
    }

    private String[] obtainBasicAuthorization(HttpServletRequest request) {
        String[] clientIdAndSecret = HttpUtils.obtainBasicAuthorization(request);
        if (ArrayUtil.isEmpty(clientIdAndSecret) || clientIdAndSecret.length != 2) {
            throw exception0(BAD_REQUEST.getCode(), MessageUtils.message("obtain.basic.authorization.failed"));
        }
        return clientIdAndSecret;
    }

    private static String generateCode() {
        return IdUtil.fastSimpleUUID();
    }


}
