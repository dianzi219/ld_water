package com.fastbee.record;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class })
@EnableScheduling
@ComponentScan(basePackages = {"com.fastbee.common.core.redis","com.fastbee.record"})
public class ZlmRecordApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZlmRecordApplication.class, args);
    }

}
