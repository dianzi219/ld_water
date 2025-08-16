package com.fastbee.iot.mapper;

import com.fastbee.framework.mybatis.mapper.BaseMapperX;
import com.fastbee.iot.domain.ProductSubGateway;
import com.fastbee.iot.model.gateWay.ProductSubGatewayVO;

import java.util.List;

/**
 * 网关与子产品关联Mapper接口
 *
 * @author zhuangpeng.li
 * @date 2024-09-04
 */
public interface ProductSubGatewayMapper extends BaseMapperX<ProductSubGateway>
{

    /**
     * @description: 查询网关
     * @param: productSubGateway 网关
     * @return: java.util.List<com.fastbee.iot.model.gateWay.ProductSubGatewayVO>
     */
    List<ProductSubGatewayVO> selectListVO(ProductSubGateway productSubGateway);
}
