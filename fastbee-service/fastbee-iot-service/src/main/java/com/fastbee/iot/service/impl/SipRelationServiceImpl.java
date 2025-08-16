package com.fastbee.iot.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fastbee.common.utils.DateUtils;
import com.fastbee.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fastbee.iot.mapper.SipRelationMapper;
import com.fastbee.iot.domain.SipRelation;
import com.fastbee.iot.service.ISipRelationService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 监控设备关联Service业务层处理
 *
 * @author kerwincui
 * @date 2024-06-06
 */
@Service
public class SipRelationServiceImpl implements ISipRelationService
{
    @Resource
    private SipRelationMapper sipRelationMapper;

    /**
     * 查询监控设备关联
     *
     * @param id 监控设备关联主键
     * @return 监控设备关联
     */
    @Override
    public SipRelation selectSipRelationById(Long id)
    {
        return sipRelationMapper.selectSipRelationById(id);
    }

    @Override
    public List<SipRelation> selectSipRelationByDeviceId(Long deviceId) {
        SipRelation sipRelation = new SipRelation();
        sipRelation.setReDeviceId(deviceId);
        return sipRelationMapper.selectSipRelationList(sipRelation);
    }

    /**
     * 查询监控设备关联列表
     *
     * @param sipRelation 监控设备关联
     * @return 监控设备关联
     */
    @Override
    public List<SipRelation> selectSipRelationList(SipRelation sipRelation)
    {
        return sipRelationMapper.selectSipRelationList(sipRelation);
    }

    /**
     * 根据channelId获取关联关系
     * @param channelId
     * @return
     */
    @Override
     public SipRelation selectByChannelId(String channelId){
        return sipRelationMapper.selectByChannelId(channelId);
     }

    /**
     * 新增或者更新监控设备关联
     * @param sipRelation
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addOrUpdateSipRelation(SipRelation sipRelation){
        String channelId = sipRelation.getChannelId();
        assert !Objects.isNull(channelId) : "channelId is null";
        SipRelation selectObj = sipRelationMapper.selectByChannelId(channelId);
        if (Objects.isNull(selectObj)){
            //新增
            sipRelation.setCreateTime(DateUtils.getNowDate());
            sipRelation.setCreateBy(SecurityUtils.getUsername());
            return this.insertSipRelation(sipRelation);
        }else {
            sipRelation.setUpdateTime(DateUtils.getNowDate());
            sipRelation.setUpdateBy(SecurityUtils.getUsername());
            return sipRelationMapper.updateByChannelId(sipRelation);
        }
    }

    /**
     * 新增监控设备关联
     *
     * @param sipRelation 监控设备关联
     * @return 结果
     */
    @Override
    public int insertSipRelation(SipRelation sipRelation)
    {
        sipRelation.setCreateTime(DateUtils.getNowDate());
        return sipRelationMapper.insertSipRelation(sipRelation);
    }

    /**
     * 修改监控设备关联
     *
     * @param sipRelation 监控设备关联
     * @return 结果
     */
    @Override
    public int updateSipRelation(SipRelation sipRelation)
    {
        sipRelation.setUpdateTime(DateUtils.getNowDate());
        return sipRelationMapper.updateSipRelation(sipRelation);
    }


    /**
     * 批量删除监控设备关联
     *
     * @param ids 需要删除的监控设备关联主键
     * @return 结果
     */
    @Override
    public int deleteSipRelationByIds(Long[] ids)
    {
        return sipRelationMapper.deleteSipRelationByIds(ids);
    }

    /**
     * 删除监控设备关联信息
     *
     * @param id 监控设备关联主键
     * @return 结果
     */
    @Override
    public int deleteSipRelationById(Long id)
    {
        return sipRelationMapper.deleteSipRelationById(id);
    }
}
