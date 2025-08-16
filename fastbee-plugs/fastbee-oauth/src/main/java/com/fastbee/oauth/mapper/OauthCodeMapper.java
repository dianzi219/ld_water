package com.fastbee.oauth.mapper;

import com.fastbee.oauth.domain.OauthCode;

import java.util.List;

/**
 * 【请填写功能名称】Mapper接口
 *
 * @author kerwincui
 * @date 2024-03-20
 */
public interface OauthCodeMapper
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
     * 删除【请填写功能名称】
     *
     * @param code 【请填写功能名称】主键
     * @return 结果
     */
    public int deleteOauthCodeByCode(String code);

    /**
     * 批量删除【请填写功能名称】
     *
     * @param codes 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteOauthCodeByCodes(String[] codes);
}
