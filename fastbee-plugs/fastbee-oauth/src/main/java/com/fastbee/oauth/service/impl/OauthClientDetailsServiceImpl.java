package com.fastbee.oauth.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.common.core.domain.entity.SysUser;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.oauth.domain.OauthClientDetails;
import com.fastbee.oauth.mapper.OauthClientDetailsMapper;
import com.fastbee.oauth.service.IOauthClientDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static com.fastbee.common.utils.SecurityUtils.getLoginUser;

/**
 * 云云对接Service业务层处理
 *
 * @author kerwincui
 * @date 2022-02-07
 */
@Service
public class OauthClientDetailsServiceImpl implements IOauthClientDetailsService
{
    @Autowired
    private OauthClientDetailsMapper oauthClientDetailsMapper;

    /**
     * 查询云云对接
     *
     * @param id 云云对接主键
     * @return 云云对接
     */
    @Override
    public OauthClientDetails selectOauthClientDetailsById(Long id)
    {
        return oauthClientDetailsMapper.selectOauthClientDetailsById(id);
    }

    /**
     * 查询云云对接列表
     *
     * @param oauthClientDetails 云云对接
     * @return 云云对接
     */
    @Override
    public List<OauthClientDetails> selectOauthClientDetailsList(OauthClientDetails oauthClientDetails)
    {
        // 查询所属机构
        SysUser user = getLoginUser().getUser();
        oauthClientDetails.setTenantId(user.getDept().getDeptUserId());
        return oauthClientDetailsMapper.selectOauthClientDetailsList(oauthClientDetails);
    }

    /**
     * 新增云云对接
     *
     * @param oauthClientDetails 云云对接
     * @return 结果
     */
    @Override
    public AjaxResult insertOauthClientDetails(OauthClientDetails oauthClientDetails)
    {
        SysUser user = getLoginUser().getUser();
        if (null == user.getDept() || null == user.getDept().getDeptUserId()) {
            throw new ServiceException("只允许租户配置");
        }
        oauthClientDetails.setTenantId(user.getDept().getDeptUserId());
        oauthClientDetails.setTenantName(user.getDept().getDeptUserName());
        OauthClientDetails oauthClientDetails1 = oauthClientDetailsMapper.selectOauthClientDetailsByType(oauthClientDetails.getType(), oauthClientDetails.getTenantId());
        if (oauthClientDetails1 != null) {
            return AjaxResult.error("同一个授权平台只能配置一条信息，请勿重复配置");
        }
        OauthClientDetails oauthClientDetails2 = oauthClientDetailsMapper.selectOauthClientDetailsByClientId(oauthClientDetails.getClientId());
        if (oauthClientDetails2 != null) {
            return AjaxResult.error("客户端id：" + oauthClientDetails.getClientId() + "已存在");
        }
        return oauthClientDetailsMapper.insertOauthClientDetails(oauthClientDetails) > 0 ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     * 修改云云对接
     *
     * @param oauthClientDetails 云云对接
     * @return 结果
     */
    @Override
    public AjaxResult updateOauthClientDetails(OauthClientDetails oauthClientDetails)
    {
        OauthClientDetails oauthClientDetails1 = oauthClientDetailsMapper.selectOauthClientDetailsByClientId(oauthClientDetails.getClientId());
        if (oauthClientDetails1 != null && !Objects.equals(oauthClientDetails1.getId(), oauthClientDetails.getId())) {
            return AjaxResult.error("客户端id：" + oauthClientDetails.getClientId() + "已存在");
        }
        return oauthClientDetailsMapper.updateOauthClientDetails(oauthClientDetails) > 0 ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     * 批量删除云云对接
     *
     * @param ids 需要删除的云云对接主键
     * @return 结果
     */
    @Override
    public int deleteOauthClientDetailsByIds(Long[] ids)
    {
        return oauthClientDetailsMapper.deleteOauthClientDetailsByIds(ids);
    }

    /**
     * 删除云云对接信息
     *
     * @param clientId 云云对接主键
     * @return 结果
     */
    @Override
    public int deleteOauthClientDetailsByClientId(String clientId)
    {
        return oauthClientDetailsMapper.deleteOauthClientDetailsByClientId(clientId);
    }

    @Override
    public OauthClientDetails validOAuthClientFromCache(String clientId, String clientSecret, String grantType, Collection<String> scopes, String redirectUri) {
        // 校验客户端存在、且开启
        OauthClientDetails client = this.getOAuth2ClientFromCache(clientId);
        if (client == null) {
            throw new ServiceException("OAuth2 客户端不存在");
        }
        if (0 != client.getStatus()) {
            throw new ServiceException("OAuth2 客户端已禁用");
        }

        // 校验客户端密钥
        if (StrUtil.isNotEmpty(clientSecret) && ObjectUtil.notEqual(client.getClientSecret(), clientSecret)) {
            throw new ServiceException("无效 client_secret");
        }
        // 校验授权方式
//        if (StrUtil.isNotEmpty(grantType) && !StringUtils.contains(client.getAuthorizedGrantTypes(), grantType)) {
//            throw new ServiceException("不支持该授权类型");
//        }
        // 校验授权范围
//        if (CollUtil.isNotEmpty(scopes) && !CollUtil.containsAll(client.getScope(), scopes)) {
//            throw new ServiceException("授权范围过大");
//        }
        // 校验回调地址
        if (StrUtil.isNotEmpty(redirectUri) && !redirectUri.equals(client.getWebServerRedirectUri())) {
            throw new ServiceException("无效 redirect_uri:" + redirectUri);
        }
        return client;
    }

    private OauthClientDetails getOAuth2ClientFromCache(String clientId) {
        return oauthClientDetailsMapper.selectOauthClientDetailsByClientId(clientId);
    }
}
