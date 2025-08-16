package com.fastbee.iot.data.service;

import com.fastbee.common.core.mq.message.ReportDataBo;

import java.util.concurrent.ScheduledExecutorService;

/**
 * 规则引擎处理数据方法集合
 * @author bill
 */
public interface IRuleEngine {
    /**
     * 规则匹配(告警和场景联动)
     *
     * @param bo 上报数据模型
     */
    public void ruleMatch(ReportDataBo bo);
    public void addCheckTask(String key, ScheduledExecutorService task);
    public void removeCheckTask(String key);
}
