package com.fastbee.iot.mapper;

import java.util.List;
import com.fastbee.iot.domain.CommandPreferences;

/**
 * 指令偏好设置Mapper接口
 *
 * @author kerwincui
 * @date 2024-06-29
 */
public interface CommandPreferencesMapper
{
    /**
     * 查询指令偏好设置
     *
     * @param id 指令偏好设置主键
     * @return 指令偏好设置
     */
    public CommandPreferences selectCommandPreferencesById(Long id);

    /**
     * 查询指令偏好设置列表
     *
     * @param commandPreferences 指令偏好设置
     * @return 指令偏好设置集合
     */
    public List<CommandPreferences> selectCommandPreferencesList(CommandPreferences commandPreferences);

    /**
     * 新增指令偏好设置
     *
     * @param commandPreferences 指令偏好设置
     * @return 结果
     */
    public int insertCommandPreferences(CommandPreferences commandPreferences);

    /**
     * 修改指令偏好设置
     *
     * @param commandPreferences 指令偏好设置
     * @return 结果
     */
    public int updateCommandPreferences(CommandPreferences commandPreferences);

    /**
     * 删除指令偏好设置
     *
     * @param id 指令偏好设置主键
     * @return 结果
     */
    public int deleteCommandPreferencesById(Long id);

    /**
     * 批量删除指令偏好设置
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCommandPreferencesByIds(Long[] ids);
}
