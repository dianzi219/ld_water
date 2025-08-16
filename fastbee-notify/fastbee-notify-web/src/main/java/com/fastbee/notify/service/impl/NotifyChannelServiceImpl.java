package com.fastbee.notify.service.impl;

import com.fastbee.common.core.domain.entity.SysDictData;
import com.fastbee.common.core.domain.entity.SysRole;
import com.fastbee.common.core.domain.entity.SysUser;
import com.fastbee.common.core.notify.NotifyConfigVO;
import com.fastbee.common.enums.NotifyChannelProviderEnum;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.notify.domain.NotifyChannel;
import com.fastbee.notify.domain.NotifyTemplate;
import com.fastbee.notify.mapper.NotifyChannelMapper;
import com.fastbee.notify.mapper.NotifyTemplateMapper;
import com.fastbee.notify.service.INotifyChannelService;
import com.fastbee.notify.vo.ChannelProviderVO;
import com.fastbee.system.service.ISysDictDataService;
import org.apache.commons.collections4.CollectionUtils;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.fastbee.common.utils.SecurityUtils.getLoginUser;

/**
 * 通知渠道Service业务层处理
 *
 * @author kerwincui
 * @date 2023-12-01
 */
@Service
public class NotifyChannelServiceImpl implements INotifyChannelService
{
    @Resource
    private NotifyChannelMapper notifyChannelMapper;
    @Resource
    private ISysDictDataService sysDictDataService;
    @Resource
    private NotifyTemplateMapper notifyTemplateMapper;

    /**
     * 查询通知渠道
     *
     * @param id 通知渠道主键
     * @return 通知渠道
     */
    @Override
    public NotifyChannel selectNotifyChannelById(Long id)
    {
        return notifyChannelMapper.selectNotifyChannelById(id);
    }

    /**
     * 查询通知渠道列表
     *
     * @param notifyChannel 通知渠道
     * @return 通知渠道
     */
    @Override
    public List<NotifyChannel> selectNotifyChannelList(NotifyChannel notifyChannel)
    {
        SysUser user = getLoginUser().getUser();
//        List<SysRole> roles=user.getRoles();
//        // 租户
//        if(roles.stream().anyMatch(a-> "tenant".equals(a.getRoleKey()))){
//            notifyChannel.setTenantId(user.getUserId());
//        }
        // 查询所属机构
        if (null != user.getDeptId()) {
            notifyChannel.setTenantId(user.getDept().getDeptUserId());
        } else {
            notifyChannel.setTenantId(user.getUserId());
        }
        return notifyChannelMapper.selectNotifyChannelList(notifyChannel);
    }

    /**
     * 新增通知渠道
     *
     * @param notifyChannel 通知渠道
     * @return 结果
     */
    @Override
    public int insertNotifyChannel(NotifyChannel notifyChannel)
    {
        SysUser user = getLoginUser().getUser();
        if (null == user.getDeptId()) {
            throw new ServiceException("只允许租户配置");
        }
        notifyChannel.setTenantId(user.getDept().getDeptUserId());
        notifyChannel.setTenantName(user.getDept().getDeptUserName());
        return notifyChannelMapper.insertNotifyChannel(notifyChannel);
    }

    /**
     * 修改通知渠道
     *
     * @param notifyChannel 通知渠道
     * @return 结果
     */
    @Override
    public int updateNotifyChannel(NotifyChannel notifyChannel)
    {
        notifyChannel.setUpdateTime(DateUtils.getNowDate());
        List<NotifyTemplate> notifyTemplateList = notifyTemplateMapper.selectNotifyTemplateByChannelId(notifyChannel.getId());
        for (NotifyTemplate notifyTemplate : notifyTemplateList) {
            SmsFactory.unregister(notifyTemplate.getId().toString());
        }
        return notifyChannelMapper.updateNotifyChannel(notifyChannel);
    }

    /**
     * 批量删除通知渠道
     *
     * @param ids 需要删除的通知渠道主键
     * @return 结果
     */
    @Override
    public int deleteNotifyChannelByIds(Long[] ids)
    {
        int result = notifyChannelMapper.deleteNotifyChannelByIds(ids);
        // 删除渠道下的模板
        if (result > 0) {
            notifyTemplateMapper.deleteNotifyTemplateByChannelIds(ids);
        }
        return result;
    }

    /**
     * 删除通知渠道信息
     *
     * @param id 通知渠道主键
     * @return 结果
     */
    @Override
    public int deleteNotifyChannelById(Long id)
    {
        int result = notifyChannelMapper.deleteNotifyChannelById(id);
        // 删除渠道下的模板
        if (result > 0) {
            notifyTemplateMapper.deleteNotifyTemplateByChannelIds(new Long[]{id});
        }
        return result;
    }

    @Override
    public List<ChannelProviderVO> listChannel() {
        SysDictData sysDictData = new SysDictData();
        sysDictData.setDictType("notify_channel_type");
        sysDictData.setStatus("0");
        List<SysDictData> parentDataList = sysDictDataService.selectDictDataList(sysDictData);
        if (CollectionUtils.isEmpty(parentDataList)) {
            return new ArrayList<>();
        }
        List<String> dictValueList = parentDataList.stream().map(SysDictData::getDictValue).collect(Collectors.toList());
        List<String> dictTypeList = new ArrayList<>();
        for (String s : dictValueList) {
            dictTypeList.add("notify_channel_" + s + "_provider");
        }
        List<SysDictData> childerDataList = sysDictDataService.selectDictDataListByDictTypes(dictTypeList);
        Map<String, List<SysDictData>> map = childerDataList.stream().collect(Collectors.groupingBy(SysDictData::getDictType));
        List<ChannelProviderVO> result = new ArrayList<>();
        for (SysDictData dictData : parentDataList) {
            ChannelProviderVO channelProviderVO = new ChannelProviderVO();
            channelProviderVO.setChannelType(dictData.getDictValue());
            channelProviderVO.setChannelName(dictData.getDictLabel());
            String key = "notify_channel_" + dictData.getDictValue() + "_provider";
            if (!map.containsKey(key)) {
                result.add(channelProviderVO);
                continue;
            }
            List<SysDictData> dataList = map.get(key);
            List<ChannelProviderVO.Provider> providerList = new ArrayList<>();
            for (SysDictData data : dataList) {
                ChannelProviderVO.Provider provider = new ChannelProviderVO.Provider();
                provider.setProvider(data.getDictValue());
                provider.setProviderName(data.getDictLabel());
                provider.setCategory(dictData.getDictValue());
                providerList.add(provider);
            }
            channelProviderVO.setProviderList(providerList);
            result.add(channelProviderVO);
        }
        return result;
    }

    @Override
    public List<NotifyConfigVO> getConfigContent(String channelType, String provider) {
        return NotifyChannelProviderEnum.getConfigContent(Objects.requireNonNull(NotifyChannelProviderEnum.getByChannelTypeAndProvider(channelType, provider)));
    }
}
