package com.fastbee.iot.service;

import com.fastbee.common.core.iot.response.IdentityAndName;
import com.fastbee.iot.domain.ThingsModel;
import com.fastbee.iot.model.ImportThingsModelInput;
import com.fastbee.iot.model.ThingsModelPerm;
import com.fastbee.iot.model.ThingsModelSimVO;
import com.fastbee.iot.model.ThingsModels.IdentifierVO;
import com.fastbee.iot.model.ThingsModels.PropertyDto;
import com.fastbee.iot.model.ThingsModels.ThingsItems;
import com.fastbee.iot.model.ThingsModels.ThingsModelValueItem;
import com.fastbee.iot.model.dto.ThingsModelDTO;
import com.fastbee.iot.model.modbus.ModbusAndThingsVO;

import java.util.List;
import java.util.Map;

/**
 * 物模型Service接口
 *
 * @author kerwincui
 * @date 2021-12-16
 */
public interface IThingsModelService
{
    /**
     * 查询物模型
     *
     * @param modelId 物模型主键
     * @return 物模型
     */
    public ThingsModel selectThingsModelByModelId(Long modelId);

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
     * 查询物模型对应设备分享用户权限列表
     *
     * @param productId 产品ID
     */
    public List<ThingsModelPerm> selectThingsModelPermList(Long productId);


    /**
     * 新增物模型
     *
     * @param thingsModel 物模型
     * @return 结果
     */
    public int insertThingsModel(ThingsModel thingsModel);

    /**
     * 从模板导入物模型
     * @param input
     * @return
     */
    public int importByTemplateIds(ImportThingsModelInput input);

    /**
     * 修改物模型
     *
     * @param thingsModel 物模型
     * @return 结果
     */
    public int updateThingsModel(ThingsModel thingsModel);

    /**
     * 批量删除物模型
     *
     * @param modelIds 需要删除的物模型主键集合
     * @return 结果
     */
    public int deleteThingsModelByModelIds(Long[] modelIds);

    /**
     * 删除物模型信息
     *
     * @param modelId 物模型主键
     * @return 结果
     */
    public int deleteThingsModelByModelId(Long modelId);


    /**
     * 获取单个物模型获取
     * @param productId
     * @param identify
     * @return
     */
    public ThingsModelValueItem getSingleThingModels(Long productId, String identify);



    /**
     * 导入采集点数据
     * @param lists 数据列表
     * @param tempSlaveId 从机编码
     * @return 结果
     */
    public String importData(List<ThingsModel> lists,Long productId);

    /**
     * 根据产品id删除 产品物模型以及物模型缓存
     * @param productId
     */
    public void deleteProductThingsModelAndCacheByProductId(Long productId);

    /**
     * 查询物模型数据定义
     * @param productId 产品id
     * @param identifier 物模型id
     * @return
     */
    String getSpecsByProductIdAndIdentifier(Long productId, String identifier);

    /**
     * 批量查询产品物模型
     * @param productIdList 产品id集合
     * @return
     */
    List<ThingsModelSimVO> listSimByProductIds(List<Long> productIdList);


    /**
     * 获取modbus配置可选择物模型
     * @param productId
     * @return
     */
    List<ModbusAndThingsVO> getModbusConfigUnSelectThingsModel(Long productId);

    /**
     * 产品下物模型统一拆分数组、对象、数组对象
     * @param productId 产品id
     * @return
     */
    List<ThingsModelDTO> changeObjectOrArrayByProductId(Long productId);

    /**
     * 单个物模型统一拆分数组、对象、数组对象
     * @param model
     * @return
     */
    List<ThingsModelDTO> changeObjectOrArray(ThingsModel model);

    /**
     * 还原数组或对象物模型标识
     * @param identifier 物模标识 带array_
     * @return java.lang.String
     */
    IdentifierVO revertObjectOrArrayIdentifier(String identifier);

    /**
     * 获取单个物模缓存值
     * @param productId 产品id
     * @param: serialNumber 设备编号
     * @param: identifier 物模标识
     * @return java.lang.String
     */
    String getCacheIdentifierValue(Long productId, String serialNumber, String identifier);

    /**
     * 获取产品缓存物模型并拆分数组
     * @param productId 产品id
     * @param serialNumber 设备编号 可以不传
     * @return java.util.List<com.fastbee.iot.domain.ThingsModel>
     */
    List<ThingsModelDTO> getCacheAndHandleArrayByProductId(Long productId, String serialNumber);

    /**
     * 批量查询物模型
     * @param modelIdList 物模id集合
     * @return java.util.List<com.fastbee.iot.domain.ThingsModel>
     */
    List<ThingsModel> selectThingsModelListByModelIds(List<Long> modelIdList);

    /**
     * 根据设备编号获取读写物模型
     * @param serialNumber
     * @return
     */
    List<ThingsModel> selectThingsModelBySerialNumber(Long deviceId);
}
