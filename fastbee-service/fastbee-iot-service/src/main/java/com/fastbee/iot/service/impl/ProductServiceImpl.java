package com.fastbee.iot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.common.core.redis.RedisCache;
import com.fastbee.common.core.redis.RedisKeyBuilder;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.iot.cache.ITSLCache;
import com.fastbee.iot.domain.Device;
import com.fastbee.iot.domain.Product;
import com.fastbee.iot.domain.ProductModbusJob;
import com.fastbee.iot.domain.ProductSubGateway;
import com.fastbee.iot.mapper.*;
import com.fastbee.iot.model.ChangeProductStatusModel;
import com.fastbee.iot.model.IdAndName;
import com.fastbee.iot.model.SceneDeviceBindVO;
import com.fastbee.iot.service.IProductService;
import org.springframework.cache.annotation.*;
import com.fastbee.iot.cache.ITSLValueCache;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 产品Service业务层处理
 *
 * @author kerwincui
 * @date 2021-12-16
 */
@Service
public class ProductServiceImpl implements IProductService {

    @Resource
    private ProductMapper productMapper;

    @Resource
    private ProductAuthorizeMapper productAuthorizeMapper;
    @Resource
    private RedisCache redisCache;
    @Resource
    private ToolServiceImpl toolService;
    @Resource
    private DeviceMapper deviceMapper;
    @Resource
    private SceneDeviceMapper sceneDeviceMapper;
    @Resource
    private ITSLValueCache thingModelCache;
    @Resource
    private ITSLCache itslCache;
    @Resource
    private ProductSubGatewayMapper productSubGatewayMapper;
    @Resource
    private ProductModbusJobMapper productModbusJobMapper;


    // select cache

    /**
     * 查询产品
     *
     * @param productId 产品主键
     * @return 产品
     */
    @Cacheable(cacheNames = "product", key = "#root.methodName + ':' + #productId", unless = "#result == null")
    @Override
    public Product selectProductByProductId(Long productId) {
        return productMapper.selectProductByProductId(productId);
    }

    /**
     * 获取产品下面的设备数量
     *
     * @param productId 产品ID
     * @return 结果
     */
    @Cacheable(cacheNames = "product", key = "#root.methodName + ':' + #productId", unless = "#result == null")
    @Override
    public int selectDeviceCountByProductId(Long productId) {
        return deviceMapper.selectDeviceCountByProductId(productId);
    }


    /**
     * 根据产品id获取协议编号
     *
     * @param productId 产品id
     * @return 协议编号
     */
    @Cacheable(cacheNames = "product", key = "#root.methodName + ':' + #productId", unless = "#result == null")
    @Override
    public String getProtocolByProductId(Long productId) {
        return productMapper.getProtocolByProductId(productId);
    }


    /**
     * 修改产品
     *
     * @param product 产品
     * @return 结果
     */
    @Caching(evict = {
            @CacheEvict(cacheNames = "product", key = "'selectProductByProductId:' + #product.productId"),
            @CacheEvict(cacheNames = "product", key = "'selectDeviceCountByProductId:' + #product.productId"),
            @CacheEvict(cacheNames = "product", key = "'getProtocolByProductId:' + #product.productId")
    })
    @Override
    public int updateProduct(Product product) {
        product.setUpdateTime(DateUtils.getNowDate());
        return productMapper.updateProduct(product);
    }

    /**
     * 批量删除产品
     *
     * @param productIds 需要删除的产品主键
     * @return 结果
     */
    @CacheEvict(cacheNames = "product", allEntries = true)
    @Override
    @Transactional
    public AjaxResult deleteProductByProductIds(Long[] productIds) {
        // 删除物模型JSON缓存
        for (int i = 0; i < productIds.length; i++) {
            String key = RedisKeyBuilder.buildTSLCacheKey(productIds[i]);
            redisCache.deleteObject(key);
        }
        // 产品下不能有固件
        int firmwareCount = productMapper.firmwareCountInProducts(productIds);
        if (firmwareCount > 0) {
            return AjaxResult.error("删除失败，请先删除对应产品下的固件");
        }
        // 产品下不能有设备
        int deviceCount = productMapper.deviceCountInProducts(productIds);
        if (deviceCount > 0) {
            return AjaxResult.error("删除失败，请先删除对应产品下的设备");
        }
        // 产品下不能有场景联动
        List<SceneDeviceBindVO> sceneDeviceBindVOList = sceneDeviceMapper.listSceneProductBind(productIds);
        if (CollectionUtils.isNotEmpty(sceneDeviceBindVOList)) {
            String sceneNames = sceneDeviceBindVOList.stream().map(SceneDeviceBindVO::getSceneName).collect(Collectors.joining("，"));
            return AjaxResult.error("删除失败，请先修改或删除对应产品下的场景联动：" + sceneNames);
        }
        // 删除产品物模型
        productMapper.deleteProductThingsModelByProductIds(productIds);
        // 删除产品的授权码
        productAuthorizeMapper.deleteProductAuthorizeByProductIds(productIds);
        // 删除产品
        if (productMapper.deleteProductByProductIds(productIds) > 0) {
            List<Long> productIdList = Arrays.asList(productIds);
            // 删除产品绑定的子产品关系
            LambdaQueryWrapper<ProductSubGateway> productSubGatewayLambdaQueryWrapper = new LambdaQueryWrapper<>();
            productSubGatewayLambdaQueryWrapper.in(ProductSubGateway::getGwProductId, productIdList);
            productSubGatewayMapper.delete(productSubGatewayLambdaQueryWrapper);
            // 删除产品配置的轮询
            LambdaQueryWrapper<ProductModbusJob> productModbusJobLambdaQueryWrapper = new LambdaQueryWrapper<>();
            productModbusJobLambdaQueryWrapper.in(ProductModbusJob::getProductId, productIdList);
            productModbusJobMapper.delete(productModbusJobLambdaQueryWrapper);
            return AjaxResult.success("删除成功");
        }
        return AjaxResult.error("删除失败");
    }


    /**
     * 删除产品信息
     *
     * @param productId 产品主键
     * @return 结果
     */
    @CacheEvict(cacheNames = "product", allEntries = true)
    @Override
    public int deleteProductByProductId(Long productId) {
        // 删除物模型JSON缓存
        redisCache.deleteObject(RedisKeyBuilder.buildTSLCacheKey(productId));
        return productMapper.deleteProductByProductId(productId);
    }

    /**
     * 查询产品列表
     *
     * @param product 产品
     * @return 产品
     */
    @Override
    public List<Product> selectProductList(Product product) {
        List<Product> productList = productMapper.selectProductList(product);
        // 组态不是所有人都买了，故单独查询组态信息
        List<String> guidList = productList.stream().map(Product::getGuid).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(guidList)) {
            List<Product> scadaList = productMapper.selectListScadaIdByGuidS(guidList);
            Map<String, Long> map = scadaList.stream().collect(Collectors.toMap(Product::getGuid, Product::getScadaId));
            for (Product product1 : productList) {
                Long scadaId = map.get(product1.getGuid());
                product1.setScadaId(scadaId);
            }
        }
        return productList;
    }

    /**
     * 查询产品简短列表
     *
     * @return 产品
     */
    @Override
    public List<IdAndName> selectProductShortList(Product product) {
        return productMapper.selectProductShortList(product);
    }

    /**
     * 根据设备编号查询产品信息
     *
     * @param serialNumber 设备编号
     * @return 结果
     */
    @Override
    public Product getProductBySerialNumber(String serialNumber) {
        Device dev = deviceMapper.selectDeviceBySerialNumber(serialNumber);
        if (dev != null) {
            return this.selectProductByProductId(dev.getProductId());
        }
        return null;
    }

    /**
     * 新增产品
     *
     * @param product 产品
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Product insertProduct(Product product) {
        // mqtt账号密码
        if (product.getMqttAccount() == null || product.getMqttAccount().equals("")) {
            product.setMqttAccount("FastBee");
        }
        if (product.getMqttPassword() == null || product.getMqttPassword().equals("")) {
            product.setMqttPassword("P" + toolService.getStringRandom(15));
        }
        if (product.getMqttSecret() == null || product.getMqttSecret().equals("")) {
            product.setMqttSecret("K" + toolService.getStringRandom(15));
        }
        product.setStatus(product.getStatus() == null ? 1 : product.getStatus());
        product.setCreateTime(DateUtils.getNowDate());
        productMapper.insertProduct(product);
        return product;
    }


    /**
     * 更新产品状态,1-未发布，2-已发布
     *
     * @param model
     * @return 结果
     */
    @Override
    @Transactional
    public AjaxResult changeProductStatus(ChangeProductStatusModel model) {
        if (model.getStatus() != 1 && model.getStatus() != 2) {
            return AjaxResult.error("状态更新失败,状态值有误");
        }
        if (model.getStatus() == 2) {
            // 不需要一定要有物模型才可以发布
            updateDeviceStatusByProductIdAsync(model.getProductId());
            //更新物模型缓存
            itslCache.setCacheThingsModelByProductId(model.getProductId());
        }
        if (productMapper.changeProductStatus(model) > 0) {
            return AjaxResult.success("操作成功");
        }
        return AjaxResult.error("状态更新失败");
    }

    /***
     * 更新产品下所有设备的物模型值
     * @param productId
     */
    @Async
    public void updateDeviceStatusByProductIdAsync(Long productId) {
        List<String> deviceNumbers = deviceMapper.selectSerialNumberByProductId(productId);
        deviceNumbers.forEach(x -> {
            // 缓存新的物模型值
            thingModelCache.addCacheDeviceStatus(productId, x);
        });
    }

    /**
     * 根据模板id查询所有使用的产品
     *
     * @param templeId 模板id
     * @return
     */
    @Override
    public List<Product> selectByTempleId(Long templeId) {
        return productMapper.selectByTempleId(templeId);
    }

    @Override
    public String selectImgUrlByProductId(Long productId) {
        return productMapper.selectImgUrlByProductId(productId);
    }

    @Override
    public List<Product> selectTerminalUserProduct(Product product) {
        return productMapper.selectTerminalUserProduct(product);
    }

    /**
     * 根据产品id获取关联组态guid
     *
     * @param productId
     * @return guid
     */
    @Override
    public String selectGuidByProductId(Long productId) {
        return productMapper.selectGuidByProductId(productId);
    }
}
