package com.fastbee.iot.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.qos.logback.classic.LoggerContext;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.fastbee.common.core.domain.AjaxResult;
import com.fastbee.common.core.domain.entity.SysUser;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.iot.domain.Script;
import com.fastbee.iot.mapper.ScriptMapper;
import com.fastbee.iot.ruleEngine.MsgContext;
import com.fastbee.iot.service.IScriptService;
import com.fastbee.iot.model.ScriptCondition;
import com.fastbee.ruleEngine.core.FlowLogExecutor;
import com.yomahub.liteflow.builder.LiteFlowNodeBuilder;
import com.yomahub.liteflow.builder.el.LiteFlowChainELBuilder;
import com.yomahub.liteflow.flow.FlowBus;
import com.yomahub.liteflow.flow.LiteflowResponse;
import com.yomahub.liteflow.script.ScriptExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.File;
import org.slf4j.MDC;

import static com.fastbee.common.core.domain.AjaxResult.success;
import static com.fastbee.common.utils.SecurityUtils.getLoginUser;

/**
 * 规则引擎脚本Service业务层处理
 *
 * @author lizhuangpeng
 * @date 2023-07-01
 */
@Slf4j
@Service
public class ScriptServiceImpl implements IScriptService {

    private static final Logger rule_logger = LoggerFactory.getLogger("script");

    @Autowired
    private ScriptMapper ruleScriptMapper;

    @Autowired
    private FlowLogExecutor flowExecutor;

    /**
     * 查询规则引擎脚本
     *
     * @param scriptId 规则引擎脚本主键
     * @return 规则引擎脚本
     */
    @Override
    public Script selectRuleScriptById(String scriptId) {
        return ruleScriptMapper.selectRuleScriptById(scriptId);
    }

    /**
     * 查询规则引擎脚本日志
     *
     * @param scriptId 规则引擎脚本主键
     * @return 规则引擎脚本
     */
    @Override
    public String selectRuleScriptLog(String scriptId) {
        // 获取日志存储路径
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        String path = loggerContext.getProperty("log.path");

        // 倒叙读取500条日志
        try{
            List<String> lines = new ArrayList<>();
            ReversedLinesFileReader reader = new ReversedLinesFileReader(new File(path+"/script/"+scriptId+".log"));
            String line = "";
            while ((line = reader.readLine()) != null && lines.size() < 500) {
                lines.add(line);
            }
            Collections.reverse(lines);
            return String.join("\n", lines);
        } catch (IOException e) {
            return "暂无日志,详情如下：\n"+e.toString();
        }
    }

    @Override
    public void openScriptLog(String scriptId) {
        flowExecutor.openScriptLog(scriptId);
    }

    @Override
    public void closeScriptLog(String scriptId) {
        flowExecutor.closeScriptLog(scriptId);
    }

    /**
     * 查询规则引擎脚本列表
     *
     * @param ruleScript 规则引擎脚本
     * @return 规则引擎脚本
     */
    @Override
    public List<Script> selectRuleScriptList(Script ruleScript) {
        return ruleScriptMapper.selectRuleScriptList(ruleScript);
    }

    @Override
    public List<Script> selectExecRuleScriptList(ScriptCondition ruleScript) {
        return ruleScriptMapper.selectExecRuleScriptList(ruleScript);
    }

    /**
     * 查询规则引擎脚本标识数组（设备用户和租户的脚本）
     *
     * @return 规则引擎脚本
     */
    @Override
    public String[] selectRuleScriptIdArray(ScriptCondition scriptCondition) {
        return ruleScriptMapper.selectRuleScriptIdArray(scriptCondition);
    }

    /**
     * 新增规则引擎脚本
     *
     * @param ruleScript 规则引擎脚本
     * @return 结果
     */
    @Override
    public int insertRuleScript(Script ruleScript) {
        // 脚本中引用包替换为许可的包
        ruleScript.setScriptData(replaceAllowPackage(ruleScript.getScriptData()));
        // 设置脚本标识,D=数据流，A=执行动作，T=触发器,雪花算法生成唯一数
        Snowflake snowflake = IdUtil.createSnowflake(1, 1);
        ruleScript.setScriptId("D" + snowflake.nextId());
        SysUser sysUser = getLoginUser().getUser();
        // 归属为机构管理员
        if (null != sysUser.getDeptId()) {
            ruleScript.setUserId(sysUser.getDept().getDeptUserId());
            ruleScript.setUserName(sysUser.getDept().getDeptUserName());
        } else {
            ruleScript.setUserId(sysUser.getUserId());
            ruleScript.setUserName(sysUser.getUserName());
        }
        ruleScript.setCreateTime(DateUtils.getNowDate());
        int result = ruleScriptMapper.insertRuleScript(ruleScript);
        // 动态刷新脚本
        if (result == 1) {
            LiteFlowNodeBuilder builder = null;
            if (ruleScript.getScriptType().equals("script")) {
                builder = LiteFlowNodeBuilder.createScriptNode();
            } else if (ruleScript.getScriptType().equals("switch_script")) {
                builder = LiteFlowNodeBuilder.createScriptSwitchNode();
            } else if (ruleScript.getScriptType().equals("boolean_script")) {
                builder = LiteFlowNodeBuilder.createScriptBooleanNode();
            } else if (ruleScript.getScriptType().equals("for_script")) {
                builder = LiteFlowNodeBuilder.createScriptForNode();
            }
            if (builder != null) {
                builder.setId(ruleScript.getScriptId())
                        .setName(ruleScript.getScriptName())
                        .setScript(ruleScript.getScriptData())
                        .build();
            }
        }
        if ((ruleScript.getScriptEvent() == 5 || ruleScript.getScriptEvent() == 6) && ruleScript.getBridgeId() != 0) {
            ruleScriptMapper.insertScriptBridge(ruleScript.getScriptId(), ruleScript.getBridgeId());
        }
        return result;
    }

    /**
     * 脚本中引用包替换为许可的包
     *
     * @return
     */
    private String replaceAllowPackage(String scriptData) {
        String header = "import cn.hutool.json.JSONArray;\n" +
                "import cn.hutool.json.JSONObject;\n" +
                "import cn.hutool.json.JSONUtil;\n" +
                "import cn.hutool.core.util.NumberUtil;\n";
        // 正则替换import为许可的引用包
        String pattern = "import.*[;\\n\\s]";
        String data = scriptData.replaceAll(pattern, "");
        return header + data;
    }

    /**
     * 修改规则引擎脚本
     *
     * @param ruleScript 规则引擎脚本
     * @return 结果
     */
    @Override
    public int updateRuleScript(Script ruleScript) {
        // 脚本中引用包替换为许可的包
        ruleScript.setScriptData(replaceAllowPackage(ruleScript.getScriptData()));
        ruleScript.setUpdateTime(DateUtils.getNowDate());
        if ((ruleScript.getScriptEvent() == 5 || ruleScript.getScriptEvent() == 6) && ruleScript.getBridgeId() != 0) {
            ruleScriptMapper.updateScriptBridge(ruleScript.getScriptId(), ruleScript.getBridgeId());
        }
        int result = ruleScriptMapper.updateRuleScript(ruleScript);
        // 动态刷新脚本
        if (result == 1) {
            FlowBus.reloadScript(ruleScript.getScriptId(), ruleScript.getScriptData());
        }
        return result;
    }

    /**
     * 批量删除规则引擎脚本
     *
     * @param ids 需要删除的规则引擎脚本主键
     * @return 结果
     */
    @Override
    public int deleteRuleScriptByIds(String[] ids) {
        for (String id : ids) {
            FlowBus.unloadScriptNode(id);
            ruleScriptMapper.deleteScriptBridge(id);
        }
        return ruleScriptMapper.deleteRuleScriptByIds(ids);
    }

    /**
     * 删除规则引擎脚本信息
     *
     * @param id 规则引擎脚本主键
     * @return 结果
     */
    @Override
    public int deleteRuleScriptById(String id) {
        FlowBus.unloadScriptNode(id);
        ruleScriptMapper.deleteScriptBridge(id);
        return ruleScriptMapper.deleteRuleScriptById(id);
    }

    /**
     * 验证脚本
     * ruleScript.scriptData 脚本数据
     *
     * @return
     */
    @Override
    public AjaxResult validateScript(Script ruleScript) {
        // 检查安全性检查
        String pattern = ".*while|for\\s*\\(|InputStream|OutputStream|Reader|Writer|File|Socket.*";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(ruleScript.getScriptData());
        if (m.find()) {
            return success("验证失败，错误信息：" + "不能包含关键词for、while、Reader、Write、File、Socket等", false);
        }
        // 热刷新脚本
        try {
            ScriptExecutorFactory.loadInstance().getScriptExecutor("groovy").load("validateScript", ruleScript.getScriptData());
        } catch (Exception e) {
            return success("验证失败，错误信息：" + e.getMessage(), false);
        }
        return success("验证成功，脚本的实际执行情况可以查看后端日志文件", true);
    }

    @Override
    public int deleteRuleScriptBySceneIds(Long[] sceneIds) {
        for (Long id : sceneIds) {
            Script script = new Script();
            script.setSceneId(id);
            List<Script> list = ruleScriptMapper.selectRuleScriptList(script);
            list.forEach(item -> {
                FlowBus.unloadScriptNode(item.getScriptId());
            });
        }
        return ruleScriptMapper.deleteRuleScriptBySceneIds(sceneIds);
    }

    @Override
    public int insertRuleScriptList(List<Script> ruleScriptList) {
        return ruleScriptMapper.insertRuleScriptList(ruleScriptList);
    }

    @Override
    public MsgContext execRuleScript(ScriptCondition scriptCondition, Object... contextBeanArray) {
        List<Script> scripts = selectExecRuleScriptList(scriptCondition);
        //如果查询不到脚本，则认为是不用处理
        if (Objects.isNull(scripts) || scripts.isEmpty()) {
            return null;
        }

        LiteflowResponse response = null;
        for (Script script : scripts) {
            // 打印信息到日志
            MDC.put("scriptId", script.getScriptId());  // 传递参数到日志配置中
            rule_logger.info("执行规则引擎脚本开始：" + script.getScriptId());
            rule_logger.info("原主题：" + ((MsgContext) contextBeanArray[0]).getTopic());
            rule_logger.info("原消息：\n" + ((MsgContext) contextBeanArray[0]).getPayload());
            String el;
            String eChainName = "dataChain_" + script.getScriptId();
            String requestId = "dataChain/" + script.getScriptId();
            if (script.getScriptAction() == 3) {
                el = "THEN(" + script.getScriptId() + ",httpBridge" + ")";
            } else if (script.getScriptAction() == 4) {
                el = "THEN(" + script.getScriptId() + ",mqttBridge" + ")";
            } else {
                el = "THEN(" + script.getScriptId() + ")";
            }
            LiteFlowChainELBuilder.createChain().setChainName(eChainName).setEL(el).build();
            // 执行规则脚本
            response = flowExecutor.execute2RespWithRid(eChainName, null, requestId, contextBeanArray);
            if (!response.isSuccess()) {
                rule_logger.info("规则脚本执行发生错误：" + response.getMessage());
            }
            rule_logger.info("脚本执行后主题：" + ((MsgContext) contextBeanArray[0]).getTopic());
            rule_logger.info("脚本执行后消息：\n" + ((MsgContext) contextBeanArray[0]).getPayload());
            rule_logger.info("执行规则引擎脚本结束：" + script.getScriptId());
            MDC.clear();  // 清除日志配置中的参数
        }
        return response.getContextBean(MsgContext.class);
    }
}
