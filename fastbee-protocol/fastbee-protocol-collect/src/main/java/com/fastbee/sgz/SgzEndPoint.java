package com.fastbee.sgz;

import cn.hutool.json.JSONObject;
import com.fastbee.base.core.annotation.Node;
import com.fastbee.base.core.annotation.PakMapping;
import com.fastbee.base.session.Session;
import com.fastbee.common.core.mq.DeviceReport;
import com.fastbee.common.core.thingsModel.ThingsModelSimpleItem;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author gsb
 * @date 2024/3/29 16:06
 */
@Node
@Component
public class SgzEndPoint {

    @PakMapping(types = 150)
    public DeviceReport heart(DeviceReport report, Session session){
        JSONObject params = new JSONObject();
        params.putIfAbsent("device",report.getClientId());
        List<ThingsModelSimpleItem> items = report.getThingsModelSimpleItem();
        for (ThingsModelSimpleItem item : items) {
            if (item.getId().equals("dtype")){
                params.putIfAbsent("dtype",item.getValue());
            }
        }
        params.putIfAbsent("fuc",SgzMessageType.LINKING.type);
        params.putIfAbsent("sdata",SgzMessageType.LINKING.type);
        report.setBody(params);
        return report;
    }

    @PakMapping(types = 154)
    public DeviceReport dataCallBack(DeviceReport report){
        JSONObject params = new JSONObject();
        params.putIfAbsent("device",report.getClientId());
        List<ThingsModelSimpleItem> items = report.getThingsModelSimpleItem();
        for (ThingsModelSimpleItem item : items) {
            if (item.getId().equals("dtype")){
                params.putIfAbsent("dtype",item.getValue());
            }
        }
        params.putIfAbsent("fuc",SgzMessageType.CZROK.type);
        params.putIfAbsent("sdata",SgzMessageType.CZROK.type);
        report.setBody(params);
        return report;
    }

}
