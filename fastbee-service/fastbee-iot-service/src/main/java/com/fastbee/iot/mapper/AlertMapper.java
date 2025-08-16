package com.fastbee.iot.mapper;

import com.fastbee.iot.domain.Alert;
import com.fastbee.iot.domain.AlertNotifyTemplate;
import com.fastbee.iot.domain.AlertScene;
import com.fastbee.iot.domain.Scene;
import com.fastbee.iot.model.AlertSceneSendVO;
import com.fastbee.notify.domain.NotifyTemplate;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 设备告警Mapper接口
 *
 * @author kerwincui
 * @date 2022-01-13
 */
@Repository
public interface AlertMapper
{
    /**
     * 查询设备告警
     *
     * @param alertId 设备告警主键
     * @return 设备告警
     */
    public Alert selectAlertByAlertId(Long alertId);

    /**
     * 查询设备告警列表
     *
     * @param alert 设备告警
     * @return 设备告警集合
     */
    public List<Alert> selectAlertList(Alert alert);

    /**
     * 获取告警关联的场景列表
     * @param alertId
     * @return
     */
    public List<Scene> selectScenesByAlertId(Long alertId);

    /**
     * 新增设备告警
     *
     * @param alert 设备告警
     * @return 结果
     */
    public int insertAlert(Alert alert);

    /**
     * 修改设备告警
     *
     * @param alert 设备告警
     * @return 结果
     */
    public int updateAlert(Alert alert);

    /**
     * 删除设备告警
     *
     * @param alertId 设备告警主键
     * @return 结果
     */
    public int deleteAlertByAlertId(Long alertId);

    /**
     * 批量删除设备告警
     *
     * @param alertIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteAlertByAlertIds(Long[] alertIds);

    /**
     * 修改设备告警状态
     * @param alertId 告警id
     * @param status 状态
     * @return 结果
     */
    int updateAlertStatus(@Param("alertId") Long alertId, @Param("status") Integer status);

    /**
     * 批量插入告警场景
     * @param alerts
     * @return
     */
    public int insertAlertSceneList(List<AlertScene> alerts);

    /**
     * 根据告警ID批量删除告警场景
     * @param alertIds
     * @return
     */
    public int deleteAlertSceneByAlertIds(Long[]  alertIds);

    /**
     * 根据场景ID批量删除告警场景
     * @param sceneIds
     * @return
     */
    public int deleteAlertSceneBySceneIds(Long[] sceneIds);

    /**
     * 批量插入告警通知模版
     * @param alertNotifyTemplateList
     * @return
     */
    public int insertAlertNotifyTemplateList(List<AlertNotifyTemplate> alertNotifyTemplateList);

    /**
     * 根据告警ID批量删除告警场景
     * @param alertIds
     * @return
     */
    public int deleteAlertNotifyTemplateByAlertIds(Long[]  alertIds);

    /**
     * 查询告警通知模版
     * @param alertId 告警id
     * @return java.util.List<com.fastbee.iot.domain.AlertNotifyTemplate>
     */
    List<AlertNotifyTemplate> selectAlertNotifyTemplateList(Long alertId);

    /**
     * 获取告警关联的通知模版列表
     * @param alertId 告警id
     * @return
     */
    public List<NotifyTemplate> selectNotifyTemplateListByAlertId(Long alertId);
    
    /** 
     * 获取场景关联的告警列表
     * @param sceneId 场景id
     * @return java.util.List<com.fastbee.iot.domain.AlertScene>
     */ 
    List<AlertScene> selectAlertSceneListBySceneId(Long sceneId);

    /**
     * 获取场景告警通知参数
     *
     * @param sceneId 场景id
     * @return 设备告警集合
     */
    List<AlertSceneSendVO> listByAlertIds(Long sceneId);
}
