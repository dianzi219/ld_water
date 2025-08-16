package com.fastbee.notify.service;

import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.common.core.notify.NotifyConfigVO;
import com.fastbee.common.enums.NotifyChannelProviderEnum;
import com.fastbee.notify.domain.NotifyTemplate;

import java.util.List;

/**
 * 通知模版Service接口
 *
 * @author kerwincui
 * @date 2023-12-01
 */
public interface INotifyTemplateService
{
    /**
     * 查询通知模版
     *
     * @param id 通知模版主键
     * @return 通知模版
     */
    public NotifyTemplate selectNotifyTemplateById(Long id);

    /**
     * 查询通知模版列表
     *
     * @param notifyTemplate 通知模版
     * @return 通知模版集合
     */
    public List<NotifyTemplate> selectNotifyTemplateList(NotifyTemplate notifyTemplate);

    /**
     * 新增通知模版
     *
     * @param notifyTemplate 通知模版
     * @return 结果
     */
    public AjaxResult insertNotifyTemplate(NotifyTemplate notifyTemplate);

    /**
     * 修改通知模版
     *
     * @param notifyTemplate 通知模版
     * @return 结果
     */
    public AjaxResult updateNotifyTemplate(NotifyTemplate notifyTemplate);

    /**
     * 批量删除通知模版
     *
     * @param ids 需要删除的通知模版主键集合
     * @return 结果
     */
    public int deleteNotifyTemplateByIds(Long[] ids);

    /**
     * 删除通知模版信息
     *
     * @param id 通知模版主键
     * @return 结果
     */
    public int deleteNotifyTemplateById(Long id);

    /**
     * 查询某一业务通知通道是否有启动的（业务编码唯一启用一个模板）
     * @param notifyTemplate 通知模板
     */
    public Integer countNormalTemplate(NotifyTemplate notifyTemplate);

    /**
     * 更新某一类型为不可用状态，选中的为可用状态
     * @param notifyTemplate 通知模板
     */
    public void updateTemplateStatus(NotifyTemplate notifyTemplate);

    /**
     * @description: 查询启用通知模板
     * @param: serviceCode  业务编码
     * @return: com.fastbee.notify.domain.NotifyTemplate
     */
    NotifyTemplate selectOnlyEnable(NotifyTemplate notifyTemplate);

    /**
     * @description: 获取消息通知模版参数信息
     * @author fastb
     * @date 2023-12-22 11:01
     * @version 1.0
     */
    List<NotifyConfigVO> getNotifyMsgParams(Long channelId, String msgType);

    /**
     * @description: 统一获取模板参数内容变量，调用这个方法
     * @param: channelId
     * @return: java.lang.String
     */
    List<String> listVariables(String content, NotifyChannelProviderEnum notifyChannelProviderEnum);

    /**
     * @description: 获取告警微信小程序模板id
     * @param:
     * @return: java.lang.String
     */
    String getAlertWechatMini();

    /**
     * 获取唯一启用模版查询条件
     * 短信、语音、邮箱以业务编码+渠道保证唯一启用，微信、钉钉以业务编码+渠道+服务商保证唯一启用
     * @param: serviceCode
     * @param: channelType
     * @param: provider
     * @return com.fastbee.notify.domain.NotifyTemplate
     */
    NotifyTemplate getEnableQueryCondition(String serviceCode, String channelType, String provider, Long tenantId);

}
