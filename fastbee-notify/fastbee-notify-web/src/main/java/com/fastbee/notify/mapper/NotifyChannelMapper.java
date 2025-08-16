package com.fastbee.notify.mapper;

import com.fastbee.notify.domain.NotifyChannel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知渠道Mapper接口
 *
 * @author kerwincui
 * @date 2023-12-01
 */
public interface NotifyChannelMapper
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
     * 删除通知渠道
     *
     * @param id 通知渠道主键
     * @return 结果
     */
    public int deleteNotifyChannelById(Long id);

    /**
     * 批量删除通知渠道
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteNotifyChannelByIds(Long[] ids);

    /**
     * 批量查询通知渠道
     * @param idList  主键id集合
     * @return java.util.List<com.fastbee.notify.domain.NotifyChannel>
     */
    List<NotifyChannel> selectNotifyChannelByIds(@Param("idList") List<Long> idList);
}
