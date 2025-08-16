package com.fastbee.iot.service.impl;

import java.util.List;

import com.fastbee.iot.domain.Scene;
import org.springframework.stereotype.Service;
import com.fastbee.iot.mapper.SceneDeviceMapper;
import com.fastbee.iot.domain.SceneDevice;
import com.fastbee.iot.service.ISceneDeviceService;

import javax.annotation.Resource;

/**
 * 场景设备Service业务层处理
 *
 * @author kerwincui
 * @date 2023-12-28
 */
@Service
public class SceneDeviceServiceImpl implements ISceneDeviceService
{
    @Resource
    private SceneDeviceMapper sceneDeviceMapper;

    /**
     * 查询场景设备
     *
     * @param sceneDeviceId 场景设备主键
     * @return 场景设备
     */
    @Override
    public SceneDevice selectSceneDeviceBySceneDeviceId(Long sceneDeviceId)
    {
        return sceneDeviceMapper.selectSceneDeviceBySceneDeviceId(sceneDeviceId);
    }

    /**
     * 查询场景设备列表
     *
     * @param sceneDevice 场景设备
     * @return 场景设备
     */
    @Override
    public List<SceneDevice> selectSceneDeviceList(SceneDevice sceneDevice)
    {
        return sceneDeviceMapper.selectSceneDeviceList(sceneDevice);
    }

    /**
     * 新增场景设备
     *
     * @param sceneDevice 场景设备
     * @return 结果
     */
    @Override
    public int insertSceneDevice(SceneDevice sceneDevice)
    {
        return sceneDeviceMapper.insertSceneDevice(sceneDevice);
    }

    /**
     * 修改场景设备
     *
     * @param sceneDevice 场景设备
     * @return 结果
     */
    @Override
    public int updateSceneDevice(SceneDevice sceneDevice)
    {
        return sceneDeviceMapper.updateSceneDevice(sceneDevice);
    }

    /**
     * 批量删除场景设备
     *
     * @param sceneDeviceIds 需要删除的场景设备主键
     * @return 结果
     */
    @Override
    public int deleteSceneDeviceBySceneDeviceIds(Long[] sceneDeviceIds)
    {
        return sceneDeviceMapper.deleteSceneDeviceBySceneDeviceIds(sceneDeviceIds);
    }

    @Override
    public int deleteSceneDeviceBySceneIds(Long[] sceneIds) {
        return sceneDeviceMapper.deleteSceneDeviceBySceneIds(sceneIds);
    }

    @Override
    public int insertSceneDeviceList(List<SceneDevice> sceneDeviceList) {
        return sceneDeviceMapper.insertSceneDeviceList(sceneDeviceList);
    }

    @Override
    public List<Scene> selectTriggerDeviceRelateScenes(SceneDevice sceneDevice) {
        return sceneDeviceMapper.selectTriggerDeviceRelateScenes(sceneDevice);
    }

    /**
     * 删除场景设备信息
     *
     * @param sceneDeviceId 场景设备主键
     * @return 结果
     */
    @Override
    public int deleteSceneDeviceBySceneDeviceId(Long sceneDeviceId)
    {
        return sceneDeviceMapper.deleteSceneDeviceBySceneDeviceId(sceneDeviceId);
    }
}
