package com.fastbee.iot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fastbee.common.utils.SecurityUtils;
import com.fastbee.iot.domain.ModbusParams;
import com.fastbee.iot.domain.ProductModbusJob;
import com.fastbee.iot.domain.ProductSubGateway;
import com.fastbee.iot.mapper.ModbusParamsMapper;
import com.fastbee.iot.mapper.ProductModbusJobMapper;
import com.fastbee.iot.mapper.ProductSubGatewayMapper;
import com.fastbee.iot.model.gateWay.ProductSubGatewayAddVO;
import com.fastbee.iot.model.gateWay.ProductSubGatewayVO;
import com.fastbee.iot.service.IProductSubGatewayService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 网关与子产品关联Service业务层处理
 *
 * @author zhuangpeng.li
 * @date 2024-09-04
 */
@Service
public class ProductSubGatewayServiceImpl extends ServiceImpl<ProductSubGatewayMapper,ProductSubGateway> implements IProductSubGatewayService {

    @Resource
    private ModbusParamsMapper modbusParamsMapper;
    @Resource
    private ProductModbusJobMapper productModbusJobMapper;

    /**
     * 查询网关与子产品关联
     *
     * @param id 主键
     * @return 网关与子产品关联
     */
    @Override
    @Cacheable(cacheNames = "sql_cache:ProductSubGateway", key = "#id")
    // 查询时更新key缓存，更新和删除时删除缓存，新增时不更新，下一次查询会更新缓存
    public ProductSubGateway queryByIdWithCache(Long id){
        return this.getById(id);
    }

    /**
     * 查询网关与子产品关联
     *
     * @param id 主键
     * @return 网关与子产品关联
     */
    @Override
    @Cacheable(cacheNames = "sql_cache:ProductSubGateway", key = "#id")
    // 查询时更新key缓存，更新和删除时删除缓存，新增时不更新，下一次查询会更新缓存
    public ProductSubGateway selectProductSubGatewayById(Long id){
        return this.getById(id);
    }

    /**
     * 查询网关与子产品关联列表
     *
     * @param productSubGateway 网关与子产品关联
     * @return 网关与子产品关联
     */
    @Override
    public List<ProductSubGatewayVO> selectProductSubGatewayList(ProductSubGateway productSubGateway) {
        return baseMapper.selectListVO(productSubGateway);
    }

    private LambdaQueryWrapper<ProductSubGateway> buildQueryWrapper(ProductSubGateway query) {
        Map<String, Object> params = query.getParams();
        LambdaQueryWrapper<ProductSubGateway> lqw = Wrappers.lambdaQuery();
                    lqw.eq(query.getGwProductId() != null, ProductSubGateway::getGwProductId, query.getGwProductId());
                    lqw.eq(query.getSubProductId() != null, ProductSubGateway::getSubProductId, query.getSubProductId());
                    lqw.eq(query.getSlaveId() != null, ProductSubGateway::getSlaveId, query.getSlaveId());

        if (!Objects.isNull(params.get("beginTime")) &&
        !Objects.isNull(params.get("endTime"))) {
            lqw.between(ProductSubGateway::getCreateTime, params.get("beginTime"), params.get("endTime"));
        }
        return lqw;
    }

    /**
     * 新增网关与子产品关联
     *
     * @param add 网关与子产品关联
     * @return 是否新增成功
     */
    @Override
    public Boolean insertWithCache(ProductSubGateway add) {
        validEntityBeforeSave(add);
        return this.save(add);
    }

    /**
     * 修改网关与子产品关联
     *
     * @param update 网关与子产品关联
     * @return 是否修改成功
     */
    @Override
    @CacheEvict(cacheNames = "sql_cache:ProductSubGateway", key = "#update.id")
    public Boolean updateWithCache(ProductSubGateway update) {
        validEntityBeforeSave(update);
        return this.updateById(update);
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ProductSubGateway entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除网关与子产品关联信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    @CacheEvict(cacheNames = "sql_cache:ProductSubGateway", keyGenerator = "deleteKeyGenerator" )
    public Boolean deleteWithCacheByIds(Long[] ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return this.removeByIds(Arrays.asList(ids));
    }

    @Override
    public int addBatch(ProductSubGatewayAddVO productSubGatewayAddVO) {
        Long gwProductId = productSubGatewayAddVO.getGwProductId();
        List<Long> subProductIdList = productSubGatewayAddVO.getSubProductIds();
        if (CollectionUtils.isEmpty(subProductIdList)) {
            return 1;
        }
        LambdaQueryWrapper<ProductSubGateway> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductSubGateway::getGwProductId, gwProductId);
        queryWrapper.in(ProductSubGateway::getSubProductId, subProductIdList);
        List<ProductSubGateway> productSubGateways = baseMapper.selectList(queryWrapper);
        List<Long> exitSubProductIdList = productSubGateways.stream().map(ProductSubGateway::getSubProductId).collect(Collectors.toList());
        subProductIdList.removeAll(exitSubProductIdList);
        List<ProductSubGateway> list = new ArrayList<>();
        for (Long subProductId : subProductIdList) {
            ModbusParams modbusParams = modbusParamsMapper.getModbusParamsByProductId(subProductId);
            ProductSubGateway productSubGateway = new ProductSubGateway();
            productSubGateway.setGwProductId(gwProductId);
            productSubGateway.setSubProductId(subProductId);
            if (!Objects.isNull(modbusParams)) {
                productSubGateway.setSlaveId(modbusParams.getSlaveId());
            }
            productSubGateway.setCreateBy(SecurityUtils.getUsername());
            list.add(productSubGateway);
        }
        if (CollectionUtils.isNotEmpty(list)) {
            baseMapper.insertBatch(list);
        }
        return 1;
    }

    @Override
    public int editBatch(List<ProductSubGateway> list) {
        // 同步产品轮询修改从机地址
        Long gwProductId = list.get(0).getGwProductId();
        LambdaQueryWrapper<ProductSubGateway> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductSubGateway::getGwProductId, gwProductId);
        List<ProductSubGateway> productSubGatewayList = baseMapper.selectList(queryWrapper);
        Map<Long, Integer> productSubGatewayMap = productSubGatewayList.stream().collect(Collectors.toMap(ProductSubGateway::getSubProductId, ProductSubGateway::getSlaveId));
        List<ProductSubGateway> editList = list.stream().filter(i -> !i.getSlaveId().equals(productSubGatewayMap.get(i.getSubProductId()))).collect(Collectors.toList());
        baseMapper.updateBatch(list, list.size());
        List<Long> editSubProductIdList = editList.stream().map(ProductSubGateway::getSubProductId).collect(Collectors.toList());
        Map<Long, ProductSubGateway> oldEdidMap = productSubGatewayList.stream().filter(i -> editSubProductIdList.contains(i.getSubProductId())).collect(Collectors.toList())
                .stream().collect(Collectors.toMap(ProductSubGateway::getSubProductId, Function.identity()));
        LambdaQueryWrapper<ProductModbusJob> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.in(ProductModbusJob::getProductId, editSubProductIdList);
        List<ProductModbusJob> modbusJobList = productModbusJobMapper.selectList(queryWrapper1);
        Map<Long, List<ProductModbusJob>> oldMap = new HashMap<>(2);
        if (CollectionUtils.isNotEmpty(modbusJobList)) {
            oldMap = modbusJobList.stream().collect(Collectors.groupingBy(ProductModbusJob::getProductId));
        }
        LambdaQueryWrapper<ModbusParams> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.in(ModbusParams::getProductId, editSubProductIdList);
        List<ModbusParams> modbusParamsList = modbusParamsMapper.selectList(queryWrapper2);
        for (ProductSubGateway productSubGateway : editList) {
            Integer slaveId = productSubGateway.getSlaveId();
            ProductSubGateway oldProductSubGateway = oldEdidMap.get(productSubGateway.getSubProductId());
            int subIndex = String.valueOf(oldProductSubGateway.getSlaveId()).length() == 1 ? 2 : String.valueOf(oldProductSubGateway.getSlaveId()).length();
            List<ProductModbusJob> productModbusJobList = oldMap.get(productSubGateway.getSubProductId());
            for (ProductModbusJob productModbusJob : productModbusJobList) {
                String oldCommand = productModbusJob.getCommand();
                String substring = oldCommand.substring(subIndex);
                String newSlaveId = String.format("%02d", slaveId);
                productModbusJob.setCommand(newSlaveId + substring);
            }
            if (CollectionUtils.isNotEmpty(productModbusJobList)) {
                productModbusJobMapper.updateBatch(productModbusJobList, productSubGatewayList.size());
            }
            for (ModbusParams modbusParams : modbusParamsList) {
                if (productSubGateway.getSubProductId().equals(modbusParams.getProductId())) {
                    modbusParams.setSlaveId(productSubGateway.getSlaveId());
                }
            }
        }
        if (CollectionUtils.isNotEmpty(modbusParamsList)) {
            modbusParamsMapper.updateBatch(modbusParamsList, modbusParamsList.size());
        }
        return 1;
    }

}
