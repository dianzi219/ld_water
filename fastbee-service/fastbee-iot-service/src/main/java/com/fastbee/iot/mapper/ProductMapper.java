package com.fastbee.iot.mapper;

import com.fastbee.iot.domain.Product;
import com.fastbee.iot.model.ChangeProductStatusModel;
import com.fastbee.iot.model.IdAndName;
import com.fastbee.iot.model.ProductCode;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 产品Mapper接口
 *
 * @author kerwincui
 * @date 2021-12-16
 */
@Repository
public interface ProductMapper
{
    /**
     * 查询产品
     *
     * @param productId 产品主键
     * @return 产品
     */
    public Product selectProductByProductId(Long productId);

    /**
     * 查询产品列表
     *
     * @param product 产品
     * @return 产品集合
     */
    public List<Product> selectProductList(Product product);

    /**
     * 查询产品简短列表
     *
     * @param product 产品
     * @return 产品集合
     */
    public List<IdAndName> selectProductShortList(Product product);

    /**
     * 新增产品
     *
     * @param product 产品
     * @return 结果
     */
    public int insertProduct(Product product);

    /**
     * 修改产品
     *
     * @param product 产品
     * @return 结果
     */
    public int updateProduct(Product product);

    /**
     * 更新产品状态，1-未发布，2-已发布
     *
     * @param model
     * @return 结果
     */
    public int changeProductStatus(ChangeProductStatusModel model);

    /**
     * 修改物模型JSON
     *
     * @param product 产品
     * @return 结果
     */
    public int updateThingsModelJson(Product product);

    /**
     * 删除产品
     *
     * @param productId 产品主键
     * @return 结果
     */
    public int deleteProductByProductId(Long productId);

    /**
     * 批量删除产品
     *
     * @param productIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteProductByProductIds(Long[] productIds);

    /**
     * 批量删除产品物模型
     *
     * @param productIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteProductThingsModelByProductIds(Long[] productIds);

    /**
     * 产品下的固件数量
     * @param productIds 需要删除的数据主键集合
     * @return 结果
     */
    public int firmwareCountInProducts(Long[] productIds);

    /**
     * 产品下的设备数量
     * @param productIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deviceCountInProducts(Long[] productIds);

    /**
     * 产品下的物模型数量
     * @param productId 需要删除的数据主键集合
     * @return 结果
     */
    public int thingsCountInProduct(Long productId);

    /**
     * 产品下的物模型标识符重复数
     * @param productId 需要删除的数据主键集合
     * @return 结果
     */
    public int thingsRepeatCountInProduct(Long productId);

    /**
     * 根据产品id获取协议编号
     * @param productId 产品id
     * @return 协议编号
     */
    public String getProtocolByProductId(Long productId);

    /**
     * 根据模板id查询所有使用的产品
     * @param templeId 模板id
     * @return
     */
    public List<Product> selectByTempleId(Long templeId);

    /**
     * 查询产品图片
     * @param productId 产品id
     * @return java.lang.String
     */
    String selectImgUrlByProductId(Long productId);

    /**
     * 查询终端用户产品
     * @param product
     * @return java.util.List<com.fastbee.iot.domain.Product>
     */
    List<Product> selectTerminalUserProduct(Product product);


    /**
     * 根据产品id获取关联组态guid
     * @param productId
     * @return guid
     */
    String selectGuidByProductId(Long productId);

    /**
     * 查询组态信息
     * @param guidList 组态id集合
     * @return java.util.List<com.fastbee.iot.domain.Product>
     */
    List<Product> selectListScadaIdByGuidS(@Param("guidList") List<String> guidList);
    /**
     * @description:
     * @param: productIdList 产品id集合
     * @return: java.util.List<com.fastbee.iot.domain.Product>
     */
    List<Product> selectProductListByProductIds(@Param("productIdList") List<Long> productIdList);
}
