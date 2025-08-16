package com.fastbee.iot.service;

import java.util.List;
import com.fastbee.iot.domain.EventLog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fastbee.iot.model.HistoryModel;

/**
 * 事件日志Service接口
 *
 * @author kerwincui
 * @date 2024-08-16
 */
public interface IEventLogService extends IService<EventLog>
{

    /**
     * 查询事件日志列表
     *
     * @param eventLog 事件日志
     * @return 事件日志集合
     */
    public List<EventLog> selectEventLogList(EventLog eventLog);

    /**
     * 根据设备编号删除
     * @param serialNumber
     */
    public void deleteEventLogBySerialNumber(String serialNumber);

    /**
     * 查询物模型历史数据
     * @param eventLog 事件日志
     * @return java.util.List<com.fastbee.iot.model.HistoryModel>
     */
    List<HistoryModel> listHistory(EventLog eventLog);

}
