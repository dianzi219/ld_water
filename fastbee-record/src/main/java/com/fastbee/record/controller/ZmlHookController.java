package com.fastbee.record.controller;

import com.alibaba.fastjson.JSONObject;
import com.fastbee.record.dto.UserSettings;
import com.fastbee.record.service.VideoFileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/zlmhook")
public class ZmlHookController {

    @Autowired
    private UserSettings userSettings;
    @Autowired
    private VideoFileService videoFileService;

    /**
     * 录制完成的通知, 对用zlm的hook
     *
     * @return
     */
    @ResponseBody
    @PostMapping(value = "/on_record_mp4", produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> onRecordMp4(@RequestBody JSONObject json) {
        JSONObject ret = new JSONObject();
        ret.put("code", 0);
        ret.put("msg", "success");
        String file_path = json.getString("file_path");

        String app = json.getString("app");
        String stream = json.getString("stream");
        log.info("ZLM 录制完成，文件路径：" + file_path);

        if (file_path == null) {
            return new ResponseEntity<String>(ret.toString(), HttpStatus.OK);
        }
        if (userSettings.getRecordDay() <= 0) {
            log.info("录像保存事件为{}天，直接删除: {}", userSettings.getRecordDay(), file_path);
            FileUtils.deleteQuietly(new File(file_path));
        } else {
            videoFileService.handFile(new File(file_path), app, stream);
        }

        return new ResponseEntity<String>(ret.toString(), HttpStatus.OK);
    }
}
