package com.fastbee.oss.mapper;

import java.util.List;
import com.fastbee.oss.domain.OssDetail;

/**
 * 文件记录Mapper接口
 * 
 * @author zhuangpeng.li
 * @date 2024-04-19
 */
public interface OssDetailMapper 
{
    /**
     * 查询文件记录
     * 
     * @param id 文件记录主键
     * @return 文件记录
     */
    public OssDetail selectOssDetailById(Integer id);

    /**
     * 查询文件记录列表
     * 
     * @param ossDetail 文件记录
     * @return 文件记录集合
     */
    public List<OssDetail> selectOssDetailList(OssDetail ossDetail);

    /**
     * 新增文件记录
     * 
     * @param ossDetail 文件记录
     * @return 结果
     */
    public int insertOssDetail(OssDetail ossDetail);

    /**
     * 修改文件记录
     * 
     * @param ossDetail 文件记录
     * @return 结果
     */
    public int updateOssDetail(OssDetail ossDetail);

    /**
     * 删除文件记录
     * 
     * @param id 文件记录主键
     * @return 结果
     */
    public int deleteOssDetailById(Integer id);

    /**
     * 批量删除文件记录
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteOssDetailByIds(Integer[] ids);
}
