package com.fastbee.system.service.impl;


import com.fastbee.common.utils.DateUtils;
import com.fastbee.common.utils.SecurityUtils;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.system.domain.AppPreferences;

import com.fastbee.system.enums.Language;
import com.fastbee.system.mapper.AppPreferencesMapper;
import com.fastbee.system.service.IAppPreferencesService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;


/**
 * APP用户偏好设置Service业务层处理
 */
@Service
public class AppPreferencesServiceImpl implements IAppPreferencesService
{
    @Resource
    private AppPreferencesMapper appPreferencesMapper;

    /**
     * 查询APP用户偏好设置
     *
     * @param userId APP用户偏好设置主键
     * @return APP用户偏好设置
     */
    @Override
    public AppPreferences selectAppPreferencesByUserId(Long userId)
    {
        AppPreferences appPreferences = appPreferencesMapper.selectAppPreferencesByUserId(userId);
        if (appPreferences == null){
            appPreferences = new AppPreferences();
        }
        // 设置默认语言
        if (StringUtils.isEmpty(appPreferences.getLanguage())) {
            appPreferences.setLanguage(Language.DEFAULT.getValue());
        }
        return appPreferences;
    }

    /**
     * 查询APP用户偏好设置列表
     *
     * @param appPreferences APP用户偏好设置
     * @return APP用户偏好设置
     */
    @Override
    public List<AppPreferences> selectAppPreferencesList(AppPreferences appPreferences)
    {
        return appPreferencesMapper.selectAppPreferencesList(appPreferences);
    }

    /**
     * 新增APP用户偏好设置
     *
     * @param appPreferences APP用户偏好设置
     * @return 结果
     */
    @Override
    public int insertAppPreferences(AppPreferences appPreferences)
    {
        appPreferences.setCreateTime(DateUtils.getNowDate());
        return appPreferencesMapper.insertAppPreferences(appPreferences);
    }

    /**
     * 修改APP用户偏好设置
     *
     * @param appPreferences APP用户偏好设置
     * @return 结果
     */
    @Override
    public int updateAppPreferences(AppPreferences appPreferences)
    {
        appPreferences.setUpdateTime(DateUtils.getNowDate());
        return appPreferencesMapper.updateAppPreferences(appPreferences);
    }

    /**
     * 批量删除APP用户偏好设置
     *
     * @param userIds 需要删除的APP用户偏好设置主键
     * @return 结果
     */
    @Override
    public int deleteAppPreferencesByUserIds(Long[] userIds)
    {
        return appPreferencesMapper.deleteAppPreferencesByUserIds(userIds);
    }

    /**
     * 删除APP用户偏好设置信息
     *
     * @param userId APP用户偏好设置主键
     * @return 结果
     */
    @Override
    public int deleteAppPreferencesByUserId(Long userId)
    {
        return appPreferencesMapper.deleteAppPreferencesByUserId(userId);
    }

    /**
     * 新增或者更新用户偏好设置
     * @param appPreferences
     * @return
     */
    @Override
    public int addOrUpdate(AppPreferences appPreferences)
    {
        if (Objects.isNull(appPreferences.getId())){
            appPreferences.setUserId(SecurityUtils.getUserId());
        }
        AppPreferences preferences = this.selectAppPreferencesByUserId(SecurityUtils.getUserId());
        if (!Objects.isNull(preferences)){
            return appPreferencesMapper.updateAppPreferences(appPreferences);
        } else {
            return  appPreferencesMapper.insertAppPreferences(appPreferences);
        }
    }

    /**
     * 获取用户偏好语言
     * @param userId
     * @return
     */
    @Override
    public String getLanguage(Long userId) {
        AppPreferences appPreferences = selectAppPreferencesByUserId(userId);
        return appPreferences.getLanguage();
    }

}
