package com.fastbee.pay.core.controller.admin.app.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ApiModel(description = "管理后台 - 支付应用信息创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayAppCreateReqVO extends PayAppBaseVO {

}
