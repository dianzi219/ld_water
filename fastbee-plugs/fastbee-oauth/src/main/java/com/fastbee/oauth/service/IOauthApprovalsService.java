package com.fastbee.oauth.service;

import com.fastbee.oauth.domain.OauthApprovals;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 【请填写功能名称】Service接口
 *
 * @author kerwincui
 * @date 2024-03-20
 */
public interface IOauthApprovalsService
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
     * 批量删除【请填写功能名称】
     *
     * @param userids 需要删除的【请填写功能名称】主键集合
     * @return 结果
     */
    public int deleteOauthApprovalsByUserids(String[] userids);

    /**
     * 删除【请填写功能名称】信息
     *
     * @param userid 【请填写功能名称】主键
     * @return 结果
     */
    public int deleteOauthApprovalsByUserid(String userid);

    boolean checkForPreApproval(Long userId, String clientId, Set<String> requestedScopes);

    boolean updateAfterApproval(Long userId, String clientId, Map<String, Boolean> scopes);

    List<OauthApprovals> getApproveList(Long userId, String clientId);
}
