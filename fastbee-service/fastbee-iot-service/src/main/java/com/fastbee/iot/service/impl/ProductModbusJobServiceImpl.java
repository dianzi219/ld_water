package com.fastbee.iot.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fastbee.iot.convert.ProductModbusJobConvert;
import com.fastbee.iot.domain.ModbusParams;
import com.fastbee.iot.domain.SubGateway;
import com.fastbee.iot.mapper.ModbusParamsMapper;
import com.fastbee.iot.mapper.SubGatewayMapper;
import com.fastbee.iot.model.modbus.ProductModbusJobVO;
import com.fastbee.iot.util.JobCronUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import java.util.Objects;
import com.fastbee.common.utils.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import com.fastbee.iot.mapper.ProductModbusJobMapper;
import com.fastbee.iot.domain.ProductModbusJob;
import com.fastbee.iot.service.IProductModbusJobService;

import javax.annotation.Resource;

/**
 * 产品轮训任务列Service业务层处理
 *
 * @author zhuangpeng.li
 * @date 2024-09-04
 */
@Service
public class ProductModbusJobServiceImpl extends ServiceImpl<ProductModbusJobMapper,ProductModbusJob> implements IProductModbusJobService {

    @Resource
    private ModbusParamsMapper modbusParamsMapper;
    @Resource
    private SubGatewayMapper subGatewayMapper;

    /**
     * 查询产品轮训任务列
     *
     * @param taskId 主键
     * @return 产品轮训任务列
     */
    @Override
    @Cacheable(cacheNames = "sql_cache:ProductModbusJob", key = "#taskId")
    // 查询时更新key缓存，更新和删除时删除缓存，新增时不更新，下一次查询会更新缓存
    public ProductModbusJob queryByIdWithCache(Long taskId){
        return this.getById(taskId);
    }

    /**
     * 查询产品轮训任务列
     *
     * @param taskId 主键
     * @return 产品轮训任务列
     */
    @Override
    @Cacheable(cacheNames = "sql_cache:ProductModbusJob", key = "#taskId")
    // 查询时更新key缓存，更新和删除时删除缓存，新增时不更新，下一次查询会更新缓存
    public ProductModbusJob selectProductModbusJobById(Long taskId){
        return this.getById(taskId);
    }

    /**
     * 查询产品轮训任务列列表
     *
     * @param productModbusJob 产品轮训任务列
     * @return 产品轮训任务列
     */
    @Override
    public List<ProductModbusJobVO> selectProductModbusJobList(ProductModbusJob productModbusJob) {
        LambdaQueryWrapper<ProductModbusJob> lqw = buildQueryWrapper(productModbusJob);
        List<ProductModbusJob> productModbusJobList = baseMapper.selectList(lqw);
        List<ProductModbusJobVO> productModbusJobVOList = ProductModbusJobConvert.INSTANCE.convertProductModbusJobVOList(productModbusJobList);
        for (ProductModbusJobVO productModbusJobVO : productModbusJobVOList) {
            if (StringUtils.isNotEmpty(productModbusJobVO.getRemark())) {
                productModbusJobVO.setRemarkStr(JobCronUtils.handleCronCycle(1, productModbusJobVO.getRemark()).getDesc());
            }
        }
        return productModbusJobVOList;
    }

    private LambdaQueryWrapper<ProductModbusJob> buildQueryWrapper(ProductModbusJob query) {
        Map<String, Object> params = query.getParams();
        LambdaQueryWrapper<ProductModbusJob> lqw = Wrappers.lambdaQuery();
                    lqw.like(StringUtils.isNotBlank(query.getJobName()), ProductModbusJob::getJobName, query.getJobName());
                    lqw.eq(query.getProductId() != null, ProductModbusJob::getProductId, query.getProductId());
                    lqw.eq(StringUtils.isNotBlank(query.getCommand()), ProductModbusJob::getCommand, query.getCommand());

        if (!Objects.isNull(params.get("beginTime")) &&
        !Objects.isNull(params.get("endTime"))) {
            lqw.between(ProductModbusJob::getCreateTime, params.get("beginTime"), params.get("endTime"));
        }
        return lqw;
    }

    /**
     * 新增产品轮训任务列
     *
     * @param add 产品轮训任务列
     * @return 是否新增成功
     */
    @Override
    public Boolean insertWithCache(ProductModbusJob add) {
        validEntityBeforeSave(add);
        return this.save(add);
    }

    /**
     * 修改产品轮训任务列
     *
     * @param update 产品轮训任务列
     * @return 是否修改成功
     */
    @Override
    @CacheEvict(cacheNames = "sql_cache:ProductModbusJob", key = "#update.taskId")
    public Boolean updateWithCache(ProductModbusJob update) {
        validEntityBeforeSave(update);
        return this.updateById(update);
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ProductModbusJob entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除产品轮训任务列信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    @CacheEvict(cacheNames = "sql_cache:ProductModbusJob", keyGenerator = "deleteKeyGenerator" )
    public Boolean deleteWithCacheByIds(Long[] ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return this.removeByIds(Arrays.asList(ids));
    }

    @Override
    public Integer getSlaveId(Long productId, Long deviceId) {
        if (null != deviceId) {
            return subGatewayMapper.selectSlaveIdBySubDeviceId(deviceId);
        }
        LambdaQueryWrapper<ModbusParams> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ModbusParams::getProductId, productId);
        ModbusParams modbusParams = modbusParamsMapper.selectOne(queryWrapper);
        return null != modbusParams ? modbusParams.getSlaveId() : null;
    }

}
