package com.fastbee.slt427.model;

import com.fastbee.common.core.protocol.Message;
import com.fastbee.protocol.base.annotation.Column;
import com.fastbee.protocol.util.ToStringBuilder;
import lombok.NoArgsConstructor;


/**
 * @author goowy
 * @date 2025/8/14
 */
@NoArgsConstructor
public class FlowDev extends Message {

    @Column(length = 1,version = {0,1},desc = "起始地址")
    protected int start = 0x68;
    @Column(length = 1,version = {0,1},desc = "长度")
    protected int length;
    @Column(length = 1,version = {0,1} ,desc = "起始位")
    protected int start1 =0x68;
    @Column(length = 1,version = {0,1},desc = "控制域C")
    protected String control; //B3
    @Column(length = 5,version = {0,1},desc = "地址域A")
    protected String imei;
    @Column(length = 1,version = {0,1},desc = "功能码AFN")
    protected String code = "C1";
    @Column(length = 5,version = 0,desc = "瞬时流量")
    protected String instant;
    @Column(length = 5,version = 0,desc = "累积流量")
    protected String sum;
    @Column(length = 4,version = 0,desc = "报警位")
    protected int no;
    @Column(length = 6,version = 0,desc = "时间标签")
    protected String ts; // 共7个字节,需要拆分,前6个字节为时标,采用BCD码,表示启动帧发送的时间;后1个字节表示允许发送传输延时时长,指启动帧从开始发送至从动站接收到报文之间所允许的传输延时时长,采用BIN码,单位为min。
    @Column(length = 1,version = {1},desc = "无意义值")
    protected int oo = 0;
    @Column(length = 1,version = {0,1},desc = "CRC")
    protected int crc;
    @Column(length = 1,version = {0,1},desc = "结束地址")
    protected int end = 0x16;

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getStart1() {
        return start1;
    }

    public void setStart1(int start1) {
        this.start1 = start1;
    }

    public String getControl() {
        return control;
    }

    public void setControl(String control) {
        this.control = control;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getInstant() {
        return instant;
    }

    public void setInstant(String instant) {
        this.instant = instant;
    }

    public String getSum() {
        return sum;
    }

    public void setSum(String sum) {
        this.sum = sum;
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public int getCrc() {
        return crc;
    }

    public void setCrc(int crc) {
        this.crc = crc;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getOo() {
        return oo;
    }

    public void setOo(int oo) {
        this.oo = oo;
    }

    protected StringBuilder toStringHead() {
        final StringBuilder sb = new StringBuilder();
        sb.append(";[长度]:  ").append(length);
        sb.append(";[控制域C]:  ").append(control);
        sb.append(";[设备编号]:  ").append(imei);
        sb.append(";[功能码]:  ").append(code);
        sb.append(";[瞬时流量]:  ").append(instant);
        sb.append(";[累积流量]:  ").append(sum);
        sb.append(";[数据时间]:  ").append(ts);
        return sb;
    }

    @Override
    public String toString() {
        return ToStringBuilder.toString(toStringHead(), this, true);
    }
}
