package com.fastbee.iot.mapper;

import java.util.List;
import com.fastbee.iot.domain.SceneModelData;
import com.fastbee.iot.model.scenemodel.SceneDeviceThingsModelVO;
import com.fastbee.iot.model.scenemodel.SceneModelDataDTO;
import com.fastbee.iot.model.scenemodel.SceneModelDeviceVO;
import org.apache.ibatis.annotations.Param;

/**
 * 【请填写功能名称】Mapper接口
 *
 * @author kerwincui
 * @date 2024-05-21
 */
public interface SceneModelDataMapper
{
    /**
     * 查询【请填写功能名称】
     *
     * @param id 【请填写功能名称】主键
     * @return 【请填写功能名称】
     */
    public SceneModelData selectSceneModelDataById(Long id);

    /**
     * 查询【请填写功能名称】列表
     *
     * @param sceneModelData 【请填写功能名称】
     * @return 【请填写功能名称】集合
     */
    public List<SceneModelData> selectSceneModelDataList(SceneModelData sceneModelData);

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
     * 删除【请填写功能名称】
     *
     * @param id 【请填写功能名称】主键
     * @return 结果
     */
    public int deleteSceneModelDataById(Long id);

    /**
     * 批量删除【请填写功能名称】
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSceneModelDataByIds(Long[] ids);

    /**
     * 查询场景关联设备物模型变量
     * @param datasourceId
     * @return
     */
    List<SceneModelData> listBySourceIdAndType(Long datasourceId);

    /**
     * 删除场景关联设备物模型变量
     * @param datasourceIds
     * @return void
     */
    void deleteThingsModelByDatasourceId(Long[] datasourceIds);

    /**
     * 批量删除场景变量
     * @param sceneModelIds
     * @return void
     */
    void deleteBySceneModelIds(Long[] sceneModelIds);

    /**
     * 更新关联设备变量所有启用状态
     * @param sceneModelData
     * @return int
     */
    int editAllEnable(SceneModelData sceneModelData);

    /**
     * 删除设备关联变量
     * @param sceneModelDeviceIds 设备关联id
     * @return void
     */
    void deleteBySceneModelDeviceIds(Long[] sceneModelDeviceIds);

    /**
     * 统计统一关联来源未启用
     * @param sceneModelDeviceId
     * @return int
     */
    int countNoEnableBySceneModelDeviceId(Long sceneModelDeviceId);

    void deleteBySourceIds(Long[] datasourceIds);

    /**
     * 批量插入
     * @param sceneModelDataList 变量集合
     * @return int
     */
    int insertBatchSceneModelData(@Param("sceneModelDataList") List<SceneModelData> sceneModelDataList);

    /**
     * 查询变量
     * @param datasourceId
     * @param: variableType
     * @return com.fastbee.iot.domain.SceneModelData
     */
    SceneModelData selectNoDeviceBySourceIdAndVariableType(@Param("datasourceId") Long datasourceId, @Param("variableType") Integer variableType);

    /**
     * 查询关联设备
     * @param idList 变量id集合
     * @return com.fastbee.iot.model.scenemodel.SceneModelDeviceVO
     */
    List<SceneModelDeviceVO> selectSceneModelDeviceByDataIdList(@Param("idList") List<Long> idList);

    /**
     * 查询单个物模型来源
     * @param id 主键
     * @return com.fastbee.iot.model.scenemodel.SceneDeviceThingsModelVO
     */
    SceneDeviceThingsModelVO selectDeviceThingsModelById(Long id);

    /**
     * 查询录入型变量默认值
     * @param id 主键id
     * @return java.lang.String
     */
    String selectInputTagDefaultValueById(Long id);

    /**
     * 检查是否被应用到计算公式
     * @param datasourceId  数据源id
     * @param: type
     * @return int
     */
    int checkIsApplyAliasFormule(@Param("datasourceId") Long datasourceId, @Param("variableType") Integer variableType);

    /**
     * 批量查询
     * @param ids id集合
     * @return java.util.List<com.fastbee.iot.domain.SceneModelData>
     */
    List<SceneModelData> selectSceneModelDataListByIds(@Param("ids") List<Long> ids);

    /**
     * 查询场景变量完整信息列表
     * @param sceneModelData 场景变量类
     * @return java.util.List<com.fastbee.iot.model.scenemodel.SceneModelDataDTO>
     */
    List<SceneModelDataDTO> selectSceneModelDataDTOList(SceneModelData sceneModelData);

    /**
     * 查询场景关联设备物模型
     * @param datasourceId 数据源id
     * @return java.util.List<com.fastbee.iot.domain.SceneModelData>
     */
    List<SceneModelData> selectSceneDeviceThingsModelList(Long datasourceId);

    /**
     * @description: 同步变量
     * @param: updateSceneModelData 变量信息
     * @return: void
     */
    void updateSceneModelDataByDatasourceId(SceneModelData updateSceneModelData);
}
