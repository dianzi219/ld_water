package com.fastbee.notify.mapper;

import com.fastbee.notify.domain.NotifyTemplate;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知模版Mapper接口
 *
 * @author kerwincui
 * @date 2023-12-01
 */
public interface NotifyTemplateMapper
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
     * 查询同一业务已启用的模板
     * @param notifyTemplate
     * @return
     */
    public Integer selectEnableNotifyTemplateCount(NotifyTemplate notifyTemplate);

    /**
     * 新增通知模版
     *
     * @param notifyTemplate 通知模版
     * @return 结果
     */
    public int insertNotifyTemplate(NotifyTemplate notifyTemplate);

    /**
     * 修改通知模版
     *
     * @param notifyTemplate 通知模版
     * @return 结果
     */
    public int updateNotifyTemplate(NotifyTemplate notifyTemplate);

    /**
     * 批量更新渠道状态
     * @param ids ids
     * @return
     */
    public int updateNotifyBatch(@Param("ids") List<Long> ids, @Param("status") Integer status);

    /**
     * 删除通知模版
     *
     * @param id 通知模版主键
     * @return 结果
     */
    public int deleteNotifyTemplateById(Long id);

    /**
     * 批量删除通知模版
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteNotifyTemplateByIds(Long[] ids);

    /**
     * 根据业务编码查询启用模板
     * @param notifyTemplate 通知模板
     * @return com.fastbee.notify.domain.NotifyTemplate
     */
    NotifyTemplate selectOnlyEnable(NotifyTemplate notifyTemplate);

    /**
     * @description: 批量删除通知模板
     * @param: ids 渠道id数组
     * @return: void
     */
    void deleteNotifyTemplateByChannelIds(Long[] channelIds);

    /**
     * @description: 查询通知模板
     * @param: templateIdList
     * @return: java.util.List<com.fastbee.notify.domain.NotifyTemplate>
     */
    List<NotifyTemplate> selectNotifyTemplateByIds(@Param("idList") List<Long> idList);

    /**
     * 根据渠道id查询模板
     * @param channelId 渠道id
     * @return java.util.List<com.fastbee.notify.domain.NotifyTemplate>
     */
    List<NotifyTemplate> selectNotifyTemplateByChannelId(Long channelId);

    /**
     * 根据场景ID批量删除告警场景
     * @param notifyTemplateIds
     * @return
     */
    public int deleteAlertNotifyTemplateByNotifyTemplateIds(Long[] notifyTemplateIds);
}
