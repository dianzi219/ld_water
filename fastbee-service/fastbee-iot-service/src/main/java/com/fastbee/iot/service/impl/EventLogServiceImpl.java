package com.fastbee.iot.service.impl;

import java.util.List;
import java.util.Objects;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.framework.mybatis.LambdaQueryWrapperX;
import com.fastbee.iot.model.HistoryModel;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import com.fastbee.iot.mapper.EventLogMapper;
import com.fastbee.iot.domain.EventLog;
import com.fastbee.iot.service.IEventLogService;

/**
 * 事件日志Service业务层处理
 *
 * @author kerwincui
 * @date 2024-08-16
 */
@Service
public class EventLogServiceImpl extends ServiceImpl<EventLogMapper,EventLog> implements IEventLogService {

    @Resource
    private EventLogMapper eventLogMapper;


    /**
     * 查询事件日志列表
     *
     * @param eventLog 事件日志
     * @return 事件日志
     */
    @Override
    public List<EventLog> selectEventLogList(EventLog eventLog) {
        LambdaQueryWrapperX<EventLog> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(StringUtils.isNotEmpty(eventLog.getSerialNumber()),EventLog::getSerialNumber, eventLog.getSerialNumber());
        wrapper.eq(StringUtils.isNotEmpty(eventLog.getIdentity()),EventLog::getIdentity, eventLog.getIdentity());
        wrapper.eq(!Objects.isNull(eventLog.getLogType()),EventLog::getLogType, eventLog.getLogType());
        if (eventLog.getParams()!= null && eventLog.getParams().get("beginTime")!= null && eventLog.getParams().get("endTime")!= null) {
            wrapper.between(EventLog::getCreateTime, eventLog.getParams().get("beginTime"), eventLog.getParams().get("endTime"));
        }
        wrapper.orderByDesc(EventLog::getCreateTime);
        return eventLogMapper.selectList(wrapper);
    }

    /**
     * 根据设备编号删除
     * @param serialNumber
     */
    @Override
    public void deleteEventLogBySerialNumber(String serialNumber){
        LambdaQueryWrapper<EventLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EventLog::getSerialNumber,serialNumber);
        eventLogMapper.delete(wrapper);
    }

    @Override
    public List<HistoryModel> listHistory(EventLog eventLog) {
        return eventLogMapper.listHistory(eventLog);
    }
}
