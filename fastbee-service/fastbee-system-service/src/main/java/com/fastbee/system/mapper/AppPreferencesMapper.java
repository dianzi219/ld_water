package com.fastbee.system.mapper;


import com.fastbee.system.domain.AppPreferences;

import java.util.List;

/**
 * APP用户偏好设置Mapper接口
 */
public interface AppPreferencesMapper
{
    /**
     * 查询APP用户偏好设置
     *
     * @param id APP用户偏好设置主键
     * @return APP用户偏好设置
     */
    public AppPreferences selectAppPreferencesById(Long id);

    /**
     * 查询APP用户偏好设置列表
     *
     * @param appPreferences APP用户偏好设置
     * @return APP用户偏好设置集合
     */
    public List<AppPreferences> selectAppPreferencesList(AppPreferences appPreferences);

    /**
     * 通过用户ID查询APP用户偏好设置
     * @param userId 用户ID
     * @return
     */
    public AppPreferences selectAppPreferencesByUserId(Long userId);

    /**
     * 新增APP用户偏好设置
     *
     * @param appPreferences APP用户偏好设置
     * @return 结果
     */
    public int insertAppPreferences(AppPreferences appPreferences);

    /**
     * 修改APP用户偏好设置
     *
     * @param appPreferences APP用户偏好设置
     * @return 结果
     */
    public int updateAppPreferences(AppPreferences appPreferences);

    /**
     * 删除APP用户偏好设置
     *
     * @param userId APP用户偏好设置主键
     * @return 结果
     */
    public int deleteAppPreferencesByUserId(Long userId);

    /**
     * 批量删除APP用户偏好设置
     *
     * @param userIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteAppPreferencesByUserIds(Long[] userIds);
}
