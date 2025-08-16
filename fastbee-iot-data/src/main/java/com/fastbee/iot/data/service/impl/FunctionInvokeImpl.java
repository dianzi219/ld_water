package com.fastbee.iot.data.service.impl;

import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.common.core.mq.InvokeReqDto;
import com.fastbee.common.core.mq.MQSendMessageBo;
import com.fastbee.common.enums.FunctionReplyStatus;
import com.fastbee.common.enums.scenemodel.SceneModelVariableTypeEnum;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.common.utils.bean.BeanUtils;
import com.fastbee.iot.data.service.IDataHandler;
import com.fastbee.iot.data.service.IFunctionInvoke;
import com.fastbee.iot.domain.FunctionLog;
import com.fastbee.iot.service.IFunctionLogService;
import com.fastbee.iot.util.SnowflakeIdWorker;
import com.fastbee.mq.producer.MessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author gsb
 * @date 2022/12/5 11:34
 */
@Slf4j
@Service
public class FunctionInvokeImpl implements IFunctionInvoke {

    @Resource
    private IDataHandler dataHandler;
    @Resource
    private IFunctionLogService functionLogService;
    private SnowflakeIdWorker snowflakeIdWorker = new SnowflakeIdWorker(2);

    /**
     * 服务调用，等待设备响应
     * @param reqDto 服务下发对象
     * @return 数据结果
     */
    @Override
    public AjaxResult invokeReply(InvokeReqDto reqDto){
        AjaxResult ajaxResult = invokeNoReply(reqDto);
        String messageId = ajaxResult.get("msg")+"";
        int status = this.queryData(messageId);
        ajaxResult.put("code", status);
        if (status == FunctionReplyStatus.UNKNOWN.getCode()) {
            ajaxResult.put("msg", FunctionReplyStatus.UNKNOWN.getMessage());
            this.updateFunctionLog(messageId);
        }else {
            ajaxResult.put("msg",FunctionReplyStatus.SUCCESS.getMessage());
        }
        return ajaxResult;
    }

    /**
     * 服务调用,设备不响应
     * @param reqDto 服务下发对象
     * @return 消息id messageId
     */
    @Override
    public AjaxResult invokeNoReply(InvokeReqDto reqDto){
        log.debug("=>下发指令请求：[{}]",reqDto);
        if (null != reqDto.getSceneModelId() && null != reqDto.getVariableType() && !SceneModelVariableTypeEnum.THINGS_MODEL.getType().equals(reqDto.getVariableType())) {
            String messageId = snowflakeIdWorker.nextId() + "";
            dataHandler.invokeSceneModelTagValue(reqDto, messageId);
            return AjaxResult.success(messageId);
        }
        if (StringUtils.isEmpty(reqDto.getSerialNumber())) {
            return AjaxResult.error("设备编号不能为空");
        }
        MQSendMessageBo bo = new MQSendMessageBo();
        BeanUtils.copyBeanProp(bo,reqDto);
        long id = snowflakeIdWorker.nextId();
        String messageId = id+"";
        bo.setMessageId(messageId+"");
        bo.setUserId(reqDto.getUserId());
        bo.setIsShadow("true".equals(reqDto.getIsShadow()));
        MessageProducer.sendFunctionInvoke(bo);
        return AjaxResult.success(messageId);
    }

    /**
     * 轮询5S拿返回值
     * @param messageId 消息id
     * @return 回调对象
     */
    private int queryData(String messageId) {
        FunctionLog functionLog = null;
        int status = 204;
        long startTime = DateUtils.getTimestamp();
        /**5秒轮询拿值*/
        while ((DateUtils.getTimestamp() - startTime) < 5 * 1000) {
            try {
                functionLog = functionLogService.selectLogByMessageId(messageId);
                if (null != functionLog && functionLog.getResultCode() == FunctionReplyStatus.SUCCESS.getCode()) {
                    status = FunctionReplyStatus.SUCCESS.getCode();
                    break;
                }
                Thread.sleep(1000);
            } catch (Exception e) {
                log.error("=>读取设备下发指定回执异常", e);
            }
        }
        return status;
    }

    /**
     * 更新设备下发指令，当设备超时未回复情况
     * @param messageId
     */
    private void updateFunctionLog(String messageId){
        FunctionLog functionLog = new FunctionLog();
        functionLog.setResultCode(FunctionReplyStatus.UNKNOWN.getCode());
        functionLog.setResultMsg(FunctionReplyStatus.UNKNOWN.getMessage());
        functionLog.setReplyTime(DateUtils.getNowDate());
        functionLog.setMessageId(messageId);
        functionLogService.updateByMessageId(functionLog);
    }

}
