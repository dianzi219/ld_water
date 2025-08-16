package com.fastbee.iot.service;

import java.util.List;
import com.fastbee.iot.domain.ProductSubGateway;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fastbee.iot.model.gateWay.ProductSubGatewayAddVO;
import com.fastbee.iot.model.gateWay.ProductSubGatewayVO;

/**
 * 网关与子产品关联Service接口
 *
 * @author zhuangpeng.li
 * @date 2024-09-04
 */
public interface IProductSubGatewayService extends IService<ProductSubGateway>
{

    /**
     * 查询网关与子产品关联列表
     *
     * @param productSubGateway 网关与子产品关联
     * @return 网关与子产品关联集合
     */
     List<ProductSubGatewayVO> selectProductSubGatewayList(ProductSubGateway productSubGateway);

    /**
     * 查询网关与子产品关联
     *
     * @param id 主键
     * @return 网关与子产品关联
     */
     ProductSubGateway selectProductSubGatewayById(Long id);

    /**
     * 查询网关与子产品关联
     *
     * @param id 主键
     * @return 网关与子产品关联
     */
    ProductSubGateway queryByIdWithCache(Long id);

    /**
     * 新增网关与子产品关联
     *
     * @param productSubGateway 网关与子产品关联
     * @return 是否新增成功
     */
    Boolean insertWithCache(ProductSubGateway productSubGateway);

    /**
     * 修改网关与子产品关联
     *
     * @param productSubGateway 网关与子产品关联
     * @return 是否修改成功
     */
    Boolean updateWithCache(ProductSubGateway productSubGateway);

    /**
     * 校验并批量删除网关与子产品关联信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithCacheByIds(Long[] ids, Boolean isValid);

    /**
     * @description: 批量新增
     * @param: productSubGatewayAddVO 传参
     * @return: int
     */
    int addBatch(ProductSubGatewayAddVO productSubGatewayAddVO);

    /**
     * @description: 批量修改
     * @param: list 集合
     * @return: int
     */
    int editBatch(List<ProductSubGateway> list);
}
