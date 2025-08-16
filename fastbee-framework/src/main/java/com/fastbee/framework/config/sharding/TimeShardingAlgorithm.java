package com.fastbee.framework.config.sharding;

import com.fastbee.framework.config.sharding.enums.ShardingTableCacheEnum;
import com.google.common.collect.Range;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

/**
 * <p> @Title TimeShardingAlgorithm
 * <p> @Description 分片算法，按月分片
 *
 */
@Slf4j
public class TimeShardingAlgorithm implements StandardShardingAlgorithm<Date> {
    /**
     * Date类型的分片时间格式
     */
    private static final SimpleDateFormat TABLE_SHARD_Date_FORMATTER = new SimpleDateFormat("yyyyMM");

    /**
     * 分片时间格式
     */
    private static final DateTimeFormatter TABLE_SHARD_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    /**
     * 完整时间格式
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");
    /**
     * 完整时间格式
     */
    private static final SimpleDateFormat DATE_TIME_FORMATTER_SPILE = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 表分片符号，例：t_user_202201 中，分片符号为 "_"
     */
    private final String TABLE_SPLIT_SYMBOL = "_";


    /**
     * 精准分片
     * @param tableNames 对应分片库中所有分片表的集合
     * @param preciseShardingValue 分片键值，其中 logicTableName 为逻辑表，columnName 分片键，value 为从 SQL 中解析出来的分片键的值
     * @return 表名
     */
    @Override
    public String doSharding(Collection<String> tableNames, PreciseShardingValue<Date> preciseShardingValue) {
        String logicTableName = preciseShardingValue.getLogicTableName();
        ShardingTableCacheEnum logicTable = ShardingTableCacheEnum.of(logicTableName);
        createAllTable(logicTable, tableNames);

        /// 打印分片信息
        log.info(">>>>>>>>>> 【INFO】精确分片，节点配置表名：{}，数据库缓存表名：{}", tableNames, logicTable.resultTableNamesCache());

        Date date = preciseShardingValue.getValue();
        Instant instant = date.toInstant();
        LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        String resultTableName = logicTableName + "_" + TABLE_SHARD_TIME_FORMATTER.format(localDateTime);
        // 检查分表获取的表名是否存在，不存在则自动建表
        if (!tableNames.contains(resultTableName)){
            tableNames.add(resultTableName);
        }
        return ShardingAlgorithmTool.getShardingTableAndCreate(logicTable, resultTableName);
    }

    /**
     * 范围分片
     * @param tableNames 对应分片库中所有分片表的集合
     * @param rangeShardingValue 分片范围
     * @return 表名集合
     */
    @Override
    public Collection<String> doSharding(Collection<String> tableNames, RangeShardingValue<Date> rangeShardingValue) {
        log.info("开始分表查询开始:{}",System.currentTimeMillis());
        String logicTableName = rangeShardingValue.getLogicTableName();
        ShardingTableCacheEnum logicTable = ShardingTableCacheEnum.of(logicTableName);
        createAllTable(logicTable, tableNames);

        /// 打印分片信息
        log.info(">>>>>>>>>> 【INFO】范围分片，节点配置表名：{}，数据库缓存表名：{}", tableNames, logicTable.resultTableNamesCache());

        // between and 的起始值
        Range<Date> valueRange = rangeShardingValue.getValueRange();
        boolean hasLowerBound = valueRange.hasLowerBound();
        boolean hasUpperBound = valueRange.hasUpperBound();

        // 获取最大值和最小值
        Set<String> tableNameCache = logicTable.resultTableNamesCache();
        String min = hasLowerBound ? String.valueOf(valueRange.lowerEndpoint()) : getLowerEndpoint(tableNameCache);
        String max = hasUpperBound ? String.valueOf(valueRange.upperEndpoint()) : getUpperEndpoint(tableNameCache);
        // 循环计算分表范围
        Set<String> resultTableNames = new LinkedHashSet<>();
        try {
            Date minDate = DATE_TIME_FORMATTER_SPILE.parse(min);
            Date maxDate = DATE_TIME_FORMATTER_SPILE.parse(max);
            Calendar calendar = Calendar.getInstance();
            while (minDate.before(maxDate) || minDate.equals(maxDate)) {
                String tableName = logicTableName + TABLE_SPLIT_SYMBOL + TABLE_SHARD_Date_FORMATTER.format(minDate);
                resultTableNames.add(tableName);
                calendar.setTime(minDate); // 设置Calendar的时间为Date对象的时间
                calendar.add(Calendar.DAY_OF_MONTH, 1); // 给日期加一天
                minDate = calendar.getTime();
            }
            log.info("开始分表查询结束:{}",System.currentTimeMillis());
            return ShardingAlgorithmTool.getShardingTablesAndCreate(logicTable, resultTableNames);
        } catch (Exception e) {
            return ShardingAlgorithmTool.getShardingTablesAndCreate(logicTable, logicTable.resultTableNamesCache());
        }
    }


    @Override
    public void init() {

    }

    @Override
    public String getType() {
        return null;
    }

    // --------------------------------------------------------------------------------------------------------------
    // 私有方法
    // --------------------------------------------------------------------------------------------------------------

    /**
     * 获取 最小分片值
     * @param tableNames 表名集合
     * @return 最小分片值
     */
    private String getLowerEndpoint(Collection<String> tableNames) {
        Optional<LocalDateTime> optional = tableNames.stream()
                .map(o -> LocalDateTime.parse(o.replace(TABLE_SPLIT_SYMBOL, "") + "01 00:00:00", DATE_TIME_FORMATTER))
                .min(Comparator.comparing(Function.identity()));
        if (optional.isPresent()) {
            ZonedDateTime zonedDateTime = optional.get().atZone(ZoneId.systemDefault());
            Instant instant = zonedDateTime.toInstant();
            return String.valueOf(Date.from(instant));
        } else {
            log.error(">>>>>>>>>> 【ERROR】获取数据最小分表失败，请稍后重试，tableName：{}", tableNames);
            throw new IllegalArgumentException("获取数据最小分表失败，请稍后重试");
        }
    }

    /**
     * 获取 最大分片值
     * @param tableNames 表名集合
     * @return 最大分片值
     */
    private String getUpperEndpoint(Collection<String> tableNames) {
        Optional<LocalDateTime> optional = tableNames.stream()
                .map(o -> LocalDateTime.parse(o.replace(TABLE_SPLIT_SYMBOL, "") + "01 00:00:00", DATE_TIME_FORMATTER))
                .max(Comparator.comparing(Function.identity()));
        if (optional.isPresent()) {
            ZonedDateTime zonedDateTime = optional.get().atZone(ZoneId.systemDefault());
            Instant instant = zonedDateTime.toInstant();
            return String.valueOf(Date.from(instant));
        } else {
            log.error(">>>>>>>>>> 【ERROR】获取数据最大分表失败，请稍后重试，tableName：{}", tableNames);
            throw new IllegalArgumentException("获取数据最大分表失败，请稍后重试");
        }
    }

    /**
     * 根据分片规则获取的表，创建所有的表
     * @param logicTable
     * @param tableNames
     */
    private void createAllTable(ShardingTableCacheEnum logicTable, Collection<String> tableNames) {
        if (!CollectionUtils.isEmpty(logicTable.resultTableNamesCache())) {
            //如果缓存中有表了，则证明已经创建了表，无需再创建
            return;
        }
        //根据分片规则创建表
        ShardingAlgorithmTool.getShardingTablesAndCreate(logicTable,tableNames);
        //刷新缓存
        ShardingAlgorithmTool.tableNameCacheReload(logicTable);
    }
}
