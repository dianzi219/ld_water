package com.fastbee.notify.service.impl;

import com.fastbee.common.core.domain.entity.SysRole;
import com.fastbee.common.core.domain.entity.SysUser;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.notify.domain.NotifyChannel;
import com.fastbee.notify.domain.NotifyLog;
import com.fastbee.notify.domain.NotifyTemplate;
import com.fastbee.notify.mapper.NotifyChannelMapper;
import com.fastbee.notify.mapper.NotifyLogMapper;
import com.fastbee.notify.mapper.NotifyTemplateMapper;
import com.fastbee.notify.service.INotifyLogService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.fastbee.common.utils.SecurityUtils.getLoginUser;

/**
 * 通知日志Service业务层处理
 *
 * @author fastbee
 * @date 2023-12-16
 */
@Service
public class NotifyLogServiceImpl implements INotifyLogService
{
    @Resource
    private NotifyLogMapper notifyLogMapper;
    @Resource
    private NotifyChannelMapper notifyChannelMapper;
    @Resource
    private NotifyTemplateMapper notifyTemplateMapper;

    /**
     * 查询通知日志
     *
     * @param id 通知日志主键
     * @return 通知日志
     */
    @Override
    public NotifyLog selectNotifyLogById(Long id)
    {
        return notifyLogMapper.selectNotifyLogById(id);
    }

    /**
     * 查询通知日志列表
     *
     * @param notifyLog 通知日志
     * @return 通知日志
     */
    @Override
    public List<NotifyLog> selectNotifyLogList(NotifyLog notifyLog)
    {
        SysUser user = getLoginUser().getUser();
//        List<SysRole> roles=user.getRoles();
//        // 租户
//        if(roles.stream().anyMatch(a->a.getRoleKey().equals("tenant"))){
//            notifyLog.setTenantId(user.getUserId());
//        }
        // 查询所属机构
        if (null != user.getDeptId()) {
            notifyLog.setTenantId(user.getDept().getDeptUserId());
        } else {
            notifyLog.setTenantId(user.getUserId());
        }
        List<NotifyLog> notifyLogs = notifyLogMapper.selectNotifyLogList(notifyLog);
        if (CollectionUtils.isEmpty(notifyLogs)) {
            return notifyLogs;
        }
        List<Long> channelIdList = notifyLogs.stream().map(NotifyLog::getChannelId).collect(Collectors.toList());
        List<NotifyChannel> notifyChannelList = notifyChannelMapper.selectNotifyChannelByIds(channelIdList);
        Map<Long, NotifyChannel> notifyChannelMap = notifyChannelList.stream().collect(Collectors.toMap(NotifyChannel::getId, Function.identity()));
        List<Long> templateIdList = notifyLogs.stream().map(NotifyLog::getNotifyTemplateId).collect(Collectors.toList());
        List<NotifyTemplate> notifyTemplateList = notifyTemplateMapper.selectNotifyTemplateByIds(templateIdList);
        Map<Long, NotifyTemplate> notifyTemplateMap = notifyTemplateList.stream().collect(Collectors.toMap(NotifyTemplate::getId, Function.identity()));
        for (NotifyLog log : notifyLogs) {
            if (notifyChannelMap.containsKey(log.getChannelId())) {
                log.setChannelName(notifyChannelMap.get(log.getChannelId()).getName());
            }
            if (notifyTemplateMap.containsKey(log.getNotifyTemplateId())) {
                log.setTemplateName(notifyTemplateMap.get(log.getNotifyTemplateId()).getName());
            }
        }
        return notifyLogs;
    }

    /**
     * 新增通知日志
     *
     * @param notifyLog 通知日志
     * @return 结果
     */
    @Override
    public int insertNotifyLog(NotifyLog notifyLog)
    {
        notifyLog.setCreateTime(DateUtils.getNowDate());
        return notifyLogMapper.insertNotifyLog(notifyLog);
    }

    /**
     * 修改通知日志
     *
     * @param notifyLog 通知日志
     * @return 结果
     */
    @Override
    public int updateNotifyLog(NotifyLog notifyLog)
    {
        notifyLog.setUpdateTime(DateUtils.getNowDate());
        return notifyLogMapper.updateNotifyLog(notifyLog);
    }

    /**
     * 批量删除通知日志
     *
     * @param ids 需要删除的通知日志主键
     * @return 结果
     */
    @Override
    public int deleteNotifyLogByIds(Long[] ids)
    {
        return notifyLogMapper.deleteNotifyLogByIds(ids);
    }

    /**
     * 删除通知日志信息
     *
     * @param id 通知日志主键
     * @return 结果
     */
    @Override
    public int deleteNotifyLogById(Long id)
    {
        return notifyLogMapper.deleteNotifyLogById(id);
    }
}
