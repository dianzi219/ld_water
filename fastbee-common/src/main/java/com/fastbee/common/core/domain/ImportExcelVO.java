package com.fastbee.common.core.domain;

import com.fastbee.common.annotation.Excel;
import lombok.Data;

/**
 * @author admin
 * @version 1.0
 * @description: TODO
 * @date 2024-07-12 16:20
 */
@Data
public class ImportExcelVO {

    @Excel(name = "ID")
    private Long id;

    @Excel(name = "城市ID")
    private String code;

    @Excel(name = "行政归属")
    private String city;

    @Excel(name = "城市简称")
    private String simCity;

    @Excel(name = "拼音")
    private String cn;

    @Excel(name = "lat")
    private String lat;

    @Excel(name = "lon")
    private String lon;

}
