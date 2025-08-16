package com.fastbee.modbustcp.model;

import com.fastbee.common.core.protocol.Message;
import com.fastbee.common.core.protocol.modbus.ModbusCode;
import com.fastbee.protocol.base.annotation.Column;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Modbus TCP 报文由以下几个主要部分组成：
 *
 * 1. **MBAP 报文头（Modbus Application Protocol Header）**：
 *    - **事务标识符（Transaction Identifier）**：用于匹配请求和响应，通常由客户端生成。
 *    - **协议标识符（Protocol Identifier）**：固定为 0，表示 Modbus 协议。
 *    - **长度（Length）**：后续数据的字节长度，包括单元标识符和 PDU （Protocol Data Unit）。
 *    - **单元标识符（Unit Identifier）**：用于标识从站设备。
 * 2. **PDU（Protocol Data Unit）**：
 *    - **功能码（Function Code）**：指示要执行的操作，如读取、写入等。
 *    - **数据（Data）**：根据功能码的不同，包含相应的操作数据。
 *
 * @author bill
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class ModbusTcp extends Message {
    /**
     * version来决定数据
     * <p>
     * 0: 读寄存器03/04(批量)上报  {@link ModbusCode}
     * 例如:    |事务标识符|协议标识符|长度|单元标识符|功能码|起始地址|寄存器数量
     * 00 01 00 00 00 06 01 01 00 02 00 04
     * <p>
     * 1: 批量读寄存器      {@link ModbusCode}
     * <p>
     * 2: 单个写保持寄存器 0x06   {@link ModbusCode}
     *    写单个线圈 0x05 {@link ModbusCode}
     * <p>
     * 4:  解析读线圈 01/02
     * <p>
     * 15: 写多个线圈
     * <p>
     * 16: 写多个寄存器
     * <p>
     */
//    @Column(length = 2, version = {0x0000, 0xFFFF}, desc = "事务标识符")
    @Column(length = 2, desc = "事务标识符")
    protected int transactionIdentifier;
    @Column(length = 2, desc = "协议标识符")
    protected int protocolIdentifier = 0;
    @Column(length = 2, desc = "字节长度")
    protected int bitLength;
    @Column(length = 1, version = {0, 1, 2,4,15,16}, desc = "从机地址")
    protected int slaveId;
    @Column(length = 1, version = {0,1,2,4,15,16}, desc = "功能码")
    protected int code;
    @Column(length = 2, desc = "寄存器地址", version = {1,2,15, 16})
    protected int address;
    @Column(length = 2, desc = "寄存器个数", version = {1,15,16})
    protected int count;
    @Column(length = 1 ,version = {4,15,16} , desc = "字节数")
    protected int bitCount;
    @Column(totalUnit = 1, desc = "上报数据", version = 0)
    protected short[] data;

    @Column(length = 2, desc = "下发数据", version = 2)
    protected int writeData;
    @Column(totalUnit = 0,desc = "多个寄存器",version = 16)
    protected short[] tenWriteData;
    @Column(totalUnit = 0,desc = "多个线圈",version = {4,15})
    protected byte[] bitData;

    protected String serialNumber;

    /**
     * 原始报文
     */
    protected String sourceData;

}
