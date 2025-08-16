package com.fastbee.common.core.domain.entity;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fastbee.common.core.domain.BaseEntity;

/**
 * 部门表 sys_dept
 *
 * @author ruoyi
 */
@ApiModel(value = "SysDept", description = "部门表 sys_dept")
public class SysDept extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 部门ID */
    @ApiModelProperty("部门ID")
    private Long deptId;

    /**
     * 机构系统账号ID
     */
    private Long deptUserId;

    /** 父部门ID */
    @ApiModelProperty("父部门ID")
    private Long parentId;

    /** 祖级列表 */
    @ApiModelProperty("祖级列表")
    private String ancestors;

    /** 部门名称 */
    @ApiModelProperty("部门名称")
    private String deptName;

    /** 显示顺序 */
    @ApiModelProperty("显示顺序")
    private Integer orderNum;

    /** 负责人 */
    @ApiModelProperty("负责人")
    private String leader;

    /** 联系电话 */
    @ApiModelProperty("联系电话")
    private String phone;

    /** 邮箱 */
    @ApiModelProperty("邮箱")
    private String email;

    /** 部门状态:0正常,1停用 */
    @ApiModelProperty("部门状态:0正常,1停用")
    private String status;

    /** 删除标志（0代表存在 2代表删除） */
    @ApiModelProperty("删除标志（0代表存在 2代表删除）")
    private String delFlag;

    /** 父部门名称 */
    @ApiModelProperty("父部门名称")
    private String parentName;

    /** 子部门 */
    @ApiModelProperty("子部门")
    private List<SysDept> children = new ArrayList<SysDept>();

    /**
     * 系统账号名称
     */
    private String userName;

    /**
     * 系统账号密码
     */
    private String password;

    /**
     * 确认密码
     */
    private String confirmPassword;

    /**
     * 机构类型
     */
    private Integer deptType;

    public Boolean getShowOwner() {
        return showOwner;
    }

    public void setShowOwner(Boolean showOwner) {
        this.showOwner = showOwner;
    }

    /**
     * 是否显示自己
     */
    private Boolean showOwner;

    /**
     * 管理员姓名
     */
    private String deptUserName;

    public String getDeptUserName() {
        return deptUserName;
    }

    public void setDeptUserName(String deptUserName) {
        this.deptUserName = deptUserName;
    }

    public Integer getDeptType() {
        return deptType;
    }

    public void setDeptType(Integer deptType) {
        this.deptType = deptType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public Long getDeptUserId() {
        return deptUserId;
    }

    public void setDeptUserId(Long deptUserId) {
        this.deptUserId = deptUserId;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    public Long getParentId()
    {
        return parentId;
    }

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    public String getAncestors()
    {
        return ancestors;
    }

    public void setAncestors(String ancestors)
    {
        this.ancestors = ancestors;
    }

    @NotBlank(message = "部门名称不能为空")
    @Size(min = 0, max = 30, message = "部门名称长度不能超过30个字符")
    public String getDeptName()
    {
        return deptName;
    }

    public void setDeptName(String deptName)
    {
        this.deptName = deptName;
    }

    @NotNull(message = "显示顺序不能为空")
    public Integer getOrderNum()
    {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum)
    {
        this.orderNum = orderNum;
    }

    public String getLeader()
    {
        return leader;
    }

    public void setLeader(String leader)
    {
        this.leader = leader;
    }

    @Size(min = 0, max = 11, message = "联系电话长度不能超过11个字符")
    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    @Email(message = "邮箱格式不正确")
    @Size(min = 0, max = 50, message = "邮箱长度不能超过50个字符")
    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getParentName()
    {
        return parentName;
    }

    public void setParentName(String parentName)
    {
        this.parentName = parentName;
    }

    public List<SysDept> getChildren()
    {
        return children;
    }

    public void setChildren(List<SysDept> children)
    {
        this.children = children;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("deptId", getDeptId())
            .append("deptUserId", getDeptUserId())
            .append("parentId", getParentId())
            .append("ancestors", getAncestors())
            .append("deptName", getDeptName())
            .append("orderNum", getOrderNum())
            .append("leader", getLeader())
            .append("phone", getPhone())
            .append("email", getEmail())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
