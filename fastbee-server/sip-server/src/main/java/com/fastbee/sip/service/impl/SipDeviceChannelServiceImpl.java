package com.fastbee.sip.service.impl;

import com.fastbee.common.core.domain.entity.SysUser;
import com.fastbee.common.core.redis.RedisCache;
import com.fastbee.common.core.redis.RedisKeyBuilder;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.iot.domain.Product;
import com.fastbee.iot.service.IProductService;
import com.fastbee.sip.domain.BindingChannel;
import com.fastbee.sip.domain.SipDevice;
import com.fastbee.sip.domain.SipDeviceChannel;
import com.fastbee.sip.enums.DeviceChannelStatus;
import com.fastbee.sip.enums.SessionType;
import com.fastbee.sip.mapper.SipDeviceChannelMapper;
import com.fastbee.sip.mapper.SipDeviceMapper;
import com.fastbee.sip.model.BaseTree;
import com.fastbee.sip.model.Stream;
import com.fastbee.sip.model.VideoSessionInfo;
import com.fastbee.sip.server.VideoSessionManager;
import com.fastbee.sip.service.IPlayService;
import com.fastbee.sip.service.ISipDeviceChannelService;
import com.fastbee.sip.util.SipUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.fastbee.common.utils.SecurityUtils.getLoginUser;

/**
 * 监控设备通道信息Service业务层处理
 *
 * @author zhuangpeng.li
 * @date 2022-10-07
 */
@Slf4j
@Service
public class SipDeviceChannelServiceImpl implements ISipDeviceChannelService {
    @Autowired
    private SipDeviceChannelMapper sipDeviceChannelMapper;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private IProductService productService;

    @Autowired
    private SipDeviceMapper sipDeviceMapper;

    @Autowired
    private VideoSessionManager streamSession;

    @Resource
    private IPlayService playService;

    @Override
    public void updateChannel(String deviceId, SipDeviceChannel channel) {
        SipDeviceChannel deviceChannel = sipDeviceChannelMapper.selectSipDeviceChannelByChannelSipId(channel.getChannelSipId());
        if (deviceChannel == null) {
            Product product = productService.getProductBySerialNumber(deviceId);
            if (product != null) {
                insertSipDeviceChannel(channel);
            } else {
                log.error("未注册设备：{}", deviceId);
            }

        } else {
            channel.setId(deviceChannel.getId());
            sipDeviceChannelMapper.updateSipDeviceChannel(channel);
        }
    }

    /**
     * 查询监控设备通道信息
     *
     * @param channelId 监控设备通道信息主键
     * @return 监控设备通道信息
     */
    @Override
    public SipDeviceChannel selectSipDeviceChannelByChannelId(Long channelId) {
        SipDeviceChannel channel = sipDeviceChannelMapper.selectSipDeviceChannelById(channelId);
        BindingChannel bindingChannel = this.getBindingChannel(channel.getChannelSipId());
        channel.setBindingChannel(bindingChannel);
        return channel;
    }

    @Override
    public SipDeviceChannel selectSipDeviceChannelByChannelSipId(String channelSipId) {
        return sipDeviceChannelMapper.selectSipDeviceChannelByChannelSipId(channelSipId);
    }

    @Override
    public List<SipDeviceChannel> selectSipDeviceChannelByDeviceSipId(String deviceSipId) {
        return sipDeviceChannelMapper.selectSipDeviceChannelByDeviceSipId(deviceSipId);
    }

    /**
     * 查询监控设备通道信息列表
     *
     * @param sipDeviceChannel 监控设备通道信息
     * @return 监控设备通道信息
     */
    @Override
    public List<SipDeviceChannel> selectSipDeviceChannelList(SipDeviceChannel sipDeviceChannel) {
        SysUser user = getLoginUser().getUser();
//        List<SysRole> roles=user.getRoles();
        // 租户
//        if(roles.stream().anyMatch(a->a.getRoleKey().equals("tenant"))){
//            sipDeviceChannel.setTenantId(user.getUserId());
//        }
        sipDeviceChannel.setTenantId(user.getDept().getDeptUserId());
        List<SipDeviceChannel> list = sipDeviceChannelMapper.selectSipDeviceChannelList(sipDeviceChannel);
        List<SipDeviceChannel> newList = new ArrayList<>();
        if (list != null && list.size() > 0) {
            for (SipDeviceChannel channel : list) {
                newList.add(updateChannelStatus(channel));
            }
        }
        return list;
    }

    public SipDeviceChannel updateChannelStatus(SipDeviceChannel channel) {
        String playsid = String.format("%s_%s_%s", SipUtil.PREFIX_PLAY, channel.getDeviceSipId(), channel.getChannelSipId());
        String playrsid = String.format("%s_%s_%s", SipUtil.PREFIX_PLAYRECORD, channel.getDeviceSipId(), channel.getChannelSipId());
        VideoSessionInfo pinfo = streamSession.getSessionInfo(channel.getDeviceSipId(),
                channel.getChannelSipId(), playsid, SessionType.play.name());
        if (pinfo != null) {
            channel.setStreamPush(pinfo.isPushing() ? 1 : 0);
        }
        VideoSessionInfo prinfo = streamSession.getSessionInfo(channel.getDeviceSipId(),
                channel.getChannelSipId(), playrsid, SessionType.playrecord.name());
        if (prinfo != null) {
            channel.setStreamRecord(prinfo.isRecording() ? 1 : 0);
        }
        return channel;
    }

    /**
     * 新增监控设备通道信息
     *
     * @param sipDeviceChannel 监控设备通道信息
     * @return 结果
     */
    @Override
    public String insertSipDeviceChannelGen(Long createNum, SipDeviceChannel sipDeviceChannel) {
        int ret = 0;
        int devuserid = 0;
        int channeluserid = 0;
        String tmpdev = sipDeviceChannel.getDeviceSipId();
        String tmpchannel = sipDeviceChannel.getChannelSipId();
        Object cacheObj = redisCache.getCacheObject(RedisKeyBuilder.buildSipDeviceidCacheKey(tmpdev));
        if (cacheObj != null) {
            devuserid = (int) cacheObj;
        }
        cacheObj = redisCache.getCacheObject(RedisKeyBuilder.buildSipDeviceidCacheKey(tmpchannel));
        if (cacheObj != null) {
            channeluserid = (int) cacheObj;
        }
        String devstr = String.format("%06d", devuserid + 1);
        sipDeviceChannel.setDeviceSipId(tmpdev + devstr);
        sipDeviceChannel.setStatus(DeviceChannelStatus.notused.getValue());
        //限制最大添加数量为10
        if (createNum > 10) {
            createNum = 10L;
        }
        for (int i = 1; i <= createNum; i++) {
            String channelstr = String.format("%06d", channeluserid + i);
            sipDeviceChannel.setChannelSipId(tmpchannel + channelstr);
            SipDeviceChannel deviceChannel = sipDeviceChannelMapper.selectSipDeviceChannelByChannelSipId(sipDeviceChannel.getChannelSipId());
            if (deviceChannel == null) {
                ret = insertSipDeviceChannel(sipDeviceChannel);
            }
        }
        redisCache.incr(RedisKeyBuilder.buildSipDeviceidCacheKey(tmpdev), 1);
        redisCache.incr(RedisKeyBuilder.buildSipDeviceidCacheKey(tmpchannel), createNum);
        if (ret > 0) {
            return tmpdev + devstr;
        } else {
            return "";
        }
    }

    @Override
    public int insertSipDeviceChannel(SipDeviceChannel sipDeviceChannel) {
        Product product = productService.getProductBySerialNumber(sipDeviceChannel.getDeviceSipId());
        if (product != null) {
            sipDeviceChannel.setTenantId(product.getTenantId());
            sipDeviceChannel.setTenantName(product.getTenantName());
            sipDeviceChannel.setProductId(product.getProductId());
            sipDeviceChannel.setProductName(product.getProductName());
        }
        sipDeviceChannel.setCreateTime(DateUtils.getNowDate());
        return sipDeviceChannelMapper.insertSipDeviceChannel(sipDeviceChannel);
    }

    /**
     * 修改监控设备通道信息
     *
     * @param sipDeviceChannel 监控设备通道信息
     * @return 结果
     */
    @Override
    public int updateSipDeviceChannel(SipDeviceChannel sipDeviceChannel) {
        return sipDeviceChannelMapper.updateSipDeviceChannel(sipDeviceChannel);
    }

    @Override
    public int updateSipDeviceChannelStatus(String ChannelId, Integer status) {
        SipDeviceChannel sipDeviceChannel = sipDeviceChannelMapper.selectSipDeviceChannelByChannelSipId(ChannelId);
        if (sipDeviceChannel != null) {
            if (sipDeviceChannel.getRegisterTime() == null && status == 2) {
                sipDeviceChannel.setRegisterTime(DateUtils.getNowDate());
            }
            sipDeviceChannel.setStatus(status);
            return sipDeviceChannelMapper.updateSipDeviceChannel(sipDeviceChannel);
        }
        return 0;
    }

    /**
     * 批量删除监控设备通道信息
     *
     * @param channelIds 需要删除的监控设备通道信息主键
     * @return 结果
     */
    @Override
    public int deleteSipDeviceChannelByChannelIds(Long[] channelIds) {
        return sipDeviceChannelMapper.deleteSipDeviceChannelByIds(channelIds);
    }

    /**
     * 删除监控设备通道信息信息
     *
     * @param channelId 监控设备通道信息主键
     * @return 结果
     */
    @Override
    public int deleteSipDeviceChannelByChannelId(Long channelId) {
        return sipDeviceChannelMapper.deleteSipDeviceChannelById(channelId);
    }

    @Override
    public List<BaseTree<SipDeviceChannel>> queryVideoDeviceTree(String deviceId, String parentId, boolean onlyCatalog) {
        SipDevice device = sipDeviceMapper.selectSipDeviceBySipId(deviceId);
        if (device == null) {
            return null;
        }
        if (parentId == null || parentId.equals(deviceId)) {
            // 字根节点开始查询
            List<SipDeviceChannel> rootNodes = getRootNodes(deviceId, true, !onlyCatalog);
            return transportChannelsToTree(rootNodes, "");
        }

        if (parentId.length() % 2 != 0) {
            return null;
        }
        if (parentId.length() == 10) {
            if (onlyCatalog) {
                return null;
            }
            // parentId为行业编码， 其下不会再有行政区划
            List<SipDeviceChannel> channels = sipDeviceChannelMapper.selectChannelByCivilCode(deviceId, parentId);
            return transportChannelsToTree(channels, parentId);
        }
        // 查询其下的行政区划和摄像机
        List<SipDeviceChannel> channelsForCivilCode = sipDeviceChannelMapper.selectChannelWithCivilCodeAndLength(deviceId, parentId, parentId.length() + 2);
        if (!onlyCatalog) {
            List<SipDeviceChannel> channels = sipDeviceChannelMapper.selectChannelByCivilCode(deviceId, parentId);

            for (SipDeviceChannel channel : channels) {
                boolean flag = false;
                for (SipDeviceChannel deviceChannel : channelsForCivilCode) {
                    if (channel.getChannelSipId().equals(deviceChannel.getChannelSipId())) {
                        flag = true;
                    }
                }
                if (!flag) {
                    channelsForCivilCode.add(channel);
                }
            }
        }
        return transportChannelsToTree(channelsForCivilCode, parentId);
    }


    /**
     * 根据channelId获取绑定的设备或场景
     *
     * @param channelId
     * @return
     */
    @Override
    public BindingChannel getBindingChannel(String channelId) {
        return sipDeviceChannelMapper.getBindingChannel(channelId);
    }

    @Override
    public List<SipDeviceChannel> listRelDeviceOrScene(String serialNumber, Long sceneModelId) {
        List<SipDeviceChannel> sipDeviceChannelList = new ArrayList<>();
        if (StringUtils.isNotEmpty(serialNumber)) {
            sipDeviceChannelList = sipDeviceChannelMapper.selectDeviceRelSipDeviceChannelList(serialNumber);
        }
        if (null != sceneModelId) {
            sipDeviceChannelList = sipDeviceChannelMapper.selectSceneRelSipDeviceChannelList(sceneModelId);
        }
        for (SipDeviceChannel sipDeviceChannel : sipDeviceChannelList) {
            Stream play = playService.play(String.valueOf(sipDeviceChannel.getDeviceSipId()), sipDeviceChannel.getChannelSipId(), false);
            if (null != play) {
                sipDeviceChannel.setPlayUrl(play.getPlayurl());
            }
        }
        return sipDeviceChannelList;
    }


    private List<SipDeviceChannel> getRootNodes(String deviceId, boolean haveCatalog, boolean haveChannel) {
        if (!haveCatalog && !haveChannel) {
            return null;
        }
        List<SipDeviceChannel> result = new ArrayList<>();
        // 使用行政区划
        Integer length = sipDeviceChannelMapper.getChannelMinLength(deviceId);
        if (length == null) {
            return null;
        }
        if (length <= 10) {
            if (haveCatalog) {
                List<SipDeviceChannel> provinceNode = sipDeviceChannelMapper.selectChannelWithCivilCodeAndLength(deviceId, null, length);
                if (provinceNode != null && provinceNode.size() > 0) {
                    result.addAll(provinceNode);
                }
            }
            if (haveChannel) {
                // 查询那些civilCode不在通道中的不规范通道，放置在根目录
                List<SipDeviceChannel> nonstandardNode = sipDeviceChannelMapper.selectChannelWithoutCiviCode(deviceId);
                if (nonstandardNode != null && nonstandardNode.size() > 0) {
                    result.addAll(nonstandardNode);
                }
            }
        } else {
            if (haveChannel) {
                List<SipDeviceChannel> deviceChannels = sipDeviceChannelMapper.selectSipDeviceChannelByDeviceSipId(deviceId);
                if (deviceChannels != null && deviceChannels.size() > 0) {
                    result.addAll(deviceChannels);
                }
            }
        }
        return result;
    }

    private List<BaseTree<SipDeviceChannel>> transportChannelsToTree(List<SipDeviceChannel> channels, String parentId) {
        if (channels == null) {
            return null;
        }
        List<BaseTree<SipDeviceChannel>> treeNotes = new ArrayList<>();
        if (channels.size() == 0) {
            return treeNotes;
        }
        for (SipDeviceChannel channel : channels) {
            BaseTree<SipDeviceChannel> node = new BaseTree<>();
            node.setId(channel.getChannelSipId());
            node.setDeviceId(channel.getDeviceSipId());
            node.setName(channel.getChannelName());
            node.setPid(parentId);
            node.setBasicData(channel);
            node.setParent(false);
            if (channel.getChannelSipId().length() > 8) {
                if (channel.getChannelSipId().length() > 13) {
                    String gbCodeType = channel.getChannelSipId().substring(10, 13);
                    node.setParent(gbCodeType.equals(SipUtil.BUSINESS_GROUP) || gbCodeType.equals(SipUtil.VIRTUAL_ORGANIZATION));
                }
            } else {
                node.setParent(true);
            }
            treeNotes.add(node);
        }
        Collections.sort(treeNotes);
        return treeNotes;
    }
}
