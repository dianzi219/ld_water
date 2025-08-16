package com.fastbee.oauth.service;

import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.oauth.domain.OauthClientDetails;

import java.util.Collection;
import java.util.List;

/**
 * 云云对接Service接口
 *
 * @author kerwincui
 * @date 2022-02-07
 */
public interface IOauthClientDetailsService
{
    /**
     * 查询云云对接
     *
     * @param id 云云对接主键
     * @return 云云对接
     */
    public OauthClientDetails selectOauthClientDetailsById(Long id);

    /**
     * 查询云云对接列表
     *
     * @param oauthClientDetails 云云对接
     * @return 云云对接集合
     */
    public List<OauthClientDetails> selectOauthClientDetailsList(OauthClientDetails oauthClientDetails);

    /**
     * 新增云云对接
     *
     * @param oauthClientDetails 云云对接
     * @return 结果
     */
    public AjaxResult insertOauthClientDetails(OauthClientDetails oauthClientDetails);

    /**
     * 修改云云对接
     *
     * @param oauthClientDetails 云云对接
     * @return 结果
     */
    public AjaxResult updateOauthClientDetails(OauthClientDetails oauthClientDetails);

    /**
     * 批量删除云云对接
     *
     * @param ids 需要删除的云云对接主键集合
     * @return 结果
     */
    public int deleteOauthClientDetailsByIds(Long[] ids);

    /**
     * 删除云云对接信息
     *
     * @param clientId 云云对接主键
     * @return 结果
     */
    public int deleteOauthClientDetailsByClientId(String clientId);

    default OauthClientDetails validOAuthClientFromCache(String clientId) {
        return validOAuthClientFromCache(clientId, null, null, null, null);
    }

    OauthClientDetails validOAuthClientFromCache(String clientId, String clientSecret, String grantType, Collection<String> strings, String redirectUri);
}
