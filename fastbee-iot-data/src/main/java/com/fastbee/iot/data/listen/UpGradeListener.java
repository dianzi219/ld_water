package com.fastbee.iot.data.listen;


import com.fastbee.common.constant.FastBeeConstant;
import com.fastbee.common.core.domain.entity.SysMenu;
import com.fastbee.common.core.mq.ota.OtaUpgradeDelayTask;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.iot.data.service.IOtaUpgradeService;
import com.fastbee.mq.queue.DelayUpgradeQueue;
import com.fastbee.system.service.ISysMenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * OTA升级监听
 * @author bill
 */
@Slf4j
@Component
@Order(3)
public class UpGradeListener implements ApplicationRunner {

    @Autowired
    private IOtaUpgradeService otaUpgradeService;
    @Resource
    private ISysMenuService menuService;
    /**
     * true: 使用netty搭建的mqttBroker  false: 使用emq
     */
    @Value("${server.broker.enabled}")
    private Boolean enabled;


    @Async(FastBeeConstant.TASK.DELAY_UPGRADE_TASK)
    public void listen(){
        while (true){
            try {
                OtaUpgradeDelayTask task = DelayUpgradeQueue.task();
                if (StringUtils.isNotNull(task)){
                    Date startTime = task.getStartTime();
                    otaUpgradeService.upgrade(task);
                    log.info("=>开始OTA升级时间{}",startTime);
                }
                continue;
            }catch (Exception e){
                log.error("=>OTA升级监听异常",e);
            }
        }
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("=>OTA监听队列启动成功");
        // updateMenu();
        // listen();
    }

    /**
     * 切换菜单中Emqx控制台和netty管理
     */
    public void updateMenu(){
        SysMenu sysMenu = new SysMenu();
        if (enabled){
            sysMenu.setMenuId(2104L);
            sysMenu.setVisible("1");
            menuService.updateMenu(sysMenu);
            sysMenu.setMenuId(3031L);
        }else {
            sysMenu.setMenuId(3031L);
            sysMenu.setVisible("1");
            menuService.updateMenu(sysMenu);
            sysMenu.setMenuId(2104L);
        }
        sysMenu.setVisible("0");
        menuService.updateMenu(sysMenu);
    }
}
