package com.fastbee.record.controller;

import com.fastbee.record.controller.bean.ControllerException;
import com.fastbee.record.controller.bean.ErrorCode;
import com.fastbee.record.controller.bean.RecordFile;
import com.fastbee.record.controller.bean.Result;
import com.fastbee.record.dto.*;
import com.fastbee.record.service.VideoFileService;
import com.fastbee.record.utils.Constants;
import com.fastbee.record.utils.PageInfo;
import com.fastbee.record.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/zlm/record")
public class RecordController {

    @Autowired
    private VideoFileService videoFileService;

    @Autowired
    private UserSettings userSettings;

    @Autowired
    private RedisUtil redisUtil;

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取Assist服务配置信息
     */

    @GetMapping(value = "/info")
    @ResponseBody
    public UserSettings getInfo(){
        return userSettings;
    }

    /**
     * 获取app+stream列表
     */
    @GetMapping(value = "/list")
    @ResponseBody
    public PageInfo<RecordInfo> getList(@RequestParam int pageNum,
                                                 @RequestParam int pageSize){
        List<RecordInfo> appList = videoFileService.getList();

        PageInfo<RecordInfo> stringPageInfo = new PageInfo<>(appList);
        stringPageInfo.startPage(pageNum, pageSize);
        return stringPageInfo;
    }

    /**
     * 分页获取app列表
     */
    @GetMapping(value = "/app/list")
    @ResponseBody
    public PageInfo<String> getAppList(@RequestParam int page,
                                                  @RequestParam int count){
        List<String> resultData = new ArrayList<>();
        List<File> appList = videoFileService.getAppList(true);
        if (appList.size() > 0) {
            for (File file : appList) {
                resultData.add(file.getName());
            }
        }
        Collections.sort(resultData);

        PageInfo<String> stringPageInfo = new PageInfo<>(resultData);
        stringPageInfo.startPage(page, count);
        return stringPageInfo;
    }

    /**
     * 分页stream列表
     */
    @GetMapping(value = "/stream/list")
    @ResponseBody
    public PageInfo<String> getStreamList(@RequestParam int page,
                                                     @RequestParam int count,
                                                     @RequestParam String app ){
        List<String> resultData = new ArrayList<>();
        if (app == null) {
            throw new ControllerException(ErrorCode.ERROR400.getCode(), "app不能为空");
        }
        List<File> streamList = videoFileService.getStreamList(app, true);
        if (streamList != null) {
            for (File file : streamList) {
                resultData.add(file.getName());
            }
        }
        PageInfo<String> stringPageInfo = new PageInfo<>(resultData);
        stringPageInfo.startPage(page, count);
        return stringPageInfo;
    }

    /**
     * 获取日期文件夹列表
     */
    @GetMapping(value = "/date/list")
    @ResponseBody
    public List<String> getDateList( @RequestParam(required = false) Integer year,
                                                @RequestParam(required = false) Integer month,
                                                 @RequestParam String app,
                                                 @RequestParam String stream ){
        List<String> resultData = new ArrayList<>();
        if (app == null) {
            throw new ControllerException(ErrorCode.ERROR400.getCode(), "app不能为空");
        };
        if (stream == null) {
            throw new ControllerException(ErrorCode.ERROR400.getCode(), "stream不能为空");
        }
        List<File> dateList = videoFileService.getDateList(app, stream, year, month, true);
        for (File file : dateList) {
            resultData.add(file.getName());
        }
        return resultData;
    }

    /**
     * 获取视频文件列表
     */
    @GetMapping(value = "/file/list")
    @ResponseBody
    public PageInfo<String> getRecordList(@RequestParam int page,
                                                     @RequestParam int count,
                                                     @RequestParam String app,
                                                     @RequestParam String stream,
                                                     @RequestParam(required = false) String startTime,
                                                     @RequestParam(required = false) String endTime
    ){
        // 开始时间与结束时间可不传或只传其一
        List<String> recordList = new ArrayList<>();
        try {
            Date startTimeDate  = null;
            Date endTimeDate  = null;
            if (startTime != null ) {
                startTimeDate = formatter.parse(startTime);
            }
            if (endTime != null ) {
                endTimeDate = formatter.parse(endTime);
            }

            List<File> filesInTime = videoFileService.getFilesInTime(app, stream, startTimeDate, endTimeDate);
            if (filesInTime != null && filesInTime.size() > 0) {
                for (File file : filesInTime) {
                    recordList.add(file.getName());
                }
            }
            PageInfo<String> stringPageInfo = new PageInfo<>(recordList);
            stringPageInfo.startPage(page, count);
            return stringPageInfo;
        } catch (ParseException e) {
            log.error("错误的开始时间[{}]或结束时间[{}]", startTime, endTime);
            throw new ControllerException(ErrorCode.ERROR400.getCode(), "错误的开始时间或结束时间, e=" + e.getMessage());
        }
    }

    /**
     * 获取视频文件列表
     * @return
     */
    @GetMapping(value = "/file/listWithDate")
    @ResponseBody
    public PageInfo<RecordFile> getRecordListWithDate(@RequestParam int page,
                                                      @RequestParam int count,
                                                      @RequestParam String app,
                                                      @RequestParam String stream,
                                                      @RequestParam(required = false) String startTime,
                                                      @RequestParam(required = false) String endTime
    ){

        // 开始时间与结束时间可不传或只传其一
        List<RecordFile> recordList = new ArrayList<>();
        try {
            Date startTimeDate  = null;
            Date endTimeDate  = null;
            if (startTime != null ) {
                startTimeDate = formatter.parse(startTime);
            }
            if (endTime != null ) {
                endTimeDate = formatter.parse(endTime);
            }

            List<File> filesInTime = videoFileService.getFilesInTime(app, stream, startTimeDate, endTimeDate);
            if (filesInTime != null && filesInTime.size() > 0) {
                for (File file : filesInTime) {
                    recordList.add(RecordFile.instance(app, stream, file.getName(), file.getParentFile().getName()));
                }
            }
            PageInfo<RecordFile> stringPageInfo = new PageInfo<>(recordList);
            stringPageInfo.startPage(page, count);
            return stringPageInfo;
        } catch (ParseException e) {
            log.error("错误的开始时间[{}]或结束时间[{}]", startTime, endTime);
            throw new ControllerException(ErrorCode.ERROR400.getCode(), "错误的开始时间或结束时间, e=" + e.getMessage());
        }
    }


    /**
     * 添加视频裁剪合并任务
     */
    @GetMapping(value = "/file/download/task/add")
    @ResponseBody
    public String addTaskForDownload(@RequestParam String app,
                                                @RequestParam String stream,
                                                @RequestParam(required = false) String startTime,
                                                @RequestParam(required = false) String endTime,
                                                @RequestParam(required = false) String remoteHost
    ){
        Date startTimeDate  = null;
        Date endTimeDate  = null;
        try {
            if (startTime != null ) {
                startTimeDate = formatter.parse(startTime);
            }
            if (endTime != null ) {
                endTimeDate = formatter.parse(endTime);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String id = videoFileService.mergeOrCut(app, stream, startTimeDate, endTimeDate, remoteHost);
        if (id== null) {
            throw new ControllerException(ErrorCode.ERROR100.getCode(), "可能未找到视频文件");
        }
        return id;
    }

    /**
     * 查询视频裁剪合并任务列表
     */
    @GetMapping(value = "/file/download/task/list")
    @ResponseBody
    public List<MergeOrCutTaskInfo> getTaskListForDownload(
            @RequestParam(required = false) String app,
            @RequestParam(required = false) String stream,
            @RequestParam(required = false) String taskId,
            @RequestParam(required = false) Boolean isEnd){
        List<MergeOrCutTaskInfo> taskList = videoFileService.getTaskListForDownload(isEnd, app, stream, taskId);
        if (taskList == null) {
            throw new ControllerException(ErrorCode.ERROR100);
        }
        return taskList;
    }

    /**
     * 收藏录像（被收藏的录像不会被清理任务清理）
     */

    @GetMapping(value = "/file/collection/add")
    @ResponseBody
    public void collection(
            @RequestParam(required = true) String type,
            @RequestParam(required = true) String app,
            @RequestParam(required = true) String stream){

        boolean collectionResult = videoFileService.collection(app, stream, type);
        if (!collectionResult) {
            throw new ControllerException(ErrorCode.ERROR100);
        }
    }

    /**
     * 移除收藏录像
     */
    @GetMapping(value = "/file/collection/remove")
    @ResponseBody
    public void removeCollection(
            @RequestParam(required = true) String type,
            @RequestParam(required = true) String app,
            @RequestParam(required = true) String stream){

        boolean collectionResult = videoFileService.removeCollection(app, stream, type);
        if (!collectionResult) {
            throw new ControllerException(ErrorCode.ERROR100);
        }
    }

    /**
     * 收藏录像列表
     */
    @GetMapping(value = "/file/collection/list")
    @ResponseBody
    public List<SignInfo> collectionList(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String app,
            @RequestParam(required = false) String stream){

        List<SignInfo> signInfos = videoFileService.getCollectionList(app, stream, type);
        return signInfos;
    }

    /**
     * 中止视频裁剪合并任务列表
     */
    @GetMapping(value = "/file/download/task/stop")
    @ResponseBody
    public Result<String> stopTaskForDownload(@RequestParam String taskId){
        return null;
    }


    /**
     * 磁盘空间查询
     */
    @ResponseBody
    @GetMapping(value = "/space", produces = "application/json;charset=UTF-8")
    public SpaceInfo getSpace() {
        return videoFileService.getSpaceInfo();
    }

    /**
     * 增加推流的鉴权信息，用于录像存储使用
     */
    @ResponseBody
    @GetMapping(value = "/addStreamCallInfo", produces = "application/json;charset=UTF-8")
    @PostMapping(value = "/addStreamCallInfo", produces = "application/json;charset=UTF-8")
    public void addStreamCallInfo(String app, String stream, String callId) {
        String key = Constants.STREAM_CALL_INFO + userSettings.getId() + "_" + app + "_" + stream;
        redisUtil.set(key, callId, -1);
    }

    /**
     * 录像文件的时长
     */
    @ResponseBody
    @GetMapping(value = "/file/duration", produces = "application/json;charset=UTF-8")
    @PostMapping(value = "/file/duration", produces = "application/json;charset=UTF-8")
    public long fileDuration( @RequestParam String app, @RequestParam String stream) {
        return videoFileService.fileDuration(app, stream);
    }
}
