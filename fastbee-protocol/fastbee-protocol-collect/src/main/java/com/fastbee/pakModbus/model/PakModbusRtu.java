package com.fastbee.pakModbus.model;

import com.fastbee.common.core.protocol.Message;
import com.fastbee.common.core.protocol.modbus.ModbusCode;
import com.fastbee.protocol.base.annotation.Column;
import com.fastbee.protocol.util.ToStringBuilder;
import com.fastbee.base.session.Session;
import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;

import java.util.Arrays;

/**
 * modbus采集方式二：dtu或模组主动轮询，变化上报，以约定报文格式进行上报
 *
 * @author gsb
 * @date 2022/12/5 16:43
 */
@NoArgsConstructor
public class PakModbusRtu extends Message {

    /**
     * 1.约定报文格式如下
     * 1.设备主动上报数据组成：
     * * 上报的指令数据
     * * FFAA 0001 010302030578B7
     * * FFAA   0001         010302030578B7
     * * 包头   起始寄存器   数据包
     * <p>
     * * 数据包
     * * 01        03      02              0305  78B7
     * * 设备地址  命令号  返回数据字节数  数据  CRC校验
     * <p>
     * 2.服务下发数据组成
     * * 下发的指令数据
     * * FFDD a69035158678888448 01 06 0101 0015 1839
     * * FFDD       a69035158678888448   0106010100151839
     * * 固定报文头   9字节消息ID    数据包
     * </p>
     * * 数据包
     * * 01         06       0101         0015    1839
     * * 设备地址   命令号   寄存器地址   数据位  CRC校验
     * <p>
     * 3.设备应答服务下发数据组成
     * * 下发的指令数据
     * * FFDD a69035158678888448 01 06 0101 0015 1839
     * * FFDD         a69035158678888448   0106010100151839
     * * 固定报文头   9字节消息ID    数据包
     * <p>
     * * 数据包
     * * 01         06       0101         0015    1839
     * * 设备地址   命令号   寄存器地址   数据位  CRC校验
     */

    @Column(length = 1, version = {0x80, 0x81}, desc = "起始地址")
    protected int begin;
    @Column(length = 1, version = {0x80, 0x81}, desc = "标识位,实例设备是0x80")
    private int mId;
    @Column(length = 6, version = 0x80, desc = "MAC地址,6个字节")
    private String mac;
    @Column(length = 1, version = {0x80, 0x81}, desc = "结尾,如：7E")
    private int end;
    @Column(length = 2, desc = "固定报文头: FFDD或FFAA",version = {0,1,2,3,4,5})
    protected int start = 0xFFDD;
    @Column(length = 9,desc = "消息id",version = {1,2,3,4,5})
    protected String messageId;
    @Column(length = 2, desc = "寄存器地址" ,version = 0)
    protected int address;
    @Column(length = 1,desc = "从机地址",version = {0,1,2,3,4,5})
    protected int slaveId;
    @Column(length = 1,desc = "功能码" ,version = {0,1,2,3,4,5})
    protected int code;
    @Column(length = 2 ,desc ="下发寄存器",version = {1,2,3,4,5})
    protected int downAdd;
    @Column(length = 2,desc = "寄存器数量",version = {1,3,4,5})
    protected int count;
    @Column(length = 1,desc = "字节数",version = {3,5})
    protected int bitCount;
    @Column(totalUnit = 1,desc = "上报数据",version = {0,4})
    protected short[] data;
    @Column(length = 2, desc = "下发数据", version = 2)
    protected int writeData;
    @Column(totalUnit = 0,desc = "10下发数据",version = 3)
    protected short[] tenWriteData;
    @Column(totalUnit = 0,desc = "10下发数据",version = 5)
    protected byte[] bitData;
    /*原始的bit字符串*/
    protected String bitString;


    public String getBitString() {
        return bitString;
    }

    public void setBitString(String bitString) {
        this.bitString = bitString;
    }

    public byte[] getBitData() {
        return bitData;
    }

    public void setBitData(byte[] bitData) {
        this.bitData = bitData;
    }

    /**
     * crc校验
     */
    protected boolean verified = true;

    protected transient Session session;

    protected transient ByteBuf payload;

    protected transient int serialNo;

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getBitCount() {
        return bitCount;
    }

    public void setBitCount(int bitCount) {
        this.bitCount = bitCount;
    }

    public short[] getTenWriteData() {
        return tenWriteData;
    }

    public void setTenWriteData(short[] tenWriteData) {
        this.tenWriteData = tenWriteData;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public int getSlaveId() {
        return slaveId;
    }

    public void setSlaveId(int slaveId) {
        this.slaveId = slaveId;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public short[] getData() {
        return data;
    }

    public void setData(short[] data) {
        this.data = data;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public ByteBuf getPayload() {
        return payload;
    }

    public void setPayload(ByteBuf payload) {
        this.payload = payload;
    }

    public int getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(int serialNo) {
        this.serialNo = serialNo;
    }

    @Override
    public String getMessageId() {
        return messageId;
    }

    @Override
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public int getDownAdd() {
        return downAdd;
    }

    public void setDownAdd(int downAdd) {
        this.downAdd = downAdd;
    }

    public int getWriteData() {
        return writeData;
    }

    public void setWriteData(int writeData) {
        this.writeData = writeData;
    }

    public int getBegin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public int getmId() {
        return mId;
    }

    public void setmId(int mId) {
        this.mId = mId;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    protected StringBuilder toStringHead() {
        final StringBuilder sb = new StringBuilder();
        String des = ModbusCode.getDes(this.code);
        sb.append(des);
        sb.append("[");
        sb.append(",slaveId[从机地址]=").append(slaveId);
        sb.append(",code[功能码]=").append(code);
        sb.append(",length[返回数据个数]").append(data==null? 0: data.length);
        sb.append(",data[上报数据]=").append(Arrays.toString(data));
        // sb.append(",count[读取寄存器个数]=").append(len);
        sb.append(",address[寄存器地址]=").append(address);
        sb.append(",writeData[下发数据]=").append(writeData);
        sb.append("]");
        return sb;
    }

    @Override
    public String toString() {
        return ToStringBuilder.toString(toStringHead(), this, false);
    }
}
