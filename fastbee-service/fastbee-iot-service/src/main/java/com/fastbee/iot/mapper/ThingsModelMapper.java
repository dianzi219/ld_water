package com.fastbee.iot.mapper;

import com.fastbee.common.core.iot.response.IdentityAndName;
import com.fastbee.iot.domain.ThingsModel;
import com.fastbee.iot.model.ThingsModelPerm;
import com.fastbee.iot.model.ThingsModelSimVO;
import com.fastbee.iot.model.ThingsModels.PropertyDto;
import com.fastbee.iot.model.ThingsModels.ThingsItems;
import com.fastbee.iot.model.modbus.ModbusAndThingsVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 物模型Mapper接口
 *
 * @author kerwincui
 * @date 2021-12-16
 */
@Repository
public interface ThingsModelMapper
{
    /**
     * 查询物模型
     *
     * @param modelId 物模型主键
     * @param language 语言
     * @return 物模型
     */
    public ThingsModel selectThingsModelByModelId(@Param("modelId") Long modelId, @Param("language") String language);

    /**
     * 查询单个物模型
     * @param model 物模型
     * @return 单个物模型
     */
    public ThingsModel selectSingleThingsModel(ThingsModel model);

    /**
     * 查询物模型列表
     *
     * @param thingsModel 物模型
     * @return 物模型集合
     */
    public List<ThingsModel> selectThingsModelList(ThingsModel thingsModel);

    /**
     * 查询物模型对应分享设备权限列表
     *
     * @param productId 产品ID
     * @param language 语言
     * @return 物模型集合
     */
    public List<ThingsModelPerm> selectThingsModelPermList(@Param("productId") Long productId, @Param("language") String language);


    /**
     * 根据产品ID数组获取物模型列表
     * @param modelIds
     * @param language 语言
     * @return
     */
    public List<ThingsModel> selectThingsModelListByProductIds(@Param("modelIds") Long[] modelIds, @Param("language") String language);

    /**
     * 新增物模型
     *
     * @param thingsModel 物模型
     * @return 结果
     */
    public int insertThingsModel(ThingsModel thingsModel);

    /**
     * 批量新增物模型
     * @param thingsModels
     * @return
     */
    public int insertBatchThingsModel(List<ThingsModel> thingsModels);

    /**
     * 修改物模型
     *
     * @param thingsModel 物模型
     * @return 结果
     */
    public int updateThingsModel(ThingsModel thingsModel);

    /**
     * 删除物模型
     *
     * @param modelId 物模型主键
     * @return 结果
     */
    public int deleteThingsModelByModelId(Long modelId);

    /**
     * 批量删除物模型
     *
     * @param modelIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteThingsModelByModelIds(Long[] modelIds);

    /**
     * 根据产品id删除对应的物模型id
     * @param productId
     * @return
     */
    public int deleteThingsModelByProductId(Long productId);

    /**
     * 查询物模型是否历史存储
     * @param items
     * @return
     */
    public List<IdentityAndName> selectThingsModelIsMonitor(ThingsItems items);


    List<ThingsModelSimVO> listSimByProductIds(@Param("productIdList") List<Long> productIdList, @Param("language") String language);

    /**
     * 查询物模型数据定义
     * @param productId 产品id
     * @param identifier 物模型id
     * @return
     */
    String getSpecsByProductIdAndIdentifier(@Param("productId") Long productId, @Param("identifier") String identifier);

    /**
     * 场景管理查询相关物模型列表
     * @param modelIdList 物模id
     * @return java.util.List<com.fastbee.iot.domain.ThingsModel>
     */
    List<ThingsModel> listSceneModelDataByModelIds(@Param("modelIdList") List<Long> modelIdList,@Param("language") String languag);

    /**
     * 获取modbus配置可选择物模型
     * @param productId
     * @return
     */
    List<ModbusAndThingsVO> getModbusConfigUnSelectThingsModel(@Param("productId")Long productId, @Param("language") String language);

    /**
     * 批量查询物模型
     * @param modelIdList 物模id集合
     * @return java.util.List<com.fastbee.iot.domain.ThingsModel>
     */
    List<ThingsModel> selectThingsModelListByModelIds(@Param("modelIdList") List<Long> modelIdList);

    /**
     * 根据设备编号获取读写物模型
     * @param serialNumber
     * @return
     */
    List<ThingsModel> selectThingsModelBySerialNumber(@Param("deviceId") Long deviceId , @Param("language") String language);
}
