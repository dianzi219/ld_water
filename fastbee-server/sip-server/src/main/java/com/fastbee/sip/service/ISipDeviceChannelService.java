package com.fastbee.sip.service;

import com.fastbee.sip.domain.BindingChannel;
import com.fastbee.sip.domain.SipDeviceChannel;
import com.fastbee.sip.model.BaseTree;

import java.util.List;

/**
 * 监控设备通道信息Service接口
 *
 * @author zhuangpeng.li
 * @date 2022-10-07
 */
public interface ISipDeviceChannelService
{
    public void updateChannel(String deviceId, SipDeviceChannel channel);
    /**
     * 查询监控设备通道信息
     *
     * @param channelId 监控设备通道信息主键
     * @return 监控设备通道信息
     */
    public SipDeviceChannel selectSipDeviceChannelByChannelId(Long channelId);

    public SipDeviceChannel selectSipDeviceChannelByChannelSipId(String channelSipId);

    public List<SipDeviceChannel> selectSipDeviceChannelByDeviceSipId(String deviceSipId);

    /**
     * 查询监控设备通道信息列表
     *
     * @param sipDeviceChannel 监控设备通道信息
     * @return 监控设备通道信息集合
     */
    public List<SipDeviceChannel> selectSipDeviceChannelList(SipDeviceChannel sipDeviceChannel);

    /**
     * 新增监控设备通道信息
     *
     * @param sipDeviceChannel 监控设备通道信息
     * @return 结果
     */
    public int insertSipDeviceChannel(SipDeviceChannel sipDeviceChannel);
    public String insertSipDeviceChannelGen(Long createNum, SipDeviceChannel sipDeviceChannel);
    /**
     * 修改监控设备通道信息
     *
     * @param sipDeviceChannel 监控设备通道信息
     * @return 结果
     */
    public int updateSipDeviceChannel(SipDeviceChannel sipDeviceChannel);
    public int updateSipDeviceChannelStatus(String ChannelId, Integer status);

    /**
     * 批量删除监控设备通道信息
     *
     * @param channelIds 需要删除的监控设备通道信息主键集合
     * @return 结果
     */
    public int deleteSipDeviceChannelByChannelIds(Long[] channelIds);

    /**
     * 删除监控设备通道信息信息
     *
     * @param channelId 监控设备通道信息主键
     * @return 结果
     */
    public int deleteSipDeviceChannelByChannelId(Long channelId);

    public List<BaseTree<SipDeviceChannel>> queryVideoDeviceTree(String deviceId, String parentId, boolean onlyCatalog);


    /**
     * 根据channelId获取绑定的设备或场景
     * @param channelId
     * @return
     */
    public BindingChannel getBindingChannel(String channelId);

    /**
     * @description: 查询设备或场景关联通道
     * @param: serialNumber 设备编号
     * @param: sceneModelId  场景id
     * @return: java.util.List<com.fastbee.sip.domain.SipDeviceChannel>
     */
    List<SipDeviceChannel> listRelDeviceOrScene(String serialNumber, Long sceneModelId);
}
