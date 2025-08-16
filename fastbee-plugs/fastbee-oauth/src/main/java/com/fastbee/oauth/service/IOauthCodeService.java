package com.fastbee.oauth.service;

import com.fastbee.oauth.domain.OauthCode;

import java.util.List;

/**
 * 【请填写功能名称】Service接口
 *
 * @author kerwincui
 * @date 2024-03-20
 */
public interface IOauthCodeService
{
    /**
     * 查询【请填写功能名称】
     *
     * @param code 【请填写功能名称】主键
     * @return 【请填写功能名称】
     */
    public OauthCode selectOauthCodeByCode(String code);

    /**
     * 查询【请填写功能名称】列表
     *
     * @param oauthCode 【请填写功能名称】
     * @return 【请填写功能名称】集合
     */
    public List<OauthCode> selectOauthCodeList(OauthCode oauthCode);

    /**
     * 新增【请填写功能名称】
     *
     * @param oauthCode 【请填写功能名称】
     * @return 结果
     */
    public int insertOauthCode(OauthCode oauthCode);

    /**
     * 修改【请填写功能名称】
     *
     * @param oauthCode 【请填写功能名称】
     * @return 结果
     */
    public int updateOauthCode(OauthCode oauthCode);

    /**
     * 批量删除【请填写功能名称】
     *
     * @param codes 需要删除的【请填写功能名称】主键集合
     * @return 结果
     */
    public int deleteOauthCodeByCodes(String[] codes);

    /**
     * 删除【请填写功能名称】信息
     *
     * @param code 【请填写功能名称】主键
     * @return 结果
     */
    public int deleteOauthCodeByCode(String code);

    OauthCode consumeAuthorizationCode(String code);
}
