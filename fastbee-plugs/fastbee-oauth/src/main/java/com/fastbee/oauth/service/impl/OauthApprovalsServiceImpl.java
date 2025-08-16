package com.fastbee.oauth.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.common.utils.date.DateUtils;
import com.fastbee.oauth.domain.OauthApprovals;
import com.fastbee.oauth.domain.OauthClientDetails;
import com.fastbee.oauth.mapper.OauthApprovalsMapper;
import com.fastbee.oauth.service.IOauthApprovalsService;
import com.fastbee.oauth.service.IOauthClientDetailsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author kerwincui
 * @date 2024-03-20
 */
@Service
public class OauthApprovalsServiceImpl implements IOauthApprovalsService
{
    /**
     * 批准的过期时间，默认 30 天
     */
    private static final Integer TIMEOUT = 30 * 24 * 60 * 60; // 单位：秒
    @Resource
    private OauthApprovalsMapper oauthApprovalsMapper;
    @Resource
    private IOauthClientDetailsService oauthClientDetailsService;

    /**
     * 查询【请填写功能名称】
     *
     * @param userid 【请填写功能名称】主键
     * @return 【请填写功能名称】
     */
    @Override
    public OauthApprovals selectOauthApprovalsByUserid(String userid)
    {
        return oauthApprovalsMapper.selectOauthApprovalsByUserid(userid);
    }

    /**
     * 查询【请填写功能名称】列表
     *
     * @param oauthApprovals 【请填写功能名称】
     * @return 【请填写功能名称】
     */
    @Override
    public List<OauthApprovals> selectOauthApprovalsList(OauthApprovals oauthApprovals)
    {
        return oauthApprovalsMapper.selectOauthApprovalsList(oauthApprovals);
    }

    /**
     * 新增【请填写功能名称】
     *
     * @param oauthApprovals 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int insertOauthApprovals(OauthApprovals oauthApprovals)
    {
        return oauthApprovalsMapper.insertOauthApprovals(oauthApprovals);
    }

    /**
     * 修改【请填写功能名称】
     *
     * @param oauthApprovals 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int updateOauthApprovals(OauthApprovals oauthApprovals)
    {
        return oauthApprovalsMapper.updateOauthApprovals(oauthApprovals);
    }

    /**
     * 批量删除【请填写功能名称】
     *
     * @param userids 需要删除的【请填写功能名称】主键
     * @return 结果
     */
    @Override
    public int deleteOauthApprovalsByUserids(String[] userids)
    {
        return oauthApprovalsMapper.deleteOauthApprovalsByUserids(userids);
    }

    /**
     * 删除【请填写功能名称】信息
     *
     * @param userid 【请填写功能名称】主键
     * @return 结果
     */
    @Override
    public int deleteOauthApprovalsByUserid(String userid)
    {
        return oauthApprovalsMapper.deleteOauthApprovalsByUserid(userid);
    }

    @Override
    public boolean checkForPreApproval(Long userId, String clientId, Set<String> requestedScopes) {
        OauthClientDetails oauthClientDetails = oauthClientDetailsService.validOAuthClientFromCache(clientId);
        Assert.notNull(oauthClientDetails, "客户端不能为空"); // 防御性编程
        List<String> strings = StringUtils.str2List(oauthClientDetails.getScope(), ",", true, true);
        if (CollUtil.containsAll(strings, requestedScopes)) {
            // gh-877 - if all scopes are auto approved, approvals still need to be added to the approval store.
            LocalDateTime expireTime = LocalDateTime.now().plusSeconds(TIMEOUT);
            for (String scope : requestedScopes) {
                saveApprove(userId, clientId, scope, true, expireTime);
            }
            return true;
        }

        // 第二步，算上用户已经批准的授权。如果 scopes 都包含，则返回 true
        List<OauthApprovals> approvalsList = this.getApproveList(userId, clientId);
        Set<String> scopes = approvalsList.stream().filter(a -> "true".equals(a.getStatus())).map(OauthApprovals::getScope).collect(Collectors.toSet());
        return CollUtil.containsAll(scopes, requestedScopes);
    }

    @Override
    public boolean updateAfterApproval(Long userId, String clientId, Map<String, Boolean> requestedScopes) {
        // 如果 requestedScopes 为空，说明没有要求，则返回 true 通过
        if (CollUtil.isEmpty(requestedScopes)) {
            return true;
        }

        // 更新批准的信息
        boolean success = false; // 需要至少有一个同意
        LocalDateTime expireTime = LocalDateTime.now().plusSeconds(TIMEOUT);
        for (Map.Entry<String, Boolean> entry : requestedScopes.entrySet()) {
            if (entry.getValue()) {
                success = true;
            }
            saveApprove(userId, clientId, entry.getKey(), entry.getValue(), expireTime);
        }
        return success;
    }

    public List<OauthApprovals> getApproveList(Long userId, String clientId) {
        List<OauthApprovals> approvalsList = oauthApprovalsMapper.selectListByUserIdAndClientId(
                userId, clientId);
        approvalsList.removeIf(o -> DateUtils.isExpired(o.getExpiresat()));
        return approvalsList;
    }

    private void saveApprove(Long userId, String clientId, String scope, boolean b, LocalDateTime expireTime) {
        // 先更新
        OauthApprovals oauthApprovals = new OauthApprovals();
        oauthApprovals.setUserid(userId.toString());
        oauthApprovals.setClientid(clientId);
        oauthApprovals.setScope(scope);
        oauthApprovals.setStatus(String.valueOf(b));
        oauthApprovals.setExpiresat(expireTime);
        if (oauthApprovalsMapper.update(oauthApprovals) == 1) {
            return;
        }
        // 失败，则说明不存在，进行更新
        oauthApprovalsMapper.insertOauthApprovals(oauthApprovals);
    }
}
