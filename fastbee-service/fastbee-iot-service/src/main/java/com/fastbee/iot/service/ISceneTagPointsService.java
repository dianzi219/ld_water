package com.fastbee.iot.service;

import java.util.List;
import com.fastbee.iot.domain.SceneTagPoints;

/**
 * 运算型变量点Service接口
 * 
 * @author kerwincui
 * @date 2024-05-21
 */
public interface ISceneTagPointsService 
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
     * 批量删除运算型变量点
     * 
     * @param ids 需要删除的运算型变量点主键集合
     * @return 结果
     */
    public int deleteSceneTagPointsByIds(Long[] ids);

    /**
     * 删除运算型变量点信息
     * 
     * @param id 运算型变量点主键
     * @return 结果
     */
    public int deleteSceneTagPointsById(Long id);
}
