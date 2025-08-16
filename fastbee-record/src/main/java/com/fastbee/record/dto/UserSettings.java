package com.fastbee.record.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
@Setter
@Getter
public class UserSettings {

    @Value("${userSettings.id}")
    private String id;

    @Value("${userSettings.record}")
    private String record;

    @Value("${userSettings.recordDay:7}")
    private int recordDay;

    @Value("${userSettings.recordTempDay:-1}")
    private int recordTempDay;

    @Value("${userSettings.ffmpeg}")
    private String ffmpeg;

    @Value("${userSettings.ffprobe}")
    private String ffprobe;

    @Value("${userSettings.threads:2}")
    private int threads;
}
