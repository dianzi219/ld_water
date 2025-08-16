package com.fastbee.iot.cache;

import com.fastbee.iot.model.ThingsModels.ThingsModelValueItem;

import java.util.List;
import java.util.Map;

/**
 * 物模型缓存
 * @author gsb
 * @date 2024/6/12 15:57
 */
public interface ITSLCache {


    /**
     * 获取JSON物模型 -组装的方式是为了兼容前端数据结构
     * @param productId 产品id
     * @return
     * <p>
     * {
     *   "functions": [
     *   ],
     *   "events": [
     *   ],
     *   "properties": [
     *     {
     *       "datatype": {
     *         "max": 100,
     *         "min": 1,
     *         "step": 1,
     *         "type": "integer",
     *         "unit": "流明"
     *       },
     *       "id": "light",
     *       "isChart": 1,
     *       "isHistory": 1,
     *       "isMonitor": 1,
     *       "isReadonly": 1,
     *       "name": "光强",
     *       "order": 0,
     *       "regId": "light",
     *       "type": 1
     *     }
     *   ]
     * }
     * </p>
     *
     */
    String getCacheThingsModelByProductId(Long productId);


    /**
     * 缓存物模型
     *
     * @param productId 产品id
     * @return <p>
     * {\"functions\":[],\"events\":[],\"properties\":[{\"datatype\":{\"max\":100,\"min\":1
     * ,\"step\":1,\"type\":\"integer\",\"unit\":\"流明\"},
     * \"id\":\"light\",\"isChart\":1,\"isHistory\":1,\"isMonitor\":1,\"isReadonly\":1,
     * \"name\":\"光强\",\"order\":0,\"regId\":\"light\",\"type\":1}]}
     * </p>
     */
    public String setCacheThingsModelByProductId(Long productId);

    /**
     * 获取map物模型 --返回 Map<String, PropertyDto>
     * @param productId
     * @return
     *  <p>
     *      返回key-value
     *      key: 物模型标识符
     *      value：PropertyDto
     *  </p>
     */
    Map<String, ThingsModelValueItem> getCacheThMapByProductId(Long productId);


    /**
     * 获取List集合物模型
     */
    List<ThingsModelValueItem> getThingsModelList(Long productId);

    /**
     * 缓存物模型-返回Map
     * @param productId
     *  <p>
     *      返回key-value
     *      key: 物模型标识符
     *      value：PropertyDto
     *  </p>
     */
    public Map<String, ThingsModelValueItem> setCacheThMapByProductId(Long productId);

    /**
     * 获取单个物模型缓存
     * @param productId 产品id
     * @param identify 标识符
     * @return PropertyDto
     */
    public ThingsModelValueItem getSingleThingModels(Long productId, String identify);

}
