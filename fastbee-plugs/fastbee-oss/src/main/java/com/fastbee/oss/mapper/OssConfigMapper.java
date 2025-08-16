package com.fastbee.oss.mapper;

import java.util.List;
import com.fastbee.oss.domain.OssConfig;

/**
 * 对象存储配置Mapper接口
 * 
 * @author zhuangpeng.li
 * @date 2024-04-19
 */
public interface OssConfigMapper 
{
    /**
     * 查询对象存储配置
     * 
     * @param id 对象存储配置主键
     * @return 对象存储配置
     */
    public OssConfig selectOssConfigById(Integer id);

    /**
     * 查询对象存储配置列表
     * 
     * @param ossConfig 对象存储配置
     * @return 对象存储配置集合
     */
    public List<OssConfig> selectOssConfigList(OssConfig ossConfig);

    /**
     * 新增对象存储配置
     * 
     * @param ossConfig 对象存储配置
     * @return 结果
     */
    public int insertOssConfig(OssConfig ossConfig);

    /**
     * 修改对象存储配置
     * 
     * @param ossConfig 对象存储配置
     * @return 结果
     */
    public int updateOssConfig(OssConfig ossConfig);

    /**
     * 删除对象存储配置
     * 
     * @param id 对象存储配置主键
     * @return 结果
     */
    public int deleteOssConfigById(Integer id);

    /**
     * 批量删除对象存储配置
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteOssConfigByIds(Integer[] ids);

    public int resetConfigStatus();
}
