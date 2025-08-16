-- ----------------------------
-- Table structure for oss_detail
-- ----------------------------
DROP TABLE IF EXISTS `oss_detail`;
CREATE TABLE `oss_detail`
(
    `id`                int(10) UNSIGNED                                        NOT NULL AUTO_INCREMENT COMMENT '文件id',
    `tenant_id`         bigint(20) NOT NULL DEFAULT 1 COMMENT '租户ID',
    `tenant_name`       varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '租户名称',
    `file_name`         varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '文件名',
    `original_name`     varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '原名',
    `file_suffix`       varchar(10)  CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '文件后缀名',
    `url`               varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT 'URL地址',
    `service`           varchar(20)  CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT 'aliyun'   COMMENT '服务商',
    `del_flag` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
    `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` datetime(0) NOT NULL COMMENT '创建时间',
    `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '更新者',
    `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
    `remark` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '文件记录表'
  ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `oss_config`;
CREATE TABLE `oss_config`
(
    `id`              int(10)       UNSIGNED                                   NOT NULL AUTO_INCREMENT COMMENT 'id',
    `tenant_id`       bigint(20) NOT NULL DEFAULT 1 COMMENT '租户ID',
    `tenant_name`     varchar(30)   CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '租户名称',
    `config_key`      varchar(20)   CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '配置key',
    `access_key`      varchar(255)  CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT 'accessKey',
    `secret_key`      varchar(255)  CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '秘钥',
    `bucket_name`     varchar(255)  CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '桶名称',
    `prefix`          varchar(255)  CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT'前缀',
    `endpoint`        varchar(255)  CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '访问站点',
    `domain`          varchar(255)  CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '自定义域名',
    `is_https`        char(1)       CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT 'N' COMMENT '是否https（Y=是,N=否）',
    `region`          varchar(255)  CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '域',
    `access_policy`   char(1)       CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '1' COMMENT '桶权限类型(0=private 1=public 2=custom)',
    `status`          char(1)       CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '1' COMMENT '是否默认（0=是,1=否）',
    `ext1`            varchar(255)  CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '扩展字段',
    `del_flag`        char(1)       CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
    `create_by`       varchar(64)   CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time`     datetime(0)                                              NOT NULL COMMENT '创建时间',
    `update_by`       varchar(64)   CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT '' COMMENT '更新者',
    `update_time`     datetime(0)                                              NULL     DEFAULT NULL COMMENT '更新时间',
    `remark`          varchar(500)  CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '对象存储配置表'
  ROW_FORMAT = Dynamic;


-- ----------------------------
-- Records of oss_config
-- ----------------------------
insert into oss_config values (1, 1, 'admin', 'aliyun', 'XXXXXXXXXXXXXXX',  'XXXXXXXXXXXXXXX', 'fastbee',             '', 'oss-cn-beijing.aliyuncs.com',   '','N', '',             '0' ,'0', '', '0', '', '2023-02-25 23:15:57', '', NULL, NULL);
insert into oss_config values (2, 1, 'admin', 'qiniu',  'XXXXXXXXXXXXXXX',  'XXXXXXXXXXXXXXX', 'fastbee',             '', 's3-cn-north-1.qiniucs.com',     '','N', '',             '1' ,'1', '', '0', '', '2023-02-25 23:15:57', '', NULL, NULL);
