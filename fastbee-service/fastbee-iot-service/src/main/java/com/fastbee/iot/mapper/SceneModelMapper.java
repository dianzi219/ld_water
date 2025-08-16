package com.fastbee.iot.mapper;

import java.util.List;

import com.fastbee.iot.domain.Product;
import com.fastbee.iot.domain.SceneModel;
import org.apache.ibatis.annotations.Param;

/**
 * 场景管理Mapper接口
 *
 * @author kerwincui
 * @date 2024-05-20
 */
public interface SceneModelMapper
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
     * 删除场景管理
     *
     * @param sceneModelId 场景管理主键
     * @return 结果
     */
    public int deleteSceneModelBySceneModelId(Long sceneModelId);

    /**
     * 批量删除场景管理
     *
     * @param sceneModelIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSceneModelBySceneModelIds(Long[] sceneModelIds);

    /**
     * 根据监控设备id获取场景
     * @param channelId
     * @return
     */
    SceneModel selectSceneModelByChannelId(String channelId);

    /**
     * 查询组态信息
     * @param guidList 组态id集合
     * @return java.util.List<com.fastbee.iot.domain.Product>
     */
    List<SceneModel> selectListScadaIdByGuidS(@Param("guidList") List<String> guidList);

    /**
     * 查询场景信息
     * @param sceneModelId 场景id
     * @return com.fastbee.iot.domain.SceneModel
     */
    SceneModel selectBySceneModelId(Long sceneModelId);
}
