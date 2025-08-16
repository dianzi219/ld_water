package com.fastbee.iot.service.impl;

import java.util.Arrays;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fastbee.common.utils.DateUtils;
import com.dtflys.forest.http.ForestRequest;
import com.fastbee.http.service.HttpClientFactory;
import com.fastbee.http.service.SuccessCondition;
import com.fastbee.iot.domain.HttpClient;
import com.fastbee.iot.domain.MqttClient;
import com.fastbee.iot.domain.MultipleDataSource;
import com.fastbee.iot.ruleEngine.DataSourceService;
import com.fastbee.iot.ruleEngine.MqttClientFactory;
import com.fastbee.iot.service.IHttpClientService;
import com.fastbee.iot.service.IMqttClientService;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Objects;
import com.fastbee.common.utils.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import com.fastbee.iot.mapper.BridgeMapper;
import com.fastbee.iot.domain.Bridge;
import com.fastbee.iot.service.IBridgeService;

import javax.annotation.PostConstruct;

/**
 * 数据桥接Service业务层处理
 *
 * @author kerwincui
 * @date 2024-08-20
 */
@Service
public class BridgeServiceImpl extends ServiceImpl<BridgeMapper,Bridge> implements IBridgeService {

    @Autowired
    private IHttpClientService httpClientService;

    @Autowired
    private IMqttClientService mqttClientService;

    @Autowired
    private DataSourceService dataSourceService;

    @PostConstruct
    public void initBridge() {
        List<Bridge> list = this.selectBridgeList(new Bridge());
        for (Bridge bridge : list) {
            if (bridge.getType() == 4) {
                MqttClient config = JSON.parseObject(bridge.getConfigJson(), MqttClient.class);
                if (config != null) {
                    try {
                        MqttAsyncClient client = MqttClientFactory.instance(mqttClientService.buildmqttclientconfig(config));
                        if (client != null && bridge.getDirection() == 1 && !Objects.equals(bridge.getRoute(), "")) {
                            MqttClientFactory.addSubscribe(client, bridge.getRoute());
                        }
                    } catch (MqttException e) {
                        throw new RuntimeException(e);
                    }

                }
            }
        }
    }
    /**
     * 查询数据桥接
     *
     * @param id 主键
     * @return 数据桥接
     */
    @Override
    @Cacheable(cacheNames = "Bridge", key = "#id")
    // 查询时更新key缓存，更新和删除时删除缓存，新增时不更新，下一次查询会更新缓存
    public Bridge queryByIdWithCache(Long id){
        return this.getById(id);
    }

    /**
     * 查询数据桥接
     *
     * @param id 主键
     * @return 数据桥接
     */
    @Override
    @Cacheable(cacheNames = "Bridge", key = "#id")
    // 查询时更新key缓存，更新和删除时删除缓存，新增时不更新，下一次查询会更新缓存
    public Bridge selectBridgeById(Long id){
        return this.getById(id);
    }

    /**
     * 查询数据桥接列表
     *
     * @param bridge 数据桥接
     * @return 数据桥接
     */
    @Override
    public List<Bridge> selectBridgeList(Bridge bridge) {
        LambdaQueryWrapper<Bridge> lqw = buildQueryWrapper(bridge);
        lqw.orderByDesc(Bridge::getCreateTime);
        return baseMapper.selectList(lqw);
    }

    private LambdaQueryWrapper<Bridge> buildQueryWrapper(Bridge query) {
        Map<String, Object> params = query.getParams();
        LambdaQueryWrapper<Bridge> lqw = Wrappers.lambdaQuery();
                    lqw.eq(StringUtils.isNotBlank(query.getConfigJson()), Bridge::getConfigJson, query.getConfigJson());
                    lqw.like(StringUtils.isNotBlank(query.getName()), Bridge::getName, query.getName());
                    lqw.eq(StringUtils.isNotBlank(query.getEnable()), Bridge::getEnable, query.getEnable());
                    lqw.eq(StringUtils.isNotBlank(query.getStatus()), Bridge::getStatus, query.getStatus());
                    lqw.eq(query.getType() != null, Bridge::getType, query.getType());
                    lqw.eq(query.getDirection() != null, Bridge::getDirection, query.getDirection());
                    lqw.eq(StringUtils.isNotBlank(query.getRoute()), Bridge::getRoute, query.getRoute());

        if (!Objects.isNull(params.get("beginTime")) &&
        !Objects.isNull(params.get("endTime"))) {
            lqw.between(Bridge::getCreateTime, params.get("beginTime"), params.get("endTime"));
        }
        return lqw;
    }

    /**
     * 新增数据桥接
     *
     * @param add 数据桥接
     * @return 是否新增成功
     */
    @Override
    public Boolean insertWithCache(Bridge add) {
        validEntityBeforeSave(add);
        if (add.getType() == 4) {
            MqttClient config = JSON.parseObject(add.getConfigJson(), MqttClient.class);
            if (config != null) {
                try {
                    MqttAsyncClient client = MqttClientFactory.instance(mqttClientService.buildmqttclientconfig(config));
                    if (client != null && add.getConfigJson() != null && add.getDirection() == 1) {
                        add.setStatus("1");
                        MqttClientFactory.addSubscribe(client, add.getRoute());
                    }
                } catch (MqttException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return this.save(add);
    }

    /**
     * 修改数据桥接
     *
     * @param update 数据桥接
     * @return 是否修改成功
     */
    @Override
    @CacheEvict(cacheNames = "Bridge", key = "#update.id")
    public Boolean updateWithCache(Bridge update) {
        validEntityBeforeSave(update);
        if (update.getType() == 4) {
            if (update.getConfigJson() != null && update.getDirection() == 1) {
                MqttClient config = JSON.parseObject(update.getConfigJson(), MqttClient.class);
                update.setStatus("1");
                MqttClientFactory.addSubscribe(config.getHostUrl() + config.getClientId(), update.getRoute());
            }
        }
        return this.updateById(update);
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(Bridge entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除数据桥接信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    @CacheEvict(cacheNames = "Bridge", keyGenerator = "deleteKeyGenerator" )
    public Boolean deleteWithCacheByIds(Long[] ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return this.removeByIds(Arrays.asList(ids));
    }

    @Override
    public int connect(Bridge bridge) {
        AtomicInteger result = new AtomicInteger();
        if (bridge.getType() == 3) {
            HttpClient config = JSON.parseObject(bridge.getConfigJson(), HttpClient.class);
            ForestRequest request = HttpClientFactory.instance(httpClientService.buildhttpclientconfig(config));
            request.successWhen(SuccessCondition.class)
                    .onSuccess(((data, req, res) -> {
                        bridge.setStatus("1");
                        result.set(1);
                    }))
                    .onError((ex, req, res) -> {
                        bridge.setStatus("0");
                        result.set(0);
                    }).execute();
        } else if (bridge.getType() == 4) {
            MqttClient config = JSON.parseObject(bridge.getConfigJson(), MqttClient.class);
            if (config != null) {
                try {
                    MqttAsyncClient client = MqttClientFactory.instance(mqttClientService.buildmqttclientconfig(config));
                    if (client != null && client.isConnected()) {
                        bridge.setStatus("1");
                        result.set(1);
                    } else {
                        bridge.setStatus("0");
                        result.set(0);
                    }
                    if (client != null) {
                        client.disconnect().waitForCompletion();
                    }
                } catch (MqttException e) {
                    log.error("=>mqtt客户端创建错误");
                }
            }
        } else if (bridge.getType() == 5) {
            MultipleDataSource config = JSON.parseObject(bridge.getConfigJson(), MultipleDataSource.class);
            DruidDataSource dataSource = dataSourceService.createDataSource(config);
            try {
                if (dataSource != null && dataSource.getConnection() != null) {
                    bridge.setStatus("1");
                    result.set(1);
                } else {
                    bridge.setStatus("0");
                    result.set(0);
                }
            } catch (SQLException e) {
                log.error("=>数据源连接错误");
            }
        }
        this.updateWithCache(bridge);
        return result.get();
    }


}
