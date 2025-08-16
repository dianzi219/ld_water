package com.fastbee.sip.client;

import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.http.ForestRequest;

public interface ZlmRecordForestClient {

    @Get("/zlm/record/file/duration")
    ForestRequest<?> fileDuration();

    @Get("/zlm/record/info")
    ForestRequest<?> getInfo();

    @Get("/zlm/record/list")
    ForestRequest<?> getRecordlist();

    @Get("/zlm/record/date/list")
    ForestRequest<?> getRecordDatelist();

    @Get("/zlm/record/stream/list")
    ForestRequest<?> getRecordStreamlist();

    @Get("/zlm/record/app/list")
    ForestRequest<?> getRecordApplist();

    @Get("/zlm/record/file/list")
    ForestRequest<?> getRecordFilelist();

    @Get("/zlm/record/addStreamCallInfo")
    ForestRequest<?> addStreamCallInfo();

    @Get("/file/upload")
    ForestRequest<?> uploadOss();
}
