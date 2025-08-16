package com.fastbee.iot.service;

import java.util.List;
import com.fastbee.iot.domain.SceneScript;
import org.apache.ibatis.annotations.Param;

/**
 * 场景脚本Service接口
 *
 * @author kerwincui
 * @date 2023-12-27
 */
public interface ISceneScriptService
{
    /**
     * 查询场景脚本
     *
     * @param scriptId 场景脚本主键
     * @return 场景脚本
     */
    public SceneScript selectSceneScriptBySceneScriptId(String scriptId);

    /**
     * 查询场景脚本列表
     *
     * @param sceneScript 场景脚本
     * @return 场景脚本集合
     */
    public List<SceneScript> selectSceneScriptList(SceneScript sceneScript);

    /**
     * 新增场景脚本
     *
     * @param sceneScript 场景脚本
     * @return 结果
     */
    public int insertSceneScript(SceneScript sceneScript);

    /**
     * 修改场景脚本
     *
     * @param sceneScript 场景脚本
     * @return 结果
     */
    public int updateSceneScript(SceneScript sceneScript);

    /**
     * 批量删除场景脚本
     *
     * @param scriptIds 需要删除的场景脚本主键集合
     * @return 结果
     */
    public int deleteSceneScriptBySceneScriptIds(String[] scriptIds);

    /**
     * 批量删除场景联动脚本
     *
     * @param sceneIds 需要删除的数据场景ID集合
     * @return 结果
     */
    public int deleteSceneScriptBySceneIds(Long[] sceneIds);

    /**
     * 批量新增场景脚本
     *
     * @param sceneScriptList 场景联动触发器集合
     * @return 结果
     */
    public int insertSceneScriptList(List<SceneScript> sceneScriptList);

    /**
     * 通过脚本用途批量查询
     * @param sceneIdList 场景id
     * @param: scriptPurpose 脚本用途
     * @return List<SceneScript>
     */
    List<SceneScript> listSceneScriptByPurpose(List<Long> sceneIdList, Integer scriptPurpose);

    /**
     * 删除场景脚本信息
     *
     * @param scriptId 场景脚本主键
     * @return 结果
     */
    public int deleteSceneScriptBySceneScriptId(String scriptId);
}
