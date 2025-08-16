package com.fastbee.iot.service;

import java.util.List;
import com.fastbee.iot.domain.SceneModel;

/**
 * 场景管理Service接口
 *
 * @author kerwincui
 * @date 2024-05-20
 */
public interface ISceneModelService
{
    /**
     * 查询场景管理
     *
     * @param sceneModelId 场景管理主键
     * @return 场景管理
     */
    public SceneModel selectSceneModelBySceneModelId(Long sceneModelId);

    /**
     * 查询场景管理列表
     *
     * @param sceneModel 场景管理
     * @return 场景管理集合
     */
    public List<SceneModel> selectSceneModelList(SceneModel sceneModel);

    /**
     * 新增场景管理
     *
     * @param sceneModel 场景管理
     * @return 结果
     */
    public int insertSceneModel(SceneModel sceneModel);

    /**
     * 修改场景管理
     *
     * @param sceneModel 场景管理
     * @return 结果
     */
    public int updateSceneModel(SceneModel sceneModel);

    /**
     * 批量删除场景管理
     *
     * @param sceneModelIds 需要删除的场景管理主键集合
     * @return 结果
     */
    public int deleteSceneModelBySceneModelIds(Long[] sceneModelIds);

    /**
     * 删除场景管理信息
     *
     * @param sceneModelId 场景管理主键
     * @return 结果
     */
    public int deleteSceneModelBySceneModelId(Long sceneModelId);

    /**
     * 根据监控设备id获取场景
     * @param channelId
     * @return
     */
    SceneModel selectSceneModelByChannelId(String channelId);
}
