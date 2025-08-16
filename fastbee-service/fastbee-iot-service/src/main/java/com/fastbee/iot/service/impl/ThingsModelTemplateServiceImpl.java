package com.fastbee.iot.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson2.JSONObject;
import com.fastbee.common.core.domain.entity.SysRole;
import com.fastbee.common.core.domain.entity.SysUser;
import com.fastbee.common.core.domain.model.LoginUser;
import com.fastbee.common.exception.ServiceException;
import com.fastbee.common.utils.DateUtils;
import com.fastbee.common.utils.SecurityUtils;
import com.fastbee.common.utils.StringUtils;
import com.fastbee.iot.domain.ThingsModel;
import com.fastbee.iot.model.ThingsModelItem.Datatype;
import com.fastbee.iot.model.ThingsModelItem.EnumItem;
import com.fastbee.iot.model.varTemp.EnumClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fastbee.iot.mapper.ThingsModelTemplateMapper;
import com.fastbee.iot.domain.ThingsModelTemplate;
import com.fastbee.iot.service.IThingsModelTemplateService;
import org.springframework.util.CollectionUtils;

import static com.fastbee.common.utils.SecurityUtils.getLoginUser;
import static com.fastbee.common.utils.SecurityUtils.isAdmin;

/**
 * 通用物模型Service业务层处理
 *
 * @author kerwincui
 * @date 2021-12-16
 */
@Service
@Slf4j
public class ThingsModelTemplateServiceImpl implements IThingsModelTemplateService
{
    @Autowired
    private ThingsModelTemplateMapper thingsModelTemplateMapper;

    /**
     * 查询通用物模型
     *
     * @param templateId 通用物模型主键
     * @return 通用物模型
     */
    @Override
    public ThingsModelTemplate selectThingsModelTemplateByTemplateId(Long templateId)
    {
        return thingsModelTemplateMapper.selectThingsModelTemplateByTemplateId(templateId, SecurityUtils.getLanguage());
    }

    /**
     * 查询通用物模型列表
     *
     * @param thingsModelTemplate 通用物模型
     * @return 通用物模型
     */
    @Override
    public List<ThingsModelTemplate> selectThingsModelTemplateList(ThingsModelTemplate thingsModelTemplate)
    {
        SysUser user = getLoginUser().getUser();
//        List<SysRole> roles=user.getRoles();
        // 租户
//        if(roles.stream().anyMatch(a->a.getRoleKey().equals("tenant"))){
//            thingsModelTemplate.setTenantId(user.getUserId());
//        }
        thingsModelTemplate.setLanguage(SecurityUtils.getLanguage());
        List<ThingsModelTemplate> list = thingsModelTemplateMapper.selectThingsModelTemplateList(thingsModelTemplate);
        for (ThingsModelTemplate modelTemplate : list) {
            if (null != user.getDeptId()) {
                modelTemplate.setOwner(user.getDept().getDeptUserId().equals(modelTemplate.getTenantId()));
            } else {
                modelTemplate.setOwner(user.getUserId().equals(modelTemplate.getTenantId()));
            }
        }
        return list;
    }

    /**
     * 新增通用物模型
     *
     * @param template 通用物模型
     * @return 结果
     */
    @Override
    public int insertThingsModelTemplate(ThingsModelTemplate template)
    {
        try {
            // 判断是否为管理员
            SysUser user = getLoginUser().getUser();
            if (isAdmin(user.getUserId())) {
                template.setIsSys(1);
            } else {
                template.setIsSys(0);
            }
//            List<SysRole> roles=user.getRoles();
//            for(int i=0;i<roles.size();i++){
//                if(roles.get(i).getRoleKey().equals("tenant") || roles.get(i).getRoleKey().equals("general")){
//                    template.setIsSys(0);
//                    break;
//                }
//            }
            if (null != user.getDeptId()) {
                template.setTenantId(user.getDept().getDeptUserId());
                template.setTenantName(user.getDept().getDeptUserName());
            } else {
                template.setTenantId(user.getUserId());
                template.setTenantName(user.getUserName());
            }
            template.setCreateTime(DateUtils.getNowDate());
            return thingsModelTemplateMapper.insertThingsModelTemplate(template);
        }catch (Exception e){
            if (e.getMessage().contains("iot_things_modes_slaveId_reg")){
                throw new ServiceException("同一个采集点模板下,寄存器地址重复,请检查导入变量寄存器地址");
            }else {
                throw new ServiceException(e.getMessage());
            }
        }

    }

    /**
     * 修改通用物模型
     *
     * @param template 通用物模型
     * @return 结果
     */
    @Override
    public int updateThingsModelTemplate(ThingsModelTemplate template)
    {
        template.setUpdateTime(DateUtils.getNowDate());
        return thingsModelTemplateMapper.updateThingsModelTemplate(template);
    }


    /**
     * 批量删除通用物模型
     *
     * @param templateIds 需要删除的通用物模型主键
     * @return 结果
     */
    @Override
    public int deleteThingsModelTemplateByTemplateIds(Long[] templateIds)
    {
        return thingsModelTemplateMapper.deleteThingsModelTemplateByTemplateIds(templateIds);
    }

    /**
     * 删除通用物模型信息
     *
     * @param templateId 通用物模型主键
     * @return 结果
     */
    @Override
    public int deleteThingsModelTemplateByTemplateId(Long templateId)
    {
        return thingsModelTemplateMapper.deleteThingsModelTemplateByTemplateId(templateId);
    }



    /**
     * 导入采集点数据
     *
     * @param lists       数据列表
     * @param tempSlaveId 从机编码
     * @return 结果
     */
    public String importData(List<ThingsModelTemplate> lists, String tempSlaveId) {
        if (null == tempSlaveId || CollectionUtils.isEmpty(lists)) {
            throw new ServiceException("导入数据异常");
        }
        int success = 0;
        int failure = 0;
        StringBuilder succSb = new StringBuilder();
        StringBuilder failSb = new StringBuilder();

        for (ThingsModelTemplate model : lists) {
            try {
                //处理数据定义
                this.parseSpecs(model);
                this.insertThingsModelTemplate(model);
                success++;
                succSb.append("<br/>").append(success).append(",采集点: ").append(model.getTemplateName());
            } catch (Exception e) {
                log.error("导入错误：", e);
                failure++;
                failSb.append("<br/>").append(failure).append(",采集点: ").append(model.getTemplateName()).append("导入失败");
            }
        }
        if (failure > 0) {
            throw new ServiceException(failSb.toString());
        }
        return succSb.toString();
    }

    private void parseSpecs(ThingsModelTemplate model) {
        JSONObject specs = new JSONObject();
        String datatype = model.getDatatype();
        String limitValue = model.getLimitValue();
        if (limitValue != null && !"".equals(limitValue)) {
            String[] values = limitValue.trim().split("/");
            switch (datatype) {
                case "integer":
                    specs.put("max", new BigDecimal(values[1]));
                    specs.put("min", new BigDecimal(values[0]));
                    specs.put("type", datatype);
                    specs.put("unit", model.getUnit());
                    specs.put("step", 0);
                    break;
                case "bool":
                    specs.put("type",datatype);
                    specs.put("trueText",values[1]);
                    specs.put("falseText",values[0]);
                    break;
                case "enum":
                    List<EnumClass> list = new ArrayList<>();
                    for (String value : values) {
                        String[] params = value.trim().split(":");
                        EnumClass enumCls = new EnumClass();
                        enumCls.setText(params[1]);
                        enumCls.setValue(params[0]);
                        list.add(enumCls);
                    }
                    specs.put("type",datatype);
                    specs.put("enumList",list);
                    break;
            }
            model.setSpecs(specs.toJSONString());
        }
    }

    /**
     * 导出采集点数据
     *
     * @param thingsModelTemplate 通用物模型
     * @return 通用物模型集合
     */
    @Override
    public List<ThingsModelTemplate> selectThingsModelTemplateExport(ThingsModelTemplate thingsModelTemplate){
        thingsModelTemplate.setLanguage(SecurityUtils.getLanguage());
        List<ThingsModelTemplate> thingsModelTemplates = thingsModelTemplateMapper.selectThingsModelTemplateList(thingsModelTemplate);
        for (ThingsModelTemplate template : thingsModelTemplates) {
            Datatype datatype = JSONObject.parseObject(template.getSpecs(), Datatype.class);
            switch (datatype.getType()) {
                case "integer":
                    template.setLimitValue(datatype.getMin()+ "/"+ datatype.getMax());
                    template.setDatatype(datatype.getType());
                    template.setUnit(datatype.getUnit());
                    break;
                case "bool":
                    template.setLimitValue("0:"+datatype.getFalseText()+ "/" + "1:"+datatype.getTrueText());
                    template.setDatatype(datatype.getType());
                    break;
                case "enum":
                    List<EnumItem> enumList = datatype.getEnumList();
                    StringBuilder buffer = new StringBuilder();
                    for (int i = 0; i < enumList.size(); i++) {
                        EnumItem item = enumList.get(i);
                        String s = item.getValue() + ":" + item.getText();
                        buffer.append(s);
                        if (i < enumList.size() -1){
                            buffer.append("/");
                        }
                    }
                    template.setLimitValue(buffer.toString());
                    template.setDatatype(datatype.getType());
                    break;
            }
        }
        return  thingsModelTemplates;
    }
}
