package com.fastbee.iot.service.impl;

import com.fastbee.common.core.domain.entity.SysUser;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.iot.domain.Alert;
import com.fastbee.iot.domain.AlertNotifyTemplate;
import com.fastbee.iot.domain.AlertScene;
import com.fastbee.iot.domain.Scene;
import com.fastbee.iot.mapper.AlertMapper;
import com.fastbee.iot.model.AlertSceneSendVO;
import com.fastbee.iot.service.IAlertService;
import com.fastbee.notify.domain.NotifyTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.fastbee.common.utils.SecurityUtils.getLoginUser;

/**
 * 设备告警Service业务层处理
 *
 * @author kerwincui
 * @date 2022-01-13
 */
@Slf4j
@Service
public class AlertServiceImpl implements IAlertService {

    @Autowired
    private AlertMapper alertMapper;

    /**
     * 查询设备告警
     *
     * @param alertId 设备告警主键
     * @return 设备告警
     */
    @Override
    public Alert selectAlertByAlertId(Long alertId) {
        Alert alert = alertMapper.selectAlertByAlertId(alertId);
        alert.setScenes(alertMapper.selectScenesByAlertId(alert.getAlertId()));
        alert.setNotifyTemplateList(alertMapper.selectNotifyTemplateListByAlertId(alert.getAlertId()));
        return alert;
    }

    /**
     * 查询设备告警列表
     *
     * @param alert 设备告警
     * @return 设备告警
     */
    @Override
    public List<Alert> selectAlertList(Alert alert) {
        // 查询所属机构
        SysUser user = getLoginUser().getUser();
        if (null != user.getDeptId()) {
            alert.setTenantId(user.getDept().getDeptUserId());
        } else {
            alert.setTenantId(user.getUserId());
        }
        List<Alert> alertList = alertMapper.selectAlertList(alert);
        return alertList;
    }

    /**
     * 获取告警关联的场景列表
     * @param alertId
     * @return
     */
    public List<Scene> selectScenesByAlertId(Long alertId){
        List<Scene> sceneList =alertMapper.selectScenesByAlertId(alertId);
        return sceneList;
    }

    /**
     * 新增设备告警
     *
     * @param alert 设备告警
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertAlert(Alert alert) {
        // 新增告警配置
        alert.setCreateTime(DateUtils.getNowDate());
        int result=alertMapper.insertAlert(alert);
        // 批量新增告警关联场景
        if(alert.getScenes()!=null && alert.getScenes().size()>0) {
            List<AlertScene> alertScenes = new ArrayList<>();
            for (Scene scene : alert.getScenes()) {
                AlertScene alertScene = new AlertScene();
                alertScene.setAlertId(alert.getAlertId());
                alertScene.setSceneId(scene.getSceneId());
                alertScenes.add(alertScene);
            }
            alertMapper.insertAlertSceneList(alertScenes);
        }
        // 批量新增通知模版
        if (CollectionUtils.isNotEmpty(alert.getNotifyTemplateList())) {
            List<AlertNotifyTemplate> alertNotifyTemplateList = new ArrayList<>();
            for (NotifyTemplate notifyTemplate : alert.getNotifyTemplateList()) {
                AlertNotifyTemplate alertNotifyTemplate = new AlertNotifyTemplate();
                alertNotifyTemplate.setAlertId(alert.getAlertId());
                alertNotifyTemplate.setNotifyTemplateId(notifyTemplate.getId());
                alertNotifyTemplateList.add(alertNotifyTemplate);
            }
            alertMapper.insertAlertNotifyTemplateList(alertNotifyTemplateList);
        }
        return result;
    }

    /**
     * 修改设备告警
     *
     * @param alert 设备告警
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAlert(Alert alert) {
        // 批量删除告警场景
        alertMapper.deleteAlertSceneByAlertIds(new Long[]{alert.getAlertId()});
        // 批量删除告警通知
        alertMapper.deleteAlertNotifyTemplateByAlertIds(new Long[]{alert.getAlertId()});
        // 更新告警配置
        alert.setUpdateTime(DateUtils.getNowDate());
        int result = alertMapper.updateAlert(alert);
        // 批量新增告警关联场景
        if(alert.getScenes()!=null && alert.getScenes().size()>0) {
            List<AlertScene> alertScenes = new ArrayList<>();
            for (Scene scene : alert.getScenes()) {
                AlertScene alertScene = new AlertScene();
                alertScene.setAlertId(alert.getAlertId());
                alertScene.setSceneId(scene.getSceneId());
                alertScenes.add(alertScene);
            }
            alertMapper.insertAlertSceneList(alertScenes);
        }
        // 批量新增通知模版
        if (CollectionUtils.isNotEmpty(alert.getNotifyTemplateList())) {
            List<AlertNotifyTemplate> alertNotifyTemplateList = new ArrayList<>();
            for (NotifyTemplate notifyTemplate : alert.getNotifyTemplateList()) {
                AlertNotifyTemplate alertNotifyTemplate = new AlertNotifyTemplate();
                alertNotifyTemplate.setAlertId(alert.getAlertId());
                alertNotifyTemplate.setNotifyTemplateId(notifyTemplate.getId());
                alertNotifyTemplateList.add(alertNotifyTemplate);
            }
            alertMapper.insertAlertNotifyTemplateList(alertNotifyTemplateList);
        }
        return result;
    }

    /**
     * 批量删除设备告警
     *
     * @param alertIds 需要删除的设备告警主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAlertByAlertIds(Long[] alertIds) {
        // 批量删除告警场景
        alertMapper.deleteAlertSceneByAlertIds(alertIds);
        // 批量删除告警通知模版配置
        alertMapper.deleteAlertNotifyTemplateByAlertIds(alertIds);
        // 删除告警配置
        return alertMapper.deleteAlertByAlertIds(alertIds);
    }

    /**
     * 删除设备告警信息
     *
     * @param alertId 设备告警主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAlertByAlertId(Long alertId) {
        // 批量删除告警场景
        alertMapper.deleteAlertSceneByAlertIds(new Long[]{alertId});
        // 批量删除告警通知模版配置
        alertMapper.deleteAlertNotifyTemplateByAlertIds(new Long[]{alertId});
        return alertMapper.deleteAlertByAlertId(alertId);
    }

    /**
     * 修改设备告警状态
     *
     * @param alertId 告警id
     * @param status  状态
     * @return 结果
     */
    @Override
    public int editStatus(Long alertId, Integer status) {
        return alertMapper.updateAlertStatus(alertId, status);
    }

    @Override
    public List<NotifyTemplate> listNotifyTemplateByAlertId(Long alertId) {
        return alertMapper.selectNotifyTemplateListByAlertId(alertId);
    }

    @Override
    public List<AlertNotifyTemplate> listAlertNotifyTemplate(Long alertId) {
        return alertMapper.selectAlertNotifyTemplateList(alertId);
    }

    @Override
    public List<AlertScene> listAlertScene(Long sceneId) {
        return alertMapper.selectAlertSceneListBySceneId(sceneId);
    }

    @Override
    public List<AlertSceneSendVO> listByAlertIds(Long sceneId) {
        return alertMapper.listByAlertIds(sceneId);
    }


}
