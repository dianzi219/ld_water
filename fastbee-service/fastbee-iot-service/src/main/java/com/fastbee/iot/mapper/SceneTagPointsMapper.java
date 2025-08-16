package com.fastbee.iot.mapper;

import java.util.List;
import com.fastbee.iot.domain.SceneTagPoints;

/**
 * 运算型变量点Mapper接口
 *
 * @author kerwincui
 * @date 2024-05-21
 */
public interface SceneTagPointsMapper
{
    /**
     * 查询运算型变量点
     *
     * @param id 运算型变量点主键
     * @return 运算型变量点
     */
    public SceneTagPoints selectSceneTagPointsById(Long id);

    /**
     * 查询运算型变量点列表
     *
     * @param sceneTagPoints 运算型变量点
     * @return 运算型变量点集合
     */
    public List<SceneTagPoints> selectSceneTagPointsList(SceneTagPoints sceneTagPoints);

    /**
     * 新增运算型变量点
     *
     * @param sceneTagPoints 运算型变量点
     * @return 结果
     */
    public int insertSceneTagPoints(SceneTagPoints sceneTagPoints);

    /**
     * 修改运算型变量点
     *
     * @param sceneTagPoints 运算型变量点
     * @return 结果
     */
    public int updateSceneTagPoints(SceneTagPoints sceneTagPoints);

    /**
     * 删除运算型变量点
     *
     * @param id 运算型变量点主键
     * @return 结果
     */
    public int deleteSceneTagPointsById(Long id);

    /**
     * 批量删除运算型变量点
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSceneTagPointsByIds(Long[] ids);

    /**
     * 批量删除场景变量运算
     * @param sceneModelIds
     * @return void
     */
    void deleteBySceneModelIds(Long[] sceneModelIds);

    /**
     * 批量删除场景变量运算
     * @param tagIds
     */
    void deleteByTagIds(Long[] tagIds);

    /**
     * 查询变量运算列表
     * @param id
     * @return java.util.List<com.fastbee.iot.domain.SceneTagPoints>
     */
    List<SceneTagPoints> selectListByTagId(Long id);
}
