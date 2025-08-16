package com.fastbee.iot.service.impl;

import java.util.*;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.iot.domain.ProductModbusJob;
import com.fastbee.iot.mapper.ProductModbusJobMapper;
import org.springframework.stereotype.Service;
import com.fastbee.common.utils.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import com.fastbee.iot.mapper.ModbusParamsMapper;
import com.fastbee.iot.domain.ModbusParams;
import com.fastbee.iot.service.IModbusParamsService;

import javax.annotation.Resource;

/**
 * 产品modbus配置参数Service业务层处理
 *
 * @author zhuangpeng.li
 * @date 2024-08-20
 */
@Service
public class ModbusParamsServiceImpl extends ServiceImpl<ModbusParamsMapper,ModbusParams> implements IModbusParamsService {

    @Resource
    private ProductModbusJobMapper productModbusJobMapper;

    /**
     * 查询产品modbus配置参数
     *
     * @param id 主键
     * @return 产品modbus配置参数
     */
    @Override
    @Cacheable(cacheNames = "ModbusParams", key = "#id")
    // 查询时更新key缓存，更新和删除时删除缓存，新增时不更新，下一次查询会更新缓存
    public ModbusParams queryByIdWithCache(Long id){
        return this.getById(id);
    }

    /**
     * 查询产品modbus配置参数
     *
     * @param id 主键
     * @return 产品modbus配置参数
     */
    @Override
    @Cacheable(cacheNames = "ModbusParams", key = "#id")
    // 查询时更新key缓存，更新和删除时删除缓存，新增时不更新，下一次查询会更新缓存
    public ModbusParams selectModbusParamsById(Long id){
        return this.getById(id);
    }

    /**
     * 查询产品modbus配置参数列表
     *
     * @param modbusParams 产品modbus配置参数
     * @return 产品modbus配置参数
     */
    @Override
    public List<ModbusParams> selectModbusParamsList(ModbusParams modbusParams) {
        LambdaQueryWrapper<ModbusParams> lqw = buildQueryWrapper(modbusParams);
        return baseMapper.selectList(lqw);
    }

    private LambdaQueryWrapper<ModbusParams> buildQueryWrapper(ModbusParams query) {
        Map<String, Object> params = query.getParams();
        LambdaQueryWrapper<ModbusParams> lqw = Wrappers.lambdaQuery();
                    lqw.eq(query.getProductId() != null, ModbusParams::getProductId, query.getProductId());
                    lqw.eq(query.getPollType() != null, ModbusParams::getPollType, query.getPollType());
                    lqw.eq(query.getSlaveId() != null, ModbusParams::getSlaveId, query.getSlaveId());
                    lqw.eq(query.getStatusDeter() != null, ModbusParams::getStatusDeter, query.getStatusDeter());
                    lqw.eq(StringUtils.isNotBlank(query.getDeterTimer()), ModbusParams::getDeterTimer, query.getDeterTimer());
                    lqw.eq(query.getPollLength() != null, ModbusParams::getPollLength, query.getPollLength());

        if (!Objects.isNull(params.get("beginTime")) &&
        !Objects.isNull(params.get("endTime"))) {
            lqw.between(ModbusParams::getCreateTime, params.get("beginTime"), params.get("endTime"));
        }
        return lqw;
    }

    /**
     * 新增产品modbus配置参数
     *
     * @param add 产品modbus配置参数
     * @return 是否新增成功
     */
    @Override
    public Boolean insertWithCache(ModbusParams add) {
        validEntityBeforeSave(add);
        return this.save(add);
    }

    /**
     * 更新或新增
     * @param modbusParams
     */
    @Override
    public boolean addOrUpdate(ModbusParams modbusParams){
        Long productId = modbusParams.getProductId();
        ModbusParams params = this.getModbusParamsByProductId(productId);
        if (!Objects.isNull(params)){
            modbusParams.setId(params.getId());
        }
        boolean b = this.saveOrUpdate(modbusParams);
        if (b && 1 == modbusParams.getPollType()) {
            // 删除产品轮询任务配置
            LambdaQueryWrapper<ProductModbusJob> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ProductModbusJob::getProductId, productId);
            productModbusJobMapper.delete(queryWrapper);
        }
        return b;
    }

    /**
     * 修改产品modbus配置参数
     *
     * @param update 产品modbus配置参数
     * @return 是否修改成功
     */
    @Override
    @CacheEvict(cacheNames = "ModbusParams", key = "#update.id")
    public Boolean updateWithCache(ModbusParams update) {
        validEntityBeforeSave(update);
        return this.updateById(update);
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ModbusParams entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除产品modbus配置参数信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    @CacheEvict(cacheNames = "ModbusParams", keyGenerator = "deleteKeyGenerator" )
    public Boolean deleteWithCacheByIds(Long[] ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return this.removeByIds(Arrays.asList(ids));
    }

    /**
     * 根据产品io获取modbus配置
     * @param productId
     * @return
     */
    @Override
    public ModbusParams getModbusParamsByProductId(Long productId){
        return baseMapper.getModbusParamsByProductId(productId);
    }

    /**
     * 根据设备id获取modbus配置
     * @param deviceId
     * @return
     */
    @Override
    public ModbusParams getModbusParamsByDeviceId(Long deviceId){
        return baseMapper.getModbusParamsByDeviceId(deviceId);
    }

    @Override
    public List<ModbusParams> selectModbusParamsListByProductIds(List<Long> productIdList) {
        LambdaQueryWrapper<ModbusParams> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ModbusParams::getProductId, productIdList);
        return baseMapper.selectList(queryWrapper);
    }
}
