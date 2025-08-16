package com.fastbee.system.mapper;

import com.fastbee.common.core.domain.entity.SysDictData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典表 数据层
 *
 * @author ruoyi
 */
public interface SysDictDataMapper
{
    /**
     * 根据条件分页查询字典数据
     *
     * @param dictData 字典数据信息
     * @return 字典数据集合信息
     */
    public List<SysDictData> selectDictDataList(SysDictData dictData);

    /**
     * 根据条件分页查询字典数据--返回所有语言
     *
     * @param dictData 字典数据信息
     * @return 字典数据集合信息
     */
    public List<SysDictData> selectDictDataListAll(SysDictData dictData);

    /**
     * 根据字典类型查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据集合信息
     */
    public List<SysDictData> selectDictDataByType(@Param("dictType") String dictType);

    /**
     * 根据字典类型和字典键值查询字典数据信息
     *
     * @param dictType 字典类型
     * @param dictValue 字典键值
     * @return 字典标签
     */
    public String selectDictLabel(@Param("dictType") String dictType, @Param("dictValue") String dictValue);

    /**
     * 根据字典数据ID查询信息
     *
     * @param dictCode 字典数据ID
     * @param language 语言
     * @return 字典数据
     */
    public SysDictData selectDictDataById(@Param("dictCode") Long dictCode, @Param("language") String language);

    /**
     * 查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据
     */
    public int countDictDataByType(String dictType);

    /**
     * 通过字典ID删除字典数据信息
     *
     * @param dictCode 字典数据ID
     * @return 结果
     */
    public int deleteDictDataById(Long dictCode);

    /**
     * 批量删除字典数据信息
     *
     * @param dictCodes 需要删除的字典数据ID
     * @return 结果
     */
    public int deleteDictDataByIds(Long[] dictCodes);

    /**
     * 新增字典数据信息
     *
     * @param dictData 字典数据信息
     * @return 结果
     */
    public int insertDictData(SysDictData dictData);

    /**
     * 修改字典数据信息
     *
     * @param dictData 字典数据信息
     * @return 结果
     */
    public int updateDictData(SysDictData dictData);

    /**
     * 同步修改字典类型
     *
     * @param oldDictType 旧字典类型
     * @param newDictType 新旧字典类型
     * @return 结果
     */
    public int updateDictDataType(@Param("oldDictType") String oldDictType, @Param("newDictType") String newDictType);

    /**
     * 根据字典分类查询字典数据
     *
     * @param dictTypeList 字典分类信息
     * @param language 语言
     * @return 字典数据集合信息
     */
    List<SysDictData> selectDictDataListByDictTypes(@Param("dictTypeList") List<String> dictTypeList, @Param("language") String language);

    /**
     * 根据字典类型和字典键值查询字典数据信息
     *
     * @param dictType 字典类型
     * @param dictValue 字典键值
     * @param language 语言
     * @return 字典标签
     */
    SysDictData selectByDictTypeAndDictValue(@Param("dictType") String dictType, @Param("dictValue") String dictValue, @Param("language") String language);

    /**
     * 根据字典类型和字典键值集合查询字典数据信息
     *
     * @param dictType 字典类型
     * @param dictValueList 字典键值集合
     * @param language 语言
     * @return 字典标签
     */
    List<SysDictData> listByDictTypeAndDictValue(@Param("dictType") String dictType, @Param("dictValueList") List<String> dictValueList, @Param("language") String language);
}
