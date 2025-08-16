package com.fastbee.iot.service.impl;

import java.util.List;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fastbee.iot.mapper.CommandPreferencesMapper;
import com.fastbee.iot.domain.CommandPreferences;
import com.fastbee.iot.service.ICommandPreferencesService;

import javax.annotation.Resource;

/**
 * 指令偏好设置Service业务层处理
 *
 * @author kerwincui
 * @date 2024-06-29
 */
@Service
public class CommandPreferencesServiceImpl implements ICommandPreferencesService
{
    @Resource
    private CommandPreferencesMapper commandPreferencesMapper;

    /**
     * 查询指令偏好设置
     *
     * @param id 指令偏好设置主键
     * @return 指令偏好设置
     */
    @Override
    public CommandPreferences selectCommandPreferencesById(Long id)
    {
        CommandPreferences commandPreferences = commandPreferencesMapper.selectCommandPreferencesById(id);
        JSONObject jsonObject = JSONObject.parseObject(commandPreferences.getCommand());
        String command = jsonObject.getString("command");
        commandPreferences.setCommand(command);
        return commandPreferences;
    }

    /**
     * 查询指令偏好设置列表
     *
     * @param commandPreferences 指令偏好设置
     * @return 指令偏好设置
     */
    @Override
    public List<CommandPreferences> selectCommandPreferencesList(CommandPreferences commandPreferences)
    {
        List<CommandPreferences> list = commandPreferencesMapper.selectCommandPreferencesList(commandPreferences);
        for (CommandPreferences preferences : list) {
            JSONObject jsonObject = JSONObject.parseObject(preferences.getCommand());
            String command = jsonObject.getString("command");
            preferences.setCommand(command);
        }
        return list;
    }

    /**
     * 新增指令偏好设置
     *
     * @param commandPreferences 指令偏好设置
     * @return 结果
     */
    @Override
    public int insertCommandPreferences(CommandPreferences commandPreferences)
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command",commandPreferences.getCommand());
        commandPreferences.setCommand(JSONObject.toJSONString(jsonObject));
        return commandPreferencesMapper.insertCommandPreferences(commandPreferences);
    }

    /**
     * 修改指令偏好设置
     *
     * @param commandPreferences 指令偏好设置
     * @return 结果
     */
    @Override
    public int updateCommandPreferences(CommandPreferences commandPreferences)
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command",commandPreferences.getCommand());
        commandPreferences.setCommand(JSONObject.toJSONString(jsonObject));
        return commandPreferencesMapper.updateCommandPreferences(commandPreferences);
    }

    /**
     * 批量删除指令偏好设置
     *
     * @param ids 需要删除的指令偏好设置主键
     * @return 结果
     */
    @Override
    public int deleteCommandPreferencesByIds(Long[] ids)
    {
        return commandPreferencesMapper.deleteCommandPreferencesByIds(ids);
    }

    /**
     * 删除指令偏好设置信息
     *
     * @param id 指令偏好设置主键
     * @return 结果
     */
    @Override
    public int deleteCommandPreferencesById(Long id)
    {
        return commandPreferencesMapper.deleteCommandPreferencesById(id);
    }
}
