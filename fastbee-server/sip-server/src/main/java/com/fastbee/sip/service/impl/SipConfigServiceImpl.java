package com.fastbee.sip.service.impl;

import com.fastbee.common.constant.FastBeeConstant;
import com.fastbee.common.core.redis.RedisCache;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.iot.domain.Device;
import com.fastbee.iot.service.IDeviceService;
import com.fastbee.sip.conf.SysSipConfig;
import com.fastbee.sip.domain.SipConfig;
import com.fastbee.sip.mapper.SipConfigMapper;
import com.fastbee.sip.service.ISipConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * sip系统配置Service业务层处理
 *
 * @author zhuangpeng.li
 * @date 2022-11-30
 */
@Service
public class SipConfigServiceImpl implements ISipConfigService {
    @Autowired
    private SipConfigMapper sipConfigMapper;

    @Autowired
    private IDeviceService deviceService;

    @Autowired
    private SysSipConfig sysSipConfig;

    @Autowired
    private RedisCache redisCache;

    @Override
    public void updateDefaultSipConfig(SipConfig sipConfig) {
        SysSipConfig defConfig = new SysSipConfig();
        defConfig.setEnabled(sipConfig.getEnabled()==1);
        defConfig.setIp(sipConfig.getIp());
        defConfig.setPort(sipConfig.getPort());
        defConfig.setDomain(sipConfig.getDomain());
        defConfig.setId(sipConfig.getServerSipid());
        defConfig.setPassword(sipConfig.getPassword());
        redisCache.setCacheObject(FastBeeConstant.REDIS.DEFAULT_SIP_CONFIG, sipConfig);
    }

    @Override
    public SipConfig GetDefaultSipConfig() {
        Object temp = redisCache.getCacheObject(FastBeeConstant.REDIS.DEFAULT_SIP_CONFIG);
        SipConfig sipConfig = new SipConfig();
        if (temp == null) {
            sipConfig.setEnabled(sysSipConfig.isEnabled() ? 1 : 0);
            sipConfig.setIp(sysSipConfig.getIp());
            sipConfig.setPort(sysSipConfig.getPort());
            sipConfig.setDomain(sysSipConfig.getDomain());
            sipConfig.setServerSipid(sysSipConfig.getId());
            sipConfig.setPassword(sysSipConfig.getPassword());
            redisCache.setCacheObject(FastBeeConstant.REDIS.DEFAULT_SIP_CONFIG, sipConfig);
        } else if (temp instanceof SipConfig){
            sipConfig = (SipConfig) temp;
            updateDefaultSipConfig(sipConfig);
        } else if (temp instanceof SysSipConfig){
            SysSipConfig temp2 = (SysSipConfig) temp;
            sipConfig.setEnabled(temp2.isEnabled() ? 1 : 0);
            sipConfig.setIp(temp2.getIp());
            sipConfig.setPort(temp2.getPort());
            sipConfig.setDomain(temp2.getDomain());
            sipConfig.setServerSipid(temp2.getId());
            sipConfig.setPassword(temp2.getPassword());
        }
        return sipConfig;
    }

    /**
     * 查询产品下第一条sip系统配置
     *
     * @return sip系统配置
     */
    @Cacheable(cacheNames = "sipConfig", key = "#root.methodName +'_' + #productId", unless = "#result == null")
    @Override
    public SipConfig selectSipConfigByProductId(Long productId) {
        SipConfig sipConfig = sipConfigMapper.selectSipConfigByProductId(productId);
        if (sipConfig == null) {
            sipConfig = GetDefaultSipConfig();
            sipConfig.setProductId(productId);
            sipConfig.setCreateTime(DateUtils.getNowDate());
            sipConfigMapper.insertSipConfig(sipConfig);
        }
        return sipConfig;
    }


    @Override
    public SipConfig selectSipConfigBydeviceSipId(String deviceSipId) {
        Device device = deviceService.selectDeviceBySerialNumber(deviceSipId);
        if (device != null) {
            return this.selectSipConfigByProductId(device.getProductId());
        } else {
            return this.GetDefaultSipConfig();
        }
    }

    /**
     * 新增sip系统配置
     *
     * @param sipConfig sip系统配置
     * @return 结果
     */
    @Override
    public int insertSipConfig(SipConfig sipConfig) {
        sipConfig.setCreateTime(DateUtils.getNowDate());
        if (sipConfig.getIsdefault() != null && sipConfig.getIsdefault() == 1) {
            sipConfigMapper.resetDefaultSipConfig();
            updateDefaultSipConfig(sipConfig);
        }
        return sipConfigMapper.insertSipConfig(sipConfig);
    }

    /**
     * 修改sip系统配置
     *
     * @param sipConfig sip系统配置
     * @return 结果
     */
    @Caching(evict = {
            @CacheEvict(cacheNames = "sipConfig", key = "'selectSipConfigByProductId_' + #sipConfig.productId"),
    })
    @Override
    public int updateSipConfig(SipConfig sipConfig) {
        sipConfig.setUpdateTime(DateUtils.getNowDate());
        if (sipConfig.getIsdefault() != null && sipConfig.getIsdefault() == 1) {
            sipConfigMapper.resetDefaultSipConfig();
            updateDefaultSipConfig(sipConfig);
        }
        return sipConfigMapper.updateSipConfig(sipConfig);
    }

    @Override
    public void syncSipConfig(SysSipConfig sipConfig) {
        List<SipConfig> list = sipConfigMapper.selectSipConfigList(new SipConfig());
        for (SipConfig config : list) {
            config.setIp(sipConfig.getIp());
            config.setPort(sipConfig.getPort());
            sipConfigMapper.updateSipConfig(config);
        }
        GetDefaultSipConfig();
    }

    /**
     * 批量删除sip系统配置
     *
     * @param ids 需要删除的sip系统配置主键
     * @return 结果
     */
    @CacheEvict(cacheNames = "sipConfig", allEntries = true)
    @Override
    public int deleteSipConfigByIds(Long[] ids) {
        return sipConfigMapper.deleteSipConfigByIds(ids);
    }

    @CacheEvict(cacheNames = "sipConfig", allEntries = true)
    @Override
    public int deleteSipConfigByProductIds(Long[] productIds) {
        return sipConfigMapper.deleteSipConfigByProductId(productIds);
    }
}
