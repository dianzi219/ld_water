package com.fastbee.iot.mapper;

import java.util.List;
import com.fastbee.iot.domain.SipRelation;

/**
 * 监控设备关联Mapper接口
 *
 * @author kerwincui
 * @date 2024-06-06
 */
public interface SipRelationMapper
{
    /**
     * 查询监控设备关联
     *
     * @param id 监控设备关联主键
     * @return 监控设备关联
     */
    public SipRelation selectSipRelationById(Long id);

    /**
     * 查询监控设备关联列表
     *
     * @param sipRelation 监控设备关联
     * @return 监控设备关联集合
     */
    public List<SipRelation> selectSipRelationList(SipRelation sipRelation);

    /**
     * 新增监控设备关联
     *
     * @param sipRelation 监控设备关联
     * @return 结果
     */
    public int insertSipRelation(SipRelation sipRelation);

    /**
     * 修改监控设备关联
     *
     * @param sipRelation 监控设备关联
     * @return 结果
     */
    public int updateSipRelation(SipRelation sipRelation);

    /**
     * 删除监控设备关联
     *
     * @param id 监控设备关联主键
     * @return 结果
     */
    public int deleteSipRelationById(Long id);

    /**
     * 批量删除监控设备关联
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSipRelationByIds(Long[] ids);

    /**
     * 根据channelId获取关联关系
     * @param channelId
     * @return
     */
    SipRelation selectByChannelId(String channelId);

    /**
     * 根据channelId更新
     * @param sipRelation
     * @return
     */
    int updateByChannelId(SipRelation sipRelation);

}
