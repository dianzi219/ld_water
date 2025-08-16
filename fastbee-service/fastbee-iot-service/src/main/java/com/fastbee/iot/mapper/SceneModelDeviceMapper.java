package com.fastbee.iot.mapper;

import java.util.List;
import com.fastbee.iot.domain.SceneModelDevice;
import org.apache.ibatis.annotations.Param;

/**
 * 场景管理关联设备Mapper接口
 *
 * @author kerwincui
 * @date 2024-05-21
 */
public interface SceneModelDeviceMapper
{
    /**
     * 查询场景管理关联设备
     *
     * @param id 场景管理关联设备主键
     * @return 场景管理关联设备
     */
    public SceneModelDevice selectSceneModelDeviceById(Long id);

    /**
     * 查询场景管理关联设备列表
     *
     * @param sceneModelDevice 场景管理关联设备
     * @return 场景管理关联设备集合
     */
    public List<SceneModelDevice> selectSceneModelDeviceList(SceneModelDevice sceneModelDevice);

    /**
     * 新增场景管理关联设备
     *
     * @param sceneModelDevice 场景管理关联设备
     * @return 结果
     */
    public int insertSceneModelDevice(SceneModelDevice sceneModelDevice);

    /**
     * 修改场景管理关联设备
     *
     * @param sceneModelDevice 场景管理关联设备
     * @return 结果
     */
    public int updateSceneModelDevice(SceneModelDevice sceneModelDevice);

    /**
     * 删除场景管理关联设备
     *
     * @param id 场景管理关联设备主键
     * @return 结果
     */
    public int deleteSceneModelDeviceById(Long id);

    /**
     * 批量删除场景管理关联设备
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSceneModelDeviceByIds(Long[] ids);

    /**
     * 查询关联未关联设备的设备配置
     * @return
     */
    SceneModelDevice selectOneModelDevice(Long sceneModelId);

    /**
     * 批量删除场景关联
     * @param sceneModelIds
     * @return void
     */
    void deleteBySceneModelIds(Long[] sceneModelIds);

    /**
     * 更新全部启用状态
     * @param updateSceneModelDevice
     * @return int
     */
    int updateAllEnable(SceneModelDevice updateSceneModelDevice);

    /**
     * 查询关联的录入、运算型
     * @param sceneModelId 场景id
     * @param: variableType 类型
     * @return com.fastbee.iot.domain.SceneModelDevice
     */
    SceneModelDevice selectOneNoDeviceBySceneModelId(@Param("sceneModelId") Long sceneModelId, @Param("variableType") Integer variableType);

    /**
     * 校验是否有使用计算公式
     * @param id 主键
     * @return int
     */
    int checkContainAliasFormule(Long id);

    /**
     * 查询场景关联设备
     * @param productId 产品id
     * @return java.util.List<com.fastbee.iot.domain.SceneModelDevice>
     */
    List<SceneModelDevice> listDeviceByProductId(Long productId);
}
