package com.fastbee.iot.data.job;

import com.fastbee.iot.domain.DeviceJob;
import org.quartz.JobExecutionContext;

/**
 * 定时任务处理（允许并发执行）
 *
 * @author ruoyi
 *
 */
public class DeviceJobExecution extends DeviceAbstractQuartzJob
{
    @Override
    protected void doExecute(JobExecutionContext context, DeviceJob deviceJob) throws Exception
    {
        DeviceJobInvoke.invokeMethod(deviceJob);
    }
}
