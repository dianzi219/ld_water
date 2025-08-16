package com.fastbee.iot.service;

import java.util.List;

import com.fastbee.common.core.page.TableDataExtendInfo;
import com.fastbee.iot.domain.SceneModelData;
import com.fastbee.iot.model.scenemodel.SceneModelDataDTO;

/**
 * 【请填写功能名称】Service接口
 *
 * @author kerwincui
 * @date 2024-05-21
 */
public interface ISceneModelDataService
{
    /**
     * 查询变量详情
     *
     * @param id 主键
     * @return SceneModelData
     */
    public SceneModelData selectSceneModelDataById(Long id);

    /**
     * 查询所有变量列表
     *
     * @param sceneModelData 查询参数
     * @return 集合
     */
    public List<SceneModelDataDTO> selectSceneModelDataDTOList(SceneModelData sceneModelData);

    /**
     * 查询关联设备变量的列表
     * @param sceneModelData 查询参数
     * @return com.fastbee.common.core.page.TableDataExtendInfo
     */
    TableDataExtendInfo listByType(SceneModelData sceneModelData);

    /**
     * 新增【请填写功能名称】
     *
     * @param sceneModelData 【请填写功能名称】
     * @return 结果
     */
    public int insertSceneModelData(SceneModelData sceneModelData);

    /**
     * 修改【请填写功能名称】
     *
     * @param sceneModelData 【请填写功能名称】
     * @return 结果
     */
    public int updateSceneModelData(SceneModelData sceneModelData);

    /**
     * 批量删除【请填写功能名称】
     *
     * @param ids 需要删除的【请填写功能名称】主键集合
     * @return 结果
     */
    public int deleteSceneModelDataByIds(Long[] ids);

    /**
     * 删除【请填写功能名称】信息
     *
     * @param id 【请填写功能名称】主键
     * @return 结果
     */
    public int deleteSceneModelDataById(Long id);

    /**
     * 更新场景变量启用
     * @param sceneModelData
     * @return int
     */
    int editEnable(SceneModelData sceneModelData);

    /**
     * 批量查询
     * @param ids id集合
     * @return java.util.List<com.fastbee.iot.domain.SceneModelData>
     */
    List<SceneModelData> selectSceneModelDataListByIds(List<Long> ids);
}
