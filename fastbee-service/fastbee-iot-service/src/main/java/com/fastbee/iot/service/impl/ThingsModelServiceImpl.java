package com.fastbee.iot.service.impl;

import java.util.*;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fastbee.common.core.domain.entity.SysUser;
import com.fastbee.common.core.redis.RedisCache;
import com.fastbee.common.core.redis.RedisKeyBuilder;
import com.fastbee.common.enums.scenemodel.SceneModelVariableTypeEnum;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.common.utils.SecurityUtils;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.iot.cache.ITSLCache;
import com.fastbee.iot.domain.*;
import com.fastbee.iot.mapper.*;
import com.fastbee.iot.model.ImportThingsModelInput;
import com.fastbee.iot.model.ThingsModelItem.Datatype;
import com.fastbee.iot.model.ThingsModelPerm;
import com.fastbee.iot.model.ThingsModelSimVO;
import com.fastbee.iot.model.ThingsModels.IdentifierVO;
import com.fastbee.iot.model.ThingsModels.ThingsModelValueItem;
import com.fastbee.iot.model.ThingsModels.ValueItem;
import com.fastbee.iot.model.dto.ThingsModelDTO;
import com.fastbee.iot.model.modbus.ModbusAndThingsVO;
import com.fastbee.iot.model.varTemp.EnumClass;
import com.fastbee.iot.service.IThingsModelService;
import com.fastbee.iot.cache.ITSLValueCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.fastbee.common.utils.SecurityUtils.getLoginUser;

/**
 * 物模型Service业务层处理
 *
 * @author kerwincui
 * @date 2021-12-16
 */
@Service
@Slf4j
public class ThingsModelServiceImpl implements IThingsModelService {

    @Resource
    private ThingsModelMapper thingsModelMapper;
    @Resource
    private ThingsModelTemplateMapper thingsModelTemplateMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RedisCache redisCache;
    @Resource
    private SceneModelDataMapper sceneModelDataMapper;
    @Resource
    private SceneModelDeviceMapper sceneModelDeviceMapper;
    @Resource
    private ITSLValueCache thingModelCache;
    @Resource
    private ITSLCache itslCache;


    /**
     * 查询物模型
     *
     * @param modelId 物模型主键
     * @return 物模型
     */
    @Override
    public ThingsModel selectThingsModelByModelId(Long modelId) {
        return thingsModelMapper.selectThingsModelByModelId(modelId, SecurityUtils.getLanguage());
    }

    /**
     * 查询单个物模型
     *
     * @param model 物模型
     * @return 单个物模型
     */
    @Override
    public ThingsModel selectSingleThingsModel(ThingsModel model) {
        model.setLanguage(SecurityUtils.getLanguage());
        return thingsModelMapper.selectSingleThingsModel(model);
    }

    /**
     * 查询物模型列表
     *
     * @param thingsModel 物模型
     * @return 物模型
     */
    @Override
    public List<ThingsModel> selectThingsModelList(ThingsModel thingsModel) {
        thingsModel.setLanguage(SecurityUtils.getLanguage());
        return thingsModelMapper.selectThingsModelList(thingsModel);
    }

    /**
     * 查询物模型对应分享设备用户权限列表
     *
     * @param productId 产品编号
     * @return 物模型
     */
    @Override
    public List<ThingsModelPerm> selectThingsModelPermList(Long productId) {
        return thingsModelMapper.selectThingsModelPermList(productId, SecurityUtils.getLanguage());
    }



    /**
     * 新增物模型
     *
     * @param thingsModel 物模型
     * @return 结果
     */
    @Override
    public int insertThingsModel(ThingsModel thingsModel) {
        ThingsModel input = new ThingsModel();
        input.setProductId(thingsModel.getProductId());
        input.setLanguage(SecurityUtils.getLanguage());
        List<ThingsModel> list = thingsModelMapper.selectThingsModelList(input);
        Boolean isRepeat = list.stream().anyMatch(x -> x.getIdentifier().equals(thingsModel.getIdentifier()));
        if (!isRepeat) {
            SysUser user = getLoginUser().getUser();
            if (null != user.getDeptId()) {
                thingsModel.setTenantId(user.getDept().getDeptUserId());
                thingsModel.setTenantName(user.getDept().getDeptUserName());
            } else {
                thingsModel.setTenantId(user.getUserId());
                thingsModel.setTenantName(user.getUserName());
            }
            thingsModel.setCreateTime(DateUtils.getNowDate());
            int result = thingsModelMapper.insertThingsModel(thingsModel);
            // 更新redis缓存
            itslCache.setCacheThingsModelByProductId(thingsModel.getProductId());
            if (result > 0) {
                // 更新场景管理变量
                List<SceneModelDevice> sceneModelDeviceList = sceneModelDeviceMapper.listDeviceByProductId(thingsModel.getProductId());
                if (org.apache.commons.collections4.CollectionUtils.isEmpty(sceneModelDeviceList)) {
                    return result;
                }
                List<ThingsModelDTO> thingsModelDTOList = this.changeObjectOrArray(thingsModel);
                for (SceneModelDevice sceneModelDevice : sceneModelDeviceList) {
                    List<SceneModelData> modelDataList = getSceneModelData(sceneModelDevice, thingsModelDTOList);
                    sceneModelDataMapper.insertBatchSceneModelData(modelDataList);
                }
            }
            return result;
        }
        return 2;
    }

    private static List<SceneModelData> getSceneModelData(SceneModelDevice sceneModelDevice, List<ThingsModelDTO> thingsModelDTOList) {
        List<SceneModelData> modelDataList = new ArrayList<>();
        for (ThingsModelDTO model : thingsModelDTOList) {
            SceneModelData addSceneModelData = new SceneModelData();
            addSceneModelData.setSceneModelId(sceneModelDevice.getSceneModelId());
            addSceneModelData.setSceneModelDeviceId(sceneModelDevice.getId());
            addSceneModelData.setVariableType(SceneModelVariableTypeEnum.THINGS_MODEL.getType());
            addSceneModelData.setDatasourceId(model.getModelId());
            addSceneModelData.setEnable(1);
            addSceneModelData.setIdentifier(model.getIdentifier());
            addSceneModelData.setSourceName(model.getModelName());
            addSceneModelData.setType(model.getType());
            modelDataList.add(addSceneModelData);
        }
        return modelDataList;
    }

    /**
     * 导入通用物模型
     *
     * @param input
     * @return
     */
    @Override
    public int importByTemplateIds(ImportThingsModelInput input) {
        ThingsModel inputParameter = new ThingsModel();
        inputParameter.setLanguage(SecurityUtils.getLanguage());
        inputParameter.setProductId(input.getProductId());
        List<ThingsModel> dbList = thingsModelMapper.selectThingsModelList(inputParameter);

        SysUser user = getLoginUser().getUser();
        // 根据ID集合获取通用物模型列表
        List<ThingsModelTemplate> templateList = thingsModelTemplateMapper.selectThingsModelTemplateByTemplateIds(input.getTemplateIds(), SecurityUtils.getLanguage());
        //转换为产品物模型，并批量插入
        List<ThingsModel> list = new ArrayList<>();
        int repeatCount = 0;
        for (ThingsModelTemplate template : templateList) {
            ThingsModel thingsModel = new ThingsModel();
            BeanUtils.copyProperties(template, thingsModel);
            thingsModel.setTenantId(user.getUserId());
            thingsModel.setTenantName(user.getUserName());
            thingsModel.setCreateTime(DateUtils.getNowDate());
            thingsModel.setProductId(input.getProductId());
            thingsModel.setProductName(input.getProductName());
            thingsModel.setModelId(template.getTemplateId());
            thingsModel.setModelName(template.getTemplateName());
            thingsModel.setIsReadonly(template.getIsReadonly());
            thingsModel.setIsMonitor(template.getIsMonitor());
            thingsModel.setIsChart(template.getIsChart());
            thingsModel.setIsHistory(template.getIsHistory());
            thingsModel.setModelOrder(template.getModelOrder());
            Boolean isRepeat = dbList.stream().anyMatch(x -> x.getIdentifier().equals(thingsModel.getIdentifier()));
            if (isRepeat) {
                repeatCount = repeatCount + 1;
            } else {
                list.add(thingsModel);
            }
        }
        if (list.size() > 0) {
            int result = thingsModelMapper.insertBatchThingsModel(list);
            if (result > 0) {
                // 更新场景管理变量
                List<SceneModelDevice> sceneModelDeviceList = sceneModelDeviceMapper.listDeviceByProductId(input.getProductId());
                if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(sceneModelDeviceList)) {
                    List<SceneModelData> sceneModelDataList = new ArrayList<>();
                    for (ThingsModel thingsModel : list) {
                        List<ThingsModelDTO> thingsModelDTOList = this.changeObjectOrArray(thingsModel);
                        for (SceneModelDevice sceneModelDevice : sceneModelDeviceList) {
                            sceneModelDataList.addAll(getSceneModelData(sceneModelDevice, thingsModelDTOList));
                        }
                    }
                    sceneModelDataMapper.insertBatchSceneModelData(sceneModelDataList);
                }
            }
            //更新redis缓存
           itslCache.setCacheThingsModelByProductId(input.getProductId());
        }
        return repeatCount;
    }

    /**
     * 修改物模型
     *
     * @param thingsModel 物模型
     * @return 结果
     */
    @Override
    public int updateThingsModel(ThingsModel thingsModel) {
        // 校验场景是否被应用到计算公式
        int count = sceneModelDataMapper.checkIsApplyAliasFormule(thingsModel.getModelId(), SceneModelVariableTypeEnum.THINGS_MODEL.getType());
        if (count > 0) {
            throw new ServiceException("当前物模型被引用到场景运算型变量的计算公式中，无法修改，请先删除引用关系后再执行修改操作！");
        }
        ThingsModel input = new ThingsModel();
        input.setProductId(thingsModel.getProductId());
        input.setLanguage(SecurityUtils.getLanguage());
        List<ThingsModel> list = thingsModelMapper.selectThingsModelList(input);
        Boolean isRepeat = list.stream().anyMatch(x -> x.getIdentifier().equals(thingsModel.getIdentifier()) && x.getModelId().longValue() != thingsModel.getModelId());
        if (!isRepeat) {
            thingsModel.setUpdateTime(DateUtils.getNowDate());
            int result = thingsModelMapper.updateThingsModel(thingsModel);
            // 更新redis缓存
            itslCache.setCacheThingsModelByProductId(thingsModel.getProductId());
            if (result > 0) {
                List<SceneModelData> sceneModelDataList = sceneModelDataMapper.selectSceneDeviceThingsModelList(thingsModel.getModelId());
                if (org.apache.commons.collections4.CollectionUtils.isEmpty(sceneModelDataList)) {
                    return result;
                }
                sceneModelDataMapper.deleteThingsModelByDatasourceId(new Long[]{thingsModel.getModelId()});
                List<ThingsModelDTO> thingsModelDTOList = this.changeObjectOrArray(thingsModel);
                for (SceneModelData sceneModelData : sceneModelDataList) {
                    SceneModelDevice sceneModelDevice = new SceneModelDevice();
                    sceneModelDevice.setSceneModelId(sceneModelData.getSceneModelId());
                    sceneModelDevice.setId(sceneModelData.getSceneModelDeviceId());
                    List<SceneModelData> modelDataList = getSceneModelData(sceneModelDevice, thingsModelDTOList);
                    sceneModelDataMapper.insertBatchSceneModelData(modelDataList);
                }
            }
            return result;
        }
        return 2;
    }

    /**
     * 批量删除物模型
     *
     * @param modelIds 需要删除的物模型主键
     * @return 结果
     */
    @Override
    public int deleteThingsModelByModelIds(Long[] modelIds) {
        // 校验场景是否被应用到计算公式
        int count = sceneModelDataMapper.checkIsApplyAliasFormule(modelIds[0], SceneModelVariableTypeEnum.THINGS_MODEL.getType());
        if (count > 0) {
            throw new ServiceException("当前物模型被引用到场景运算型变量的计算公式中，无法删除，请先删除引用关系后再执行删除操作！");
        }
        ThingsModel thingsModel = thingsModelMapper.selectThingsModelByModelId(modelIds[0], SecurityUtils.getLanguage());
        int result = thingsModelMapper.deleteThingsModelByModelIds(modelIds);
        if (result > 0) {
            sceneModelDataMapper.deleteThingsModelByDatasourceId(modelIds);
        }
        // 更新redis缓存
        itslCache.setCacheThingsModelByProductId(thingsModel.getProductId());

        return result;
    }

    /**
     * 删除物模型信息
     *
     * @param modelId 物模型主键
     * @return 结果
     */
    @Override
    public int deleteThingsModelByModelId(Long modelId) {
        ThingsModel thingsModel = thingsModelMapper.selectThingsModelByModelId(modelId, SecurityUtils.getLanguage());
        int result = thingsModelMapper.deleteThingsModelByModelId(modelId);
        // 更新redis缓存
        itslCache.setCacheThingsModelByProductId(thingsModel.getProductId());
        return result;
    }

    /**
     * 获取单个物模型获取
     *
     * @param productId
     * @param identify
     * @return
     */
    @Override
    public ThingsModelValueItem getSingleThingModels(Long productId, String identify) {
        ThingsModelValueItem item = itslCache.getSingleThingModels(productId, identify);
        if (!Objects.isNull(item)) {
            return item;
        }
        ThingsModel thingsModel = new ThingsModel();
        thingsModel.setIdentifier(identify);
        thingsModel.setProductId(productId);
        ThingsModel selectModel = this.selectSingleThingsModel(thingsModel);
        if (!Objects.isNull(selectModel)){
            item = new ThingsModelValueItem();
            BeanUtils.copyProperties(selectModel, item);
            item.setId(selectModel.getIdentifier());
            item.setName(selectModel.getModelName());
            item.setDatatype(JSONObject.parseObject(selectModel.getSpecs(), Datatype.class));
            item.setOrder(selectModel.getModelOrder());
            item.setFormula(selectModel.getFormula());
            itslCache.setCacheThingsModelByProductId(productId);
        }
        return item;
    }


    /**
     * 导入采集点数据
     *
     * @param lists       数据列表
     * @return 结果
     */
    public String importData(List<ThingsModel> lists, Long productId) {
        if (null == productId || CollectionUtils.isEmpty(lists)) {
            throw new ServiceException("导入数据异常");
        }
        int success = 0;
        int failure = 0;
        StringBuilder succSb = new StringBuilder();
        StringBuilder failSb = new StringBuilder();
        Product product = productMapper.selectProductByProductId(productId);
        for (ThingsModel model : lists) {
            try {
                model.setProductId(product.getProductId());
                model.setProductName(product.getProductName());
                //处理数据定义
                this.parseSpecs(model);
                this.importData(model);
                success++;
                succSb.append("<br/>").append(success).append(",采集点: ").append(model.getModelName());
            } catch (Exception e) {
                log.error("导入错误：", e);
                failure++;
                failSb.append("<br/>").append(failure).append(",采集点: ").append(model.getModelName()).append("导入失败");
            }
        }
        if (failure > 0) {
            throw new ServiceException(failSb.toString());
        }
        if (success >0){
            // 更新redis缓存
            itslCache.setCacheThingsModelByProductId(productId);
        }
        return succSb.toString();
    }


    /**
     * 导入单个物模型
     */
    private void importData(ThingsModel thingsModel){
        ThingsModel input=new ThingsModel();
        input.setProductId(thingsModel.getProductId());
        input.setLanguage(SecurityUtils.getLanguage());
        SysUser user = getLoginUser().getUser();
        if (null != user.getDeptId()) {
            thingsModel.setTenantId(user.getDept().getDeptUserId());
            thingsModel.setTenantName(user.getDept().getDeptUserName());
        } else {
            thingsModel.setTenantId(user.getUserId());
            thingsModel.setTenantName(user.getUserName());
        }
        thingsModel.setCreateTime(DateUtils.getNowDate());
        thingsModelMapper.insertThingsModel(thingsModel);
    }

    private void parseSpecs(ThingsModel model) {
        JSONObject specs = new JSONObject();
        String datatype = model.getDatatype();
        String limitValue = model.getLimitValue();
        if (limitValue != null && !"".equals(limitValue)) {
            String[] values = limitValue.split("/");
            switch (datatype) {
                case "integer":
                    specs.put("max", values[1]);
                    specs.put("min", values[0]);
                    specs.put("type", datatype);
                    specs.put("unit", model.getUnit());
                    specs.put("step", 0);
                    break;
                case "bool":
                    specs.put("type", datatype);
                    specs.put("trueText", values[1]);
                    specs.put("falseText", values[0]);
                    break;
                case "enum":
                    List<EnumClass> list = new ArrayList<>();
                    for (String value : values) {
                        String[] params = value.split(":");
                        EnumClass enumCls = new EnumClass();
                        enumCls.setText(params[1]);
                        enumCls.setValue(params[0]);
                        list.add(enumCls);
                    }
                    specs.put("type", datatype);
                    specs.put("enumList", list);
                    break;
            }
            model.setSpecs(specs.toJSONString());
        }
    }

    /**
     * 根据产品id删除 产品物模型以及物模型缓存
     *
     * @param productId
     */
    @Override
    public void deleteProductThingsModelAndCacheByProductId(Long productId) {
        thingsModelMapper.deleteThingsModelByProductId(productId);
        String cacheKey = RedisKeyBuilder.buildTSLCacheKey(productId);
        redisCache.deleteObject(cacheKey);
    }


    @Override
    public List<ThingsModelSimVO> listSimByProductIds(List<Long> productIdList) {
        return thingsModelMapper.listSimByProductIds(productIdList, SecurityUtils.getLanguage());
    }

    @Override
    public String getSpecsByProductIdAndIdentifier(Long productId, String identifier) {
        return thingsModelMapper.getSpecsByProductIdAndIdentifier(productId, identifier);
    }

    /**
     * 获取modbus配置可选择物模型
     *
     * @param productId
     * @return
     */
    @Override
    public List<ModbusAndThingsVO> getModbusConfigUnSelectThingsModel(Long productId) {
        List<ModbusAndThingsVO> modbusAndThingsVOList = thingsModelMapper.getModbusConfigUnSelectThingsModel(productId, SecurityUtils.getLanguage());
        for (ModbusAndThingsVO thingsVO : modbusAndThingsVOList) {
            thingsVO.setIsSelectIo(false);
            thingsVO.setIsSelectData(false);
            if (thingsVO.getIsSelect()) {
                if (thingsVO.getDataType().equals("bool")) {
                    thingsVO.setIsSelectIo(true);
                } else {
                    thingsVO.setIsSelectData(true);
                }
            }
        }
        return modbusAndThingsVOList;
    }

    @Override
    public List<ThingsModelDTO> changeObjectOrArrayByProductId(Long productId) {
        ThingsModel queryThingsModel = new ThingsModel();
        queryThingsModel.setProductId(productId);
        // 因为缓存没有存modelId，这里需要用到，所以先从数据库查
        List<ThingsModel> thingsModelList = thingsModelMapper.selectThingsModelList(queryThingsModel);
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(thingsModelList)) {
            return new ArrayList<>();
        }
        List<ThingsModelDTO> resultList = new ArrayList<>();
        for (ThingsModel thingsModel : thingsModelList) {
            resultList.addAll(this.changeObjectOrArray(thingsModel));
        }
        return resultList;
    }

    @Override
    public List<ThingsModelDTO> changeObjectOrArray(ThingsModel thingsModel) {
        List<ThingsModelDTO> resultList = new ArrayList<>();
        Datatype datatype = JSONObject.parseObject(thingsModel.getSpecs(), Datatype.class);
        if ("array".equals(datatype.getType()) && "object".equals(datatype.getArrayType())) {
            List<ThingsModelValueItem> params = datatype.getParams();
            for (int i = 0; i < datatype.getArrayCount(); i++) {
                for (ThingsModelValueItem param : params) {
                    ThingsModelDTO arrayObjectThingsModel = new ThingsModelDTO();
                    arrayObjectThingsModel.setModelId(thingsModel.getModelId());
                    if (i < 10) {
                        arrayObjectThingsModel.setIdentifier("array_0" + i + "_" + param.getId());
                    } else {
                        arrayObjectThingsModel.setIdentifier("array_" + i + "_" + param.getId());
                    }
                    arrayObjectThingsModel.setModelName(thingsModel.getModelName() + (i + 1) + "_" + param.getName());
                    arrayObjectThingsModel.setType(thingsModel.getType());
                    arrayObjectThingsModel.setUnit(param.getDatatype().getUnit());
                    arrayObjectThingsModel.setIsReadonly(param.getIsReadonly());
                    arrayObjectThingsModel.setDatatype(param.getDatatype());
                    arrayObjectThingsModel.setModelOrder(thingsModel.getModelOrder());
                    resultList.add(arrayObjectThingsModel);
                }
            }
        } else if ("array".equals(datatype.getType())) {
            for (int i = 0; i < datatype.getArrayCount(); i++) {
                ThingsModelDTO arrayThingsModel = new ThingsModelDTO();
                arrayThingsModel.setModelId(thingsModel.getModelId());
                if (i < 10) {
                    arrayThingsModel.setIdentifier("array_0" + i + "_" + thingsModel.getIdentifier());
                } else {
                    arrayThingsModel.setIdentifier("array_" + i + "_" + thingsModel.getIdentifier());
                }
                arrayThingsModel.setModelName(thingsModel.getModelName() + (i + 1));
                arrayThingsModel.setUnit(datatype.getUnit());
                arrayThingsModel.setType(thingsModel.getType());
                arrayThingsModel.setIsReadonly(thingsModel.getIsReadonly());
                arrayThingsModel.setDatatype(datatype);
                arrayThingsModel.setModelOrder(thingsModel.getModelOrder());
                resultList.add(arrayThingsModel);
            }
        } else if ("object".equals(datatype.getType())) {
            for (ThingsModelValueItem objectThingsModel : datatype.getParams()) {
                ThingsModelDTO newObjectThingsModel = new ThingsModelDTO();
                newObjectThingsModel.setModelId(thingsModel.getModelId());
                newObjectThingsModel.setModelName(thingsModel.getModelName() + "_" + objectThingsModel.getName());
                newObjectThingsModel.setType(thingsModel.getType());
                newObjectThingsModel.setIdentifier(objectThingsModel.getId());
                newObjectThingsModel.setUnit(objectThingsModel.getDatatype().getUnit());
                newObjectThingsModel.setIsReadonly(objectThingsModel.getIsReadonly());
                newObjectThingsModel.setDatatype(objectThingsModel.getDatatype());
                newObjectThingsModel.setModelOrder(thingsModel.getModelOrder());
                resultList.add(newObjectThingsModel);
            }
        } else {
            ThingsModelDTO otherThingsModel = new ThingsModelDTO();
            otherThingsModel.setModelId(thingsModel.getModelId());
            otherThingsModel.setModelName(thingsModel.getModelName());
            otherThingsModel.setUnit(datatype.getUnit());
            otherThingsModel.setType(thingsModel.getType());
            otherThingsModel.setIdentifier(thingsModel.getIdentifier());
            otherThingsModel.setIsReadonly(thingsModel.getIsReadonly());
            otherThingsModel.setDatatype(datatype);
            otherThingsModel.setModelOrder(thingsModel.getModelOrder());
            resultList.add(otherThingsModel);
        }
        return resultList;
    }

    @Override
    public IdentifierVO revertObjectOrArrayIdentifier(String identifier) {
        IdentifierVO identifierVO = new IdentifierVO();
        if (identifier.startsWith("array_")) {
            int i1 = org.apache.commons.lang3.StringUtils.ordinalIndexOf(identifier, "_", 1);
            int i2 = org.apache.commons.lang3.StringUtils.ordinalIndexOf(identifier, "_", 2);
            identifierVO.setIdentifier(identifier.substring(i2 + 1));
            identifierVO.setIndex(Integer.parseInt(identifier.substring(++i1, i2)));
        } else {
            identifierVO.setIdentifier(identifier);
        }
        return identifierVO;
    }

    @Override
    public String getCacheIdentifierValue(Long productId, String serialNumber, String identifier) {
        String value = "";
        if (identifier.startsWith("array_")) {
            IdentifierVO identifierVO = this.revertObjectOrArrayIdentifier(identifier);
            String cacheValue = thingModelCache.getCacheIdentifierValue(productId, serialNumber, identifierVO.getIdentifier());
            List<String> valueList = StringUtils.str2List(cacheValue, ",", false, false);
            value = null != valueList.get(identifierVO.getIndex()) ? valueList.get(identifierVO.getIndex()) : "";
        } else {
            value = thingModelCache.getCacheIdentifierValue(productId, serialNumber, identifier);

        }
        return value;
    }

    @Override
    public List<ThingsModelDTO> getCacheAndHandleArrayByProductId(Long productId, String serialNumber) {
        if (null == productId) {
            return new ArrayList<>();
        }
        Map<String, ThingsModelValueItem> map = itslCache.getCacheThMapByProductId(productId);
        List<ThingsModelDTO> thingsModelDTOList = new ArrayList<>();
        for (Map.Entry<String, ThingsModelValueItem> entry : map.entrySet()) {
            ThingsModel thingsModel = this.propertyDtoChangeThingsModel(entry.getValue());
            thingsModelDTOList.addAll(this.changeObjectOrArray(thingsModel));
        }

        thingsModelDTOList.sort(Comparator.comparing(ThingsModelDTO::getModelOrder));
        if (StringUtils.isEmpty(serialNumber)) {
            return thingsModelDTOList;
        }
        // 获取缓存值
        List<ValueItem> thingsModelValueItems = thingModelCache.getCacheDeviceStatus(productId, serialNumber);
        Map<String, ValueItem> thingsModelValueItemMap = thingsModelValueItems.stream().collect(Collectors.toMap(ValueItem::getId, Function.identity()));
        for (ThingsModelDTO thingsModelDTO : thingsModelDTOList) {
            String identifier = thingsModelDTO.getIdentifier();
            IdentifierVO identifierVO = this.revertObjectOrArrayIdentifier(identifier);
            ValueItem thingsModelValueItem = thingsModelValueItemMap.get(identifierVO.getIdentifier());
            if (null == thingsModelValueItem) {
                thingsModelDTO.setValue("");
                continue;
            }
            if (identifier.startsWith("array")) {
                String cacheValue = thingsModelValueItem.getValue();
                List<String> valueList = StringUtils.str2List(cacheValue, ",", false, false);
                String value = null != valueList.get(identifierVO.getIndex()) ? valueList.get(identifierVO.getIndex()) : "";
                thingsModelDTO.setValue(value);
            } else {
                thingsModelDTO.setValue(thingsModelValueItem.getValue());
            }
            if (!Objects.isNull(thingsModelValueItem.getTs())) {
                thingsModelDTO.setTs(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, thingsModelValueItem.getTs()));
            }
        }
        return thingsModelDTOList;
    }

    @Override
    public List<ThingsModel> selectThingsModelListByModelIds(List<Long> modelIdList) {
        return thingsModelMapper.selectThingsModelListByModelIds(modelIdList);
    }

    /**
     * 根据设备编号获取读写物模型
     * @param deviceId
     * @return
     */
    @Override
    public List<ThingsModel> selectThingsModelBySerialNumber(Long deviceId){
        return  thingsModelMapper.selectThingsModelBySerialNumber(deviceId,SecurityUtils.getLanguage());
    }

    private ThingsModel propertyDtoChangeThingsModel(ThingsModelValueItem valueItem) {
        ThingsModel thingsModel = new ThingsModel();
        thingsModel.setIdentifier(valueItem.getId());
        thingsModel.setModelName(valueItem.getName());
        thingsModel.setIsChart(valueItem.getIsChart());
        thingsModel.setIsHistory(valueItem.getIsHistory());
        thingsModel.setIsMonitor(valueItem.getIsMonitor());
        thingsModel.setIsReadonly(valueItem.getIsReadonly());
        thingsModel.setModelOrder(valueItem.getOrder());
        thingsModel.setSpecs(JSON.toJSONString(valueItem.getDatatype()));
        thingsModel.setType(valueItem.getType());
        thingsModel.setModelId(valueItem.getModelId());
        return thingsModel;
    }

}
