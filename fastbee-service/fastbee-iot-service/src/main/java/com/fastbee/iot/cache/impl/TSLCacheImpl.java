package com.fastbee.iot.cache.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fastbee.common.core.redis.RedisCache;
import com.fastbee.common.core.redis.RedisKeyBuilder;
import com.fastbee.common.enums.ThingsModelType;
import com.fastbee.common.utils.SecurityUtils;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.iot.cache.ITSLCache;
import com.fastbee.iot.domain.ModbusConfig;
import com.fastbee.iot.domain.Product;
import com.fastbee.iot.domain.ThingsModel;
import com.fastbee.iot.mapper.ProductMapper;
import com.fastbee.iot.model.ThingsModelItem.Datatype;
import com.fastbee.iot.model.ThingsModels.IdentifierVO;
import com.fastbee.iot.model.ThingsModels.PropertyDto;
import com.fastbee.iot.model.ThingsModels.ThingsModelValueItem;
import com.fastbee.iot.service.IModbusConfigService;
import com.fastbee.iot.service.IThingsModelService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.fastbee.common.constant.Constants.EN_US;
import static com.fastbee.common.constant.Constants.ZH_CN;

/**
 * 物模型缓存
 *
 * @author gsb
 * @date 2024/6/12 15:58
 */
@Service
public class TSLCacheImpl implements ITSLCache {

    @Resource
    private RedisCache redisCache;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private IThingsModelService thingsModelService;
    @Resource
    private IModbusConfigService modbusConfigService;

    /**
     * 根据产品ID获取JSON物模型 -组装的方式是为了兼容前端数据结构
     *
     * @param productId
     * @return <p>
     * {
     * "functions": [
     * ],
     * "events": [
     * ],
     * "properties": [
     * {
     * "datatype": {
     * "max": 100,
     * "min": 1,
     * "step": 1,
     * "type": "integer",
     * "unit": "流明"
     * },
     * "id": "light",
     * "isChart": 1,
     * "isHistory": 1,
     * "isMonitor": 1,
     * "isReadonly": 1,
     * "name": "光强",
     * "order": 0,
     * "regId": "light",
     * "type": 1
     * }
     * ]
     * }
     * </p>
     */
    @Override
    public String getCacheThingsModelByProductId(Long productId) {
        assert !Objects.isNull(productId) : "产品id为空";
        // redis获取物模型
        Map<String, Object> map = redisCache.getCacheMap(RedisKeyBuilder.buildTSLCacheKey(productId));
        if (!CollectionUtils.isEmpty(map)) {
            //兼容原页面物模型的数据格式
            Map<String, List<PropertyDto>> listMap = map.values().stream().map(v -> JSON.parseObject(v.toString(), PropertyDto.class))
                    .collect(Collectors.groupingBy(dto -> ThingsModelType.getName(dto.getType())));
            if (listMap != null) {
                for (List<PropertyDto> dtoList : listMap.values()) {
                    for (PropertyDto dto : dtoList) {
                        convertModelName(dto, SecurityUtils.getLanguage());
                    }
                }
            }
            return JSON.toJSONString(listMap);
        }
        return setCacheThingsModelByProductId(productId);
    }


    /**
     * 缓存物模型 -返回JSON字符串
     *
     * @param productId
     * @return <p>
     * {\"functions\":[],\"events\":[],\"properties\":[{\"datatype\":{\"max\":100,\"min\":1
     * ,\"step\":1,\"type\":\"integer\",\"unit\":\"流明\"},
     * \"id\":\"light\",\"isChart\":1,\"isHistory\":1,\"isMonitor\":1,\"isReadonly\":1,
     * \"name\":\"光强\",\"order\":0,\"regId\":\"light\",\"type\":1}]}
     * </p>
     */
    public String setCacheThingsModelByProductId(Long productId) {
        Map<String, String> thingsModelMap = setThingModelAndModbusConfig(productId);
        /*组装成原格式数据*/
        Map<String, List<ThingsModelValueItem>> result = thingsModelMap.values().stream().map(x -> JSON.parseObject(x, ThingsModelValueItem.class))
                .collect(Collectors.groupingBy(dto -> ThingsModelType.getName(dto.getType())));
        if (result != null) {
            for (List<ThingsModelValueItem> itemList : result.values()) {
                for (ThingsModelValueItem item : itemList) {
                    convertModelName(item, SecurityUtils.getLanguage());
                }
            }
        }
        String jsonString = JSON.toJSONString(result);
        Product product = new Product();
        product.setProductId(productId);
        product.setThingsModelsJson(jsonString);
        productMapper.updateThingsModelJson(product);
        return jsonString;
    }



    /**
     * 获取map物模型
     * @param productId
     * @return
     *  <p>
     *      返回key-value
     *      key: 物模型标识符
     *      value：PropertyDto
     *  </p>
     */
    public Map<String, ThingsModelValueItem> getCacheThMapByProductId(Long productId) {
        assert !Objects.isNull(productId) : "产品id为空";
        // redis获取物模型
        Map<String, Object> map = redisCache.getCacheMap(RedisKeyBuilder.buildTSLCacheKey(productId));
        if (!CollectionUtils.isEmpty(map)) {
            Map<String, ThingsModelValueItem> itemMap = map.values().stream().map(v -> JSON.parseObject(v.toString(), ThingsModelValueItem.class)).collect(
                    Collectors.toMap(ThingsModelValueItem::getId, Function.identity()));
            for (ThingsModelValueItem item : itemMap.values()) {
                convertModelName(item, SecurityUtils.getLanguage());
            }
            return itemMap;
        }
        return setCacheThMapByProductId(productId);
    }

    /**
     * 获取List集合物模型
     */
    @Override
    public List<ThingsModelValueItem> getThingsModelList(Long productId){
        Map<String, ThingsModelValueItem> thingsModelMap = this.getCacheThMapByProductId(productId);
        if (thingsModelMap != null) {
            for (ThingsModelValueItem item : thingsModelMap.values()) {
                convertModelName(item, SecurityUtils.getLanguage());
            }
        }
        return new ArrayList<>(thingsModelMap.values());
    }


    /**
     * 缓存物模型-返回Map
     * @param productId
     *  <p>
     *      返回key-value
     *      key: 物模型标识符
     *      value：PropertyDto
     *  </p>
     */
    public Map<String, ThingsModelValueItem> setCacheThMapByProductId(Long productId) {
        Map<String, String> thingsModelMap = setThingModelAndModbusConfig(productId);
        Map<String, ThingsModelValueItem> itemMap = thingsModelMap.values().stream().map(value -> JSON.parseObject(value, ThingsModelValueItem.class)).collect(Collectors.toMap(ThingsModelValueItem::getId, Function.identity()));
        if (itemMap != null) {
            for (ThingsModelValueItem item : itemMap.values()) {
                convertModelName(item, SecurityUtils.getLanguage());
            }
        }
        return itemMap;
    }

    /**
     * 获取单个物模型缓存值
     * @param productId 产品id
     * @param identify 标识符
     * @return PropertyDto
     */
    @Override
    public ThingsModelValueItem getSingleThingModels(Long productId, String identify){
        String cacheKey = RedisKeyBuilder.buildTSLCacheKey(productId);
        String itemStr;
        // 物模型缓存有变动，需要特殊处理一下
        IdentifierVO identifierVO = thingsModelService.revertObjectOrArrayIdentifier(identify);
        // 数组或数组对象
        String identifier = identifierVO.getIdentifier();
        if (null != identifierVO.getIndex()) {
            itemStr = redisCache.getCacheMapValue(cacheKey, identifier);
            if (StringUtils.isEmpty(itemStr) && identifier.contains("_")) {
                String substring = identifier.substring(0, identifier.indexOf("_"));
                itemStr = redisCache.getCacheMapValue(cacheKey, substring);
            }
        } else {
            itemStr = redisCache.getCacheMapValue(cacheKey, identifier);
            if (StringUtils.isEmpty(itemStr) && identifier.contains("_")) {
                String substring = identifier.substring(0, identifier.indexOf("_"));
                itemStr = redisCache.getCacheMapValue(cacheKey, substring);
            }
        }
        if (StringUtils.isEmpty(itemStr)) {return null;}
        ThingsModelValueItem item = JSONObject.parseObject(itemStr,ThingsModelValueItem.class);
        convertModelName(item, SecurityUtils.getLanguage());
        // 处理数组、对象、数组对象
        if (identifier.equals(item.getId())) {
            return item;
        }
        Datatype datatype = item.getDatatype();
        List<ThingsModelValueItem> params = datatype.getParams();
        ThingsModelValueItem resultItem = params.stream().filter(t -> identifier.equals(t.getId())).findFirst().orElse(null);
        if (Objects.isNull(resultItem)) {
            return null;
        }
        resultItem.setType(item.getType());
        return resultItem;
    }


    /**
     * 整合物模型 & 整合modbus配置参数 & redis缓存
     *
     * @param productId
     */
    private Map<String, String> setThingModelAndModbusConfig(Long productId) {
        // 数据库查询物模型集合
        ThingsModel model = new ThingsModel();
        model.setProductId(productId);
        model.setLimitValue(SecurityUtils.getLanguage());
        List<ThingsModel> thingsModels = thingsModelService.selectThingsModelList(model);
        //modbus配置参数整合到物模型
        ModbusConfig config = new ModbusConfig();
        config.setProductId(productId);
        List<ModbusConfig> modbusConfigList = modbusConfigService.selectShortListByProductId(config);
        if (!CollectionUtils.isEmpty(modbusConfigList)) {
            Map<String, ModbusConfig> modbusMap = modbusConfigList.stream().collect(Collectors.toMap(ModbusConfig::getIdentifier, Function.identity()));
            for (ThingsModel thingsModel : thingsModels) {
                if (modbusMap.containsKey(thingsModel.getIdentifier())) {
                    ModbusConfig modbusConfig = modbusMap.get(thingsModel.getIdentifier());
                    thingsModel.setModbusConfig(modbusConfig);
                }
            }
        }
        //List -> MAP
        Map<String, String> thingsModelMap = thingsModels.stream().collect(Collectors.toMap(ThingsModel::getIdentifier,
                thingsModel -> {
                    //转换数据，减少不必要数据
                    ThingsModelValueItem dto = new ThingsModelValueItem();
                    BeanUtils.copyProperties(thingsModel, dto);
                    dto.setDatatype(JSONObject.parseObject(thingsModel.getSpecs(), Datatype.class))
                            .setId(thingsModel.getIdentifier())
                            .setName(thingsModel.getModelName())
                            .setName_zh_CN(thingsModel.getModelName_zh_CN())
                            .setName_en_US(thingsModel.getModelName_en_US())
                            .setOrder(thingsModel.getModelOrder())
                            .setModelId(thingsModel.getModelId())
                            .setConfig(thingsModel.getModbusConfig());
                    return JSONObject.toJSONString(dto);
                }));

        /*缓存到redis*/
        String cacheKey = RedisKeyBuilder.buildTSLCacheKey(productId);
        //先删除缓存
        if (redisCache.containsKey(cacheKey)) {
            redisCache.deleteObject(cacheKey);
        }
        redisCache.hashPutAll(cacheKey, thingsModelMap);
        return thingsModelMap;
    }

    /**
     * 将物模型名称转换为对应语言
     * @param item
     * @param language
     */
    private static void convertModelName(ThingsModelValueItem item, String language) {
        switch (language) {
            case EN_US:
                if (StringUtils.isNotEmpty(item.getName_en_US())){
                    item.setName(item.getName_en_US());
                }
                break;
            case ZH_CN:
                if (StringUtils.isNotEmpty(item.getName_zh_CN())){
                    item.setName(item.getName_zh_CN());
                }
                break;
        }
    }

    /**
     * 将物模型名称转换为对应语言
     * @param item
     * @param language
     */
    private static void convertModelName(PropertyDto item, String language) {
        switch (language) {
            case EN_US:
                item.setName(item.getName_en_US());
                break;
            case ZH_CN:
                item.setName(item.getName_zh_CN());
                break;
        }
    }

}
