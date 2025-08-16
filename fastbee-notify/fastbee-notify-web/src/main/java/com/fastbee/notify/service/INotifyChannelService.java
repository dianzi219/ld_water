package com.fastbee.notify.service;

import com.fastbee.common.core.notify.NotifyConfigVO;
import com.fastbee.notify.domain.NotifyChannel;
import com.fastbee.notify.vo.ChannelProviderVO;

import java.util.List;

/**
 * 通知渠道Service接口
 *
 * @author kerwincui
 * @date 2023-12-01
 */
public interface INotifyChannelService
{
    /**
     * 查询通知渠道
     *
     * @param id 通知渠道主键
     * @return 通知渠道
     */
    public NotifyChannel selectNotifyChannelById(Long id);

    /**
     * 查询通知渠道列表
     *
     * @param notifyChannel 通知渠道
     * @return 通知渠道集合
     */
    public List<NotifyChannel> selectNotifyChannelList(NotifyChannel notifyChannel);

    /**
     * 新增通知渠道
     *
     * @param notifyChannel 通知渠道
     * @return 结果
     */
    public int insertNotifyChannel(NotifyChannel notifyChannel);

    /**
     * 修改通知渠道
     *
     * @param notifyChannel 通知渠道
     * @return 结果
     */
    public int updateNotifyChannel(NotifyChannel notifyChannel);

    /**
     * 批量删除通知渠道
     *
     * @param ids 需要删除的通知渠道主键集合
     * @return 结果
     */
    public int deleteNotifyChannelByIds(Long[] ids);

    /**
     * 删除通知渠道信息
     *
     * @param id 通知渠道主键
     * @return 结果
     */
    public int deleteNotifyChannelById(Long id);

    /**
     * 查询通知渠道和服务商
     * @return
     */
    List<ChannelProviderVO> listChannel();

    /**
     * 获取消息通知渠道参数信息
     * @param channelType 渠道类型
     * @param: provider 服务商
     * @return 结果集
     */
    List<NotifyConfigVO> getConfigContent(String channelType, String provider);
}
