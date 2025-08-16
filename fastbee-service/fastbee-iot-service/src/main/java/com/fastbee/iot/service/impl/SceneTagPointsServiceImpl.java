package com.fastbee.iot.service.impl;

import java.util.List;
import com.fastbee.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fastbee.iot.mapper.SceneTagPointsMapper;
import com.fastbee.iot.domain.SceneTagPoints;
import com.fastbee.iot.service.ISceneTagPointsService;

import javax.annotation.Resource;

/**
 * 运算型变量点Service业务层处理
 *
 * @author kerwincui
 * @date 2024-05-21
 */
@Service
public class SceneTagPointsServiceImpl implements ISceneTagPointsService
{
    @Resource
    private SceneTagPointsMapper sceneTagPointsMapper;

    /**
     * 查询运算型变量点
     *
     * @param id 运算型变量点主键
     * @return 运算型变量点
     */
    @Override
    public SceneTagPoints selectSceneTagPointsById(Long id)
    {
        return sceneTagPointsMapper.selectSceneTagPointsById(id);
    }

    /**
     * 查询运算型变量点列表
     *
     * @param sceneTagPoints 运算型变量点
     * @return 运算型变量点
     */
    @Override
    public List<SceneTagPoints> selectSceneTagPointsList(SceneTagPoints sceneTagPoints)
    {
        return sceneTagPointsMapper.selectSceneTagPointsList(sceneTagPoints);
    }

    /**
     * 新增运算型变量点
     *
     * @param sceneTagPoints 运算型变量点
     * @return 结果
     */
    @Override
    public int insertSceneTagPoints(SceneTagPoints sceneTagPoints)
    {
        sceneTagPoints.setCreateTime(DateUtils.getNowDate());
        return sceneTagPointsMapper.insertSceneTagPoints(sceneTagPoints);
    }

    /**
     * 修改运算型变量点
     *
     * @param sceneTagPoints 运算型变量点
     * @return 结果
     */
    @Override
    public int updateSceneTagPoints(SceneTagPoints sceneTagPoints)
    {
        sceneTagPoints.setUpdateTime(DateUtils.getNowDate());
        return sceneTagPointsMapper.updateSceneTagPoints(sceneTagPoints);
    }

    /**
     * 批量删除运算型变量点
     *
     * @param ids 需要删除的运算型变量点主键
     * @return 结果
     */
    @Override
    public int deleteSceneTagPointsByIds(Long[] ids)
    {
        return sceneTagPointsMapper.deleteSceneTagPointsByIds(ids);
    }

    /**
     * 删除运算型变量点信息
     *
     * @param id 运算型变量点主键
     * @return 结果
     */
    @Override
    public int deleteSceneTagPointsById(Long id)
    {
        return sceneTagPointsMapper.deleteSceneTagPointsById(id);
    }
}
