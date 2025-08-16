package com.fastbee.iot.data.service;

import com.fastbee.common.core.mq.InvokeReqDto;
import com.fastbee.common.core.mq.message.ReportDataBo;

/**
 * 客户端上报数据处理方法集合
 * @author bill
 */
public interface IDataHandler {

    /**
     * 上报属性或功能处理
     *
     * @param bo 上报数据模型
     */
    public void reportData(ReportDataBo bo);


    /**
     * 上报事件
     *
     * @param bo 上报数据模型
     */
    public void reportEvent(ReportDataBo bo);

    /**
     * 上报设备信息
     * @param bo 上报数据模型
     */
    public void reportDevice(ReportDataBo bo);

    /**
     * 计算场景变量的值
     * @param id 主键id
     * @return java.lang.String
     */
    String calculateSceneModelTagValue(Long id);

    /**
     * 场景变量指令下发
     * @param reqDto 下发类
     * @return void
     */
    void invokeSceneModelTagValue(InvokeReqDto reqDto, String messageId);
}
