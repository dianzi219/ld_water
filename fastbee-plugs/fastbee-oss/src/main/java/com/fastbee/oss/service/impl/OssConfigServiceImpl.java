package com.fastbee.oss.service.impl;

import java.util.List;

import com.fastbee.common.core.domain.entity.SysUser;
import com.fastbee.common.core.redis.RedisCache;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.oss.entity.OssConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import com.fastbee.oss.mapper.OssConfigMapper;
import com.fastbee.oss.domain.OssConfig;
import com.fastbee.oss.service.IOssConfigService;

import static com.fastbee.common.utils.SecurityUtils.getLoginUser;
import static com.fastbee.common.utils.SecurityUtils.isAdmin;

/**
 * 对象存储配置Service业务层处理
 *
 * @author zhuangpeng.li
 * @date 2024-04-19
 */
@Service
public class OssConfigServiceImpl implements IOssConfigService
{
    @Autowired
    private OssConfigMapper ossConfigMapper;

    @Autowired
    private RedisCache redisCache;

    @Override
    public void init() {
        List<OssConfig> list = ossConfigMapper.selectOssConfigList(null);
        // 加载OSS初始化配置
        for (OssConfig config : list) {
            String configKey = config.getConfigKey();
            if ("0".equals(config.getStatus())) {
                redisCache.setCacheObject(OssConstant.DEFAULT_CONFIG_KEY, configKey);
            }
            redisCache.setCacheObject(OssConstant.OSS_CONFIG_KEY+configKey, config);
        }
    }

    /**
     * 查询对象存储配置
     *
     * @param id 对象存储配置主键
     * @return 对象存储配置
     */
    @Cacheable(cacheNames = "ossConfig", key = "#root.methodName + '_' + #id", unless = "#result == null")
    @Override
    public OssConfig selectOssConfigById(Integer id)
    {
        return ossConfigMapper.selectOssConfigById(id);
    }

    /**
     * 查询对象存储配置列表
     *
     * @param ossConfig 对象存储配置
     * @return 对象存储配置
     */
    @Override
    public List<OssConfig> selectOssConfigList(OssConfig ossConfig)
    {
        SysUser user = getLoginUser().getUser();
        ossConfig.setTenantId(user.getDept().getDeptUserId());
        return ossConfigMapper.selectOssConfigList(ossConfig);
    }

    /**
     * 新增对象存储配置
     *
     * @param ossConfig 对象存储配置
     * @return 结果
     */
    @Override
    public int insertOssConfig(OssConfig ossConfig)
    {
        SysUser user = getLoginUser().getUser();
        if (null == user.getDeptId()) {
            throw new ServiceException("只允许租户配置");
        }
        ossConfig.setTenantId(user.getDept().getDeptUserId());
        ossConfig.setTenantName(user.getDept().getDeptUserName());
        ossConfig.setCreateTime(DateUtils.getNowDate());
        redisCache.setCacheObject(OssConstant.OSS_CONFIG_KEY+ossConfig.getConfigKey(), ossConfig);
        return ossConfigMapper.insertOssConfig(ossConfig);
    }

    /**
     * 修改对象存储配置
     *
     * @param ossConfig 对象存储配置
     * @return 结果
     */

    @CacheEvict(cacheNames = "ossConfig", key = "'selectOssConfigById_' + #ossConfig.getId()")
    @Override
    public int updateOssConfig(OssConfig ossConfig)
    {
        ossConfig.setUpdateTime(DateUtils.getNowDate());
        redisCache.setCacheObject(OssConstant.OSS_CONFIG_KEY+ossConfig.getConfigKey(), ossConfig);
        return ossConfigMapper.updateOssConfig(ossConfig);
    }

    /**
     * 批量删除对象存储配置
     *
     * @param ids 需要删除的对象存储配置主键
     * @return 结果
     */
    @CacheEvict(cacheNames = "ossConfig", allEntries = true)
    @Override
    public int deleteOssConfigByIds(Integer[] ids)
    {
        return ossConfigMapper.deleteOssConfigByIds(ids);
    }

    /**
     * 删除对象存储配置信息
     *
     * @param id 对象存储配置主键
     * @return 结果
     */
    @CacheEvict(cacheNames = "ossConfig", key = "'selectOssConfigById_' + #id")
    @Override
    public int deleteOssConfigById(Integer id)
    {
        return ossConfigMapper.deleteOssConfigById(id);
    }

    @Override
    public int updateOssConfigStatus(OssConfig ossConfig) {
        //重置其他配置状态
        ossConfigMapper.resetConfigStatus();
        int row = ossConfigMapper.updateOssConfig(ossConfig);
        if (row > 0) {
            redisCache.setCacheObject(OssConstant.DEFAULT_CONFIG_KEY, ossConfig.getConfigKey());
        }
        return row;
    }
}
