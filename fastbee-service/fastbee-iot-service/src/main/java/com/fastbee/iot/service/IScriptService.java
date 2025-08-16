package com.fastbee.iot.service;

import java.util.List;

import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.iot.domain.Script;
import com.fastbee.iot.model.ScriptCondition;
import com.fastbee.iot.ruleEngine.MsgContext;

/**
 * 规则引擎脚本Service接口
 *
 * @author lizhuangpeng
 * @date 2023-07-01
 */
public interface IScriptService
{
    /**
     * 查询规则引擎脚本
     *
     * @param scriptId 规则引擎脚本主键
     * @return 规则引擎脚本
     */
    public Script selectRuleScriptById(String scriptId);

    /**
     * 查询规则脚本日志
     *
     * @param scriptId 规则引擎脚本主键
     * @return 规则引擎脚本
     */
    public String selectRuleScriptLog(String scriptId);
    public void openScriptLog(String scriptId);
    public void closeScriptLog(String scriptId);
    /**
     * 查询规则引擎脚本列表
     *
     * @param ruleScript 规则引擎脚本
     * @return 规则引擎脚本集合
     */
    public List<Script> selectRuleScriptList(Script ruleScript);
    public List<Script> selectExecRuleScriptList(ScriptCondition ruleScript);

    /**
     * 查询规则引擎脚本标识数组（设备用户和租户的脚本）
     *
     * @return 规则引擎脚本
     */
    public String[] selectRuleScriptIdArray(ScriptCondition scriptCondition);

    /**
     * 新增规则引擎脚本
     *
     * @param ruleScript 规则引擎脚本
     * @return 结果
     */
    public int insertRuleScript(Script ruleScript);

    /**
     * 修改规则引擎脚本
     *
     * @param ruleScript 规则引擎脚本
     * @return 结果
     */
    public int updateRuleScript(Script ruleScript);

    /**
     * 批量删除规则引擎脚本
     *
     * @param ids 需要删除的规则引擎脚本主键集合
     * @return 结果
     */
    public int deleteRuleScriptByIds(String[] ids);

    /**
     * 删除规则引擎脚本信息
     *
     * @param id 规则引擎脚本主键
     * @return 结果
     */
    public int deleteRuleScriptById(String id);


    /**
     * 验证脚本
     * @param ruleScript 脚本数据
     * @return
     */
    public AjaxResult validateScript(Script ruleScript);

    /**
     * 批量删除规则脚本
     *
     * @param sceneIds 需要删除的数据场景ID集合
     * @return 结果
     */
    public int deleteRuleScriptBySceneIds(Long[] sceneIds);

    /**
     * 批量新增场景脚本
     *
     * @param ruleScriptList 场景联动触发器集合
     * @return 结果
     */
    public int insertRuleScriptList(List<Script> ruleScriptList);
    public MsgContext execRuleScript(ScriptCondition scriptCondition, Object... contextBeanArray);
}
