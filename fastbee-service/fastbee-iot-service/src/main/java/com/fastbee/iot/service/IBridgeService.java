package com.fastbee.iot.service;

import java.util.List;
import com.fastbee.iot.domain.Bridge;
import com.baomidou.mybatisplus.extension.service.IService;
/**
 * 数据桥接Service接口
 *
 * @author kerwincui
 * @date 2024-08-20
 */
public interface IBridgeService extends IService<Bridge>
{

    /**
     * 查询数据桥接列表
     *
     * @param bridge 数据桥接
     * @return 数据桥接集合
     */
     List<Bridge> selectBridgeList(Bridge bridge);

    /**
     * 查询数据桥接
     *
     * @param id 主键
     * @return 数据桥接
     */
     Bridge selectBridgeById(Long id);

    /**
     * 查询数据桥接
     *
     * @param id 主键
     * @return 数据桥接
     */
    Bridge queryByIdWithCache(Long id);

    /**
     * 新增数据桥接
     *
     * @param bridge 数据桥接
     * @return 是否新增成功
     */
    Boolean insertWithCache(Bridge bridge);

    /**
     * 修改数据桥接
     *
     * @param bridge 数据桥接
     * @return 是否修改成功
     */
    Boolean updateWithCache(Bridge bridge);

    int connect(Bridge bridge);

    /**
     * 校验并批量删除数据桥接信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithCacheByIds(Long[] ids, Boolean isValid);

}
