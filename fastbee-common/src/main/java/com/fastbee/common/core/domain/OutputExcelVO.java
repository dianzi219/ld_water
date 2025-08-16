package com.fastbee.common.core.domain;

import lombok.Data;

import java.util.List;

/**
 * @author admin
 * @version 1.0
 * @description: TODO
 * @date 2024-07-12 16:25
 */
@Data
public class OutputExcelVO {

    private String code;

    private String name;

    private String lat;

    private String lon;

    private List<OutputExcelVO> children;
}
