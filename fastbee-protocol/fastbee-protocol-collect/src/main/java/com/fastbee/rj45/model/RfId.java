package com.fastbee.rj45.model;

import lombok.Data;

/**
 * @author bill
 */
@Data
public class RfId {

    /*标签类型*/
    private String labelType;
    /*天线号*/
    private String modelNo;
    /*EPC*/
    private String epc;
}
