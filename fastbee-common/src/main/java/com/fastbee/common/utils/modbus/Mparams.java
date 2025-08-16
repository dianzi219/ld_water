package com.fastbee.common.utils.modbus;

import lombok.Data;

/**
 * @author bill
 */
@Data
public class Mparams {

    private int slaveId;

    private int code;

    private int address;
}
