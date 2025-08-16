package com.fastbee.framework.config.sharding.enums;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import java.util.*;

import static com.fastbee.framework.config.sharding.ShardingAlgorithmTool.actualDataNodesRefresh;


/**
 * <p> @Title ShardingTableCacheEnum
 * <p> @Description 分片表缓存枚举
 *
 */
public enum ShardingTableCacheEnum {

    /**
     * 用户埋点表
     */
    DEVICE_LOG("iot_device_log", new HashSet<>());

    /**
     * 逻辑表名
     */
    private final String logicTableName;
    /**
     * 实际表名
     */
    private final Set<String> resultTableNamesCache;

    private static Map<String, ShardingTableCacheEnum> valueMap = new HashMap<>();

    static {
        Arrays.stream(ShardingTableCacheEnum.values()).forEach(o -> valueMap.put(o.logicTableName, o));
    }

    ShardingTableCacheEnum(String logicTableName, Set<String> resultTableNamesCache) {
        this.logicTableName = logicTableName;
        this.resultTableNamesCache = resultTableNamesCache;
    }

    public static ShardingTableCacheEnum of(String value) {
        return valueMap.get(value);
    }

    public String logicTableName() {
        return logicTableName;
    }

    public Set<String> resultTableNamesCache() {
        return resultTableNamesCache;
    }

    /**
     * 更新缓存、配置（原子操作）
     *
     * @param tableNameList
     */
    public void atomicUpdateCacheAndActualDataNodes(List<String> tableNameList) {
        if (CollectionUtils.isEmpty(tableNameList)) {
            return;
        }
        synchronized (resultTableNamesCache) {
            // 删除缓存
            resultTableNamesCache.clear();
            // 写入新的缓存
            resultTableNamesCache.addAll(tableNameList);
            // 动态更新配置 actualDataNodes
            actualDataNodesRefresh(logicTableName, tableNameList);
        }
    }

    public static Set<String> logicTableNames() {
        return valueMap.keySet();
    }

    @Override
    public String toString() {
        return "ShardingTableCacheEnum{" +
                "logicTableName='" + logicTableName + '\'' +
                ", resultTableNamesCache=" + resultTableNamesCache +
                '}';
    }
}
