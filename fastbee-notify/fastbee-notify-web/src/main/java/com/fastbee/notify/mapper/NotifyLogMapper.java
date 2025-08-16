package com.fastbee.notify.mapper;

import com.fastbee.notify.domain.NotifyLog;

import java.util.List;

/**
 * 通知日志Mapper接口
 * 
 * @author fastbee
 * @date 2023-12-16
 */
public interface NotifyLogMapper 
{
    /**
     * 查询通知日志
     * 
     * @param id 通知日志主键
     * @return 通知日志
     */
    public NotifyLog selectNotifyLogById(Long id);

    /**
     * 查询通知日志列表
     * 
     * @param notifyLog 通知日志
     * @return 通知日志集合
     */
    public List<NotifyLog> selectNotifyLogList(NotifyLog notifyLog);

    /**
     * 新增通知日志
     * 
     * @param notifyLog 通知日志
     * @return 结果
     */
    public int insertNotifyLog(NotifyLog notifyLog);

    /**
     * 修改通知日志
     * 
     * @param notifyLog 通知日志
     * @return 结果
     */
    public int updateNotifyLog(NotifyLog notifyLog);

    /**
     * 删除通知日志
     * 
     * @param id 通知日志主键
     * @return 结果
     */
    public int deleteNotifyLogById(Long id);

    /**
     * 批量删除通知日志
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteNotifyLogByIds(Long[] ids);
}
