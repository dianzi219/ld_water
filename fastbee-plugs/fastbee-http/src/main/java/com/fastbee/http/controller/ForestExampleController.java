package com.fastbee.http.controller;

import com.fastbee.http.client.Amap;
import com.fastbee.http.model.*;

import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RestController
public class ForestExampleController {

    @Resource
    private Amap amap;

    @GetMapping("/amap/location")
    public Result<Location> amapLocation(@RequestParam BigDecimal longitude, @RequestParam BigDecimal latitude) {
        Result<Location> result = amap.getLocation(longitude.toEngineeringString(), latitude.toEngineeringString());
        return result;
    }

    @GetMapping("/amap/location2")
    public Map amapLocation2(@RequestParam BigDecimal longitude, @RequestParam BigDecimal latitude) {
        Coordinate coordinate = new Coordinate(
                longitude.toEngineeringString(),
                latitude.toEngineeringString());
        Map result = amap.getLocation(coordinate);
        return result;
    }

    @GetMapping("/amap/location3")
    public Map amapLocation3(@RequestParam BigDecimal longitude, @RequestParam BigDecimal latitude) {
        Coordinate coordinate = new Coordinate(
                longitude.toEngineeringString(),
                latitude.toEngineeringString());
        Map result = amap.getLocationByCoordinate(coordinate);
        return result;
    }
}
