package com.fastbee.oauth.mapper;

import com.fastbee.oauth.domain.OauthApprovals;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 【请填写功能名称】Mapper接口
 *
 * @author kerwincui
 * @date 2024-03-20
 */
public interface OauthApprovalsMapper
{
    /**
     * 查询【请填写功能名称】
     *
     * @param userid 【请填写功能名称】主键
     * @return 【请填写功能名称】
     */
    public OauthApprovals selectOauthApprovalsByUserid(String userid);

    /**
     * 查询【请填写功能名称】列表
     *
     * @param oauthApprovals 【请填写功能名称】
     * @return 【请填写功能名称】集合
     */
    public List<OauthApprovals> selectOauthApprovalsList(OauthApprovals oauthApprovals);

    /**
     * 新增【请填写功能名称】
     *
     * @param oauthApprovals 【请填写功能名称】
     * @return 结果
     */
    public int insertOauthApprovals(OauthApprovals oauthApprovals);

    /**
     * 修改【请填写功能名称】
     *
     * @param oauthApprovals 【请填写功能名称】
     * @return 结果
     */
    public int updateOauthApprovals(OauthApprovals oauthApprovals);

    /**
     * 删除【请填写功能名称】
     *
     * @param userid 【请填写功能名称】主键
     * @return 结果
     */
    public int deleteOauthApprovalsByUserid(String userid);

    /**
     * 批量删除【请填写功能名称】
     *
     * @param userids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteOauthApprovalsByUserids(String[] userids);

    int update(OauthApprovals oauthApprovals);

    List<OauthApprovals> selectListByUserIdAndClientId(@Param("userId") Long userId, @Param("clientId") String clientId);
}
