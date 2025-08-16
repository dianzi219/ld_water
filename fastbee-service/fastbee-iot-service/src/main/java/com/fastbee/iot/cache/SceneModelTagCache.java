package com.fastbee.iot.cache;

import com.fastbee.iot.model.scenemodel.SceneModelTagCacheVO;

/**
 * @author gsb
 * @date 2024/3/22 16:37
 */
public interface SceneModelTagCache {

    /**
     * 缓存场景运算型变量值
     * @param sceneModelId 场景id
     * @param: value 值
     * @return void
     */
    void addSceneModelTagValue(Long sceneModelId, SceneModelTagCacheVO sceneModelTagCacheVO);

    /**
     * 获取场景运算型变量值
     * @param sceneModelTagId 场景变量id
     * @return java.lang.String
     */
    SceneModelTagCacheVO getSceneModelTagValue(Long sceneModelId, Long sceneModelTagId);
}
