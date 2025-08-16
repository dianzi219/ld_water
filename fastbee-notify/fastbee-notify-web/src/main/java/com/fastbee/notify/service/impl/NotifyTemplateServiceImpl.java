package com.fastbee.notify.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.common.core.domain.entity.SysRole;
import com.fastbee.common.core.domain.entity.SysUser;
import com.fastbee.common.core.domain.model.LoginUser;
import com.fastbee.common.core.notify.NotifyConfigVO;
import com.fastbee.common.enums.NotifyChannelEnum;
import com.fastbee.common.enums.NotifyChannelProviderEnum;
import com.fastbee.common.enums.NotifyServiceCodeEnum;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.notify.domain.NotifyChannel;
import com.fastbee.notify.domain.NotifyTemplate;
import com.fastbee.notify.mapper.NotifyChannelMapper;
import com.fastbee.notify.mapper.NotifyTemplateMapper;
import com.fastbee.notify.service.INotifyTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.fastbee.common.utils.SecurityUtils.getLoginUser;

/**
 * 通知模版Service业务层处理
 *
 * @author kerwincui
 * @date 2023-12-01
 */
@Service
@Slf4j
public class NotifyTemplateServiceImpl implements INotifyTemplateService {
    @Resource
    private NotifyTemplateMapper notifyTemplateMapper;
    @Resource
    private NotifyChannelMapper notifyChannelMapper;


    /**
     * 查询通知模版
     *
     * @param id 通知模版主键
     * @return 通知模版
     */
    @Override
    public NotifyTemplate selectNotifyTemplateById(Long id) {
        return notifyTemplateMapper.selectNotifyTemplateById(id);
    }

    /**
     * 查询通知模版列表
     *
     * @param notifyTemplate 通知模版
     * @return 通知模版
     */
    @Override
    public List<NotifyTemplate> selectNotifyTemplateList(NotifyTemplate notifyTemplate) {
        SysUser user = getLoginUser().getUser();
//        List<SysRole> roles=user.getRoles();
//        // 租户
//        if(roles.stream().anyMatch(a-> "tenant".equals(a.getRoleKey()))){
//            notifyTemplate.setTenantId(user.getUserId());
//        }
        // 查询所属机构
        if (null != user.getDeptId()) {
            notifyTemplate.setTenantId(user.getDept().getDeptUserId());
        } else {
            notifyTemplate.setTenantId(user.getUserId());
        }
        List<NotifyTemplate> notifyTemplates = notifyTemplateMapper.selectNotifyTemplateList(notifyTemplate);
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(notifyTemplates)) {
            return notifyTemplates;
        }
        List<Long> collect = notifyTemplates.stream().map(NotifyTemplate::getChannelId).collect(Collectors.toList());
        List<NotifyChannel> notifyChannelList = notifyChannelMapper.selectNotifyChannelByIds(collect);
        Map<Long, NotifyChannel> notifyChannelMap = notifyChannelList.stream().collect(Collectors.toMap(NotifyChannel::getId, Function.identity()));
        for (NotifyTemplate template : notifyTemplates) {
            if (notifyChannelMap.containsKey(template.getChannelId())) {
                NotifyChannel notifyChannel = notifyChannelMap.get(template.getChannelId());
                template.setChannelName(notifyChannel.getName());
            }
        }
        return notifyTemplates;
    }

    /**
     * 新增通知模版
     *
     * @param notifyTemplate 通知模版
     * @return 结果
     */
    @Override
    public AjaxResult insertNotifyTemplate(NotifyTemplate notifyTemplate) {
        SysUser user = getLoginUser().getUser();
        if (null == user.getDeptId()) {
            throw new ServiceException("只允许租户配置");
        }
        notifyTemplate.setTenantId(user.getDept().getDeptUserId());
        notifyTemplate.setTenantName(user.getDept().getDeptUserName());
        notifyTemplate.setCreateTime(DateUtils.getNowDate());
        return notifyTemplateMapper.insertNotifyTemplate(notifyTemplate) > 0 ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     * 修改通知模版
     *
     * @param notifyTemplate 通知模版
     * @return 结果
     */
    @Override
    public AjaxResult updateNotifyTemplate(NotifyTemplate notifyTemplate) {
        notifyTemplate.setUpdateTime(DateUtils.getNowDate());
        if (NotifyChannelEnum.SMS.getType().equals(notifyTemplate.getChannelType())) {
            SmsFactory.unregister(notifyTemplate.getId().toString());
        }
        return notifyTemplateMapper.updateNotifyTemplate(notifyTemplate) > 0 ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     * 批量删除通知模版
     *
     * @param ids 需要删除的通知模版主键
     * @return 结果
     */
    @Override
    public int deleteNotifyTemplateByIds(Long[] ids) {
        int i = notifyTemplateMapper.deleteNotifyTemplateByIds(ids);
        if (i > 0) {
            notifyTemplateMapper.deleteAlertNotifyTemplateByNotifyTemplateIds(ids);
        }
        return i;
    }

    /**
     * 删除通知模版信息
     *
     * @param id 通知模版主键
     * @return 结果
     */
    @Override
    public int deleteNotifyTemplateById(Long id) {
        int i = notifyTemplateMapper.deleteNotifyTemplateById(id);
        if (i > 0) {
            notifyTemplateMapper.deleteAlertNotifyTemplateByNotifyTemplateIds(new Long[]{id});
        }
        return i;
    }

    /**
     * 查询某一业务通知通道是否有启动的（业务编码唯一启用一个模板）
     * @param notifyTemplate 通知模板
     */
    @Override
    public Integer countNormalTemplate(NotifyTemplate notifyTemplate){
        LoginUser loginUser = getLoginUser();
        assert !Objects.isNull(notifyTemplate.getServiceCode()) : "业务编码不能为空";
        NotifyTemplate selectOne = this.getEnableQueryCondition(notifyTemplate.getServiceCode(), notifyTemplate.getChannelType(), notifyTemplate.getProvider(), loginUser.getUser().getDept().getDeptUserId());
        selectOne.setId(notifyTemplate.getId());
        return notifyTemplateMapper.selectEnableNotifyTemplateCount(selectOne);
    }

    /**
     * 获取唯一启用模版查询条件
     * 唯一启用条件：同一业务编码的模板短信、语音、邮箱渠道分别可以启用一个，微信、钉钉渠道下不同服务商分别可以启用一个
     * @param: serviceCode
     * @param: channelType
     * @param: provider
     * @return com.fastbee.notify.domain.NotifyTemplate
     */
    @Override
    public NotifyTemplate getEnableQueryCondition(String serviceCode, String channelType, String provider, Long tenantId) {
        NotifyTemplate notifyTemplate = new NotifyTemplate();
        notifyTemplate.setServiceCode(serviceCode);
        notifyTemplate.setStatus(1);
        notifyTemplate.setTenantId(tenantId);
        NotifyChannelEnum notifyChannelEnum = NotifyChannelEnum.getNotifyChannelEnum(channelType);
        switch (Objects.requireNonNull(notifyChannelEnum)) {
            case SMS:
            case VOICE:
            case EMAIL:
                notifyTemplate.setChannelType(channelType);
                break;
            case WECHAT:
            case DING_TALK:
                notifyTemplate.setChannelType(channelType);
                notifyTemplate.setProvider(provider);
                break;
            default:
                break;
        }
        return notifyTemplate;
    }

    /**
     * 更新某一类型为不可用状态，选中的为可用状态
     * @param notifyTemplate 通知模板
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTemplateStatus(NotifyTemplate notifyTemplate){
        LoginUser loginUser = getLoginUser();
        // 查询所有统一类型可用的渠道
        NotifyTemplate selectEnable = this.getEnableQueryCondition(notifyTemplate.getServiceCode(), notifyTemplate.getChannelType(), notifyTemplate.getProvider(), loginUser.getUser().getDept().getDeptUserId());
        selectEnable.setId(notifyTemplate.getId());
        List<NotifyTemplate> notifyTemplateList = this.selectNotifyTemplateList(selectEnable);
        if (!CollectionUtils.isEmpty(notifyTemplateList)){
            //如果有同一类型的渠道为可用，要先将更新为不可用
            List<Long> ids = notifyTemplateList.stream().map(NotifyTemplate::getId).filter(id -> !Objects.equals(id, notifyTemplate.getId())).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(ids)) {
                notifyTemplateMapper.updateNotifyBatch(ids, 0);
            }
        }
        //更新选中的为可用状态
        NotifyTemplate updateBo = new NotifyTemplate();
        updateBo.setStatus(1);
        updateBo.setId(notifyTemplate.getId());
        notifyTemplateMapper.updateNotifyTemplate(updateBo);
    }

    @Override
    public NotifyTemplate selectOnlyEnable(NotifyTemplate notifyTemplate) {
        return notifyTemplateMapper.selectOnlyEnable(notifyTemplate);
    }

    @Override
    public List<NotifyConfigVO> getNotifyMsgParams(Long channelId, String msgType) {
        NotifyChannel notifyChannel = notifyChannelMapper.selectNotifyChannelById(channelId);
        if (Objects.isNull(notifyChannel)) {
            return new ArrayList<>();
        }
        NotifyChannelProviderEnum notifyChannelProviderEnum = NotifyChannelProviderEnum.getByChannelTypeAndProvider(notifyChannel.getChannelType(), notifyChannel.getProvider());
        return NotifyChannelProviderEnum.getMsgParams(notifyChannelProviderEnum, msgType);
    }

    @Override
    public List<String> listVariables(String content, NotifyChannelProviderEnum notifyChannelProviderEnum) {
        List<String> variables;
        switch (Objects.requireNonNull(notifyChannelProviderEnum)) {
            case WECHAT_MINI_PROGRAM:
            case WECHAT_PUBLIC_ACCOUNT:
                variables = StringUtils.getWeChatMiniVariables(content);
                break;
            case SMS_TENCENT:
            case VOICE_TENCENT:
                variables = StringUtils.getVariables("{}", content);
                break;
            case EMAIL_QQ:
            case EMAIL_163:
                variables = StringUtils.getVariables("#{}", content);
                break;
            default:
                variables = StringUtils.getVariables("${}", content);
                break;
        }
        return variables;
    }

    @Override
    public String getAlertWechatMini() {
        NotifyTemplate selectOne = new NotifyTemplate();
        selectOne.setServiceCode(NotifyServiceCodeEnum.ALERT.getServiceCode()).setChannelType(NotifyChannelProviderEnum.WECHAT_MINI_PROGRAM.getChannelType()).setProvider(NotifyChannelProviderEnum.WECHAT_MINI_PROGRAM.getProvider()).setStatus(1);
        SysUser user = getLoginUser().getUser();
        if (null != user.getDeptId()) {
            selectOne.setTenantId(user.getDept().getDeptUserId());
        } else {
            selectOne.setTenantId(1L);
        }
        NotifyTemplate notifyTemplate = notifyTemplateMapper.selectOnlyEnable(selectOne);
        if (notifyTemplate == null || StringUtils.isEmpty(notifyTemplate.getMsgParams())) {
            return "";
        }
        JSONObject jsonObject = JSONObject.parseObject(notifyTemplate.getMsgParams());
        return jsonObject.get("templateId").toString();
    }

}
