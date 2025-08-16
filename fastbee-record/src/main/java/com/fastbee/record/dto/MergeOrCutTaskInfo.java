package com.fastbee.record.dto;

import lombok.Data;

@Data
public class MergeOrCutTaskInfo {
    private String id;
    private String app;
    private String stream;
    private String startTime;
    private String endTime;
    private String createTime;
    private String percentage;
    private String recordFile;
    private String downloadFile;
    private String playFile;
}
