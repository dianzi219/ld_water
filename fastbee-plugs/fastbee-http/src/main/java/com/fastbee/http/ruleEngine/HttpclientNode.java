package com.fastbee.http.ruleEngine;

import com.fastbee.http.client.Amap;
import com.fastbee.http.model.Location;
import com.fastbee.http.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Slf4j
@Component
public class HttpclientNode {
    @Resource
    private Amap amap;

    public Result<Location> amapLocation(BigDecimal longitude, BigDecimal latitude) {
        Result<Location> result = amap.getLocation(longitude.toEngineeringString(), latitude.toEngineeringString());
        return result;
    }
}
