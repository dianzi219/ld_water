package com.fastbee.iot.mapper;

import java.util.List;
import com.fastbee.framework.mybatis.mapper.BaseMapperX;
import com.fastbee.iot.domain.EventLog;
import com.fastbee.iot.model.HistoryModel;

/**
 * 事件日志Mapper接口
 *
 * @author kerwincui
 * @date 2024-08-16
 */
public interface EventLogMapper extends BaseMapperX<EventLog>
{

    /**
     * 查询物模型历史数据
     * @param eventLog 事件日志
     * @return java.util.List<com.fastbee.iot.model.HistoryModel>
     */
    List<HistoryModel> listHistory(EventLog eventLog);

    Long selectEventLogCount(Long userId);

}
