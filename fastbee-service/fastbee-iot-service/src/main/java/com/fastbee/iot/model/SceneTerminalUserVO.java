package com.fastbee.iot.model;

import lombok.Data;

/**
 * @author fastb
 * @version 1.0
 * @description: 场景终端用户
 * @date 2024-04-25 10:25
 */
@Data
public class SceneTerminalUserVO {

    private Long sceneId;

    private Long userId;

    private Integer terminalUser;
}
