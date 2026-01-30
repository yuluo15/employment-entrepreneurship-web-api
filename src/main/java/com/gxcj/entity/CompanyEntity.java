package com.gxcj.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.sql.Timestamp;

@Data
@TableName("sys_company")
public class CompanyEntity {
    private String id;
    private String name;
    private String code;
    private String industry;
    private String scale;
    private String logo;
    private String description;
    private String address;
    private String contactPerson;
    private String contactPhone;
    private String email;
    private String licenseUrl;
    private Integer status;
    private String auditReason;
    private Timestamp auditTime;
    private String auditorId;
    private Timestamp createTime;
    private Timestamp updateTime;
    private Integer isDeleted;
    private String defaultAccountId;
    //融资阶段
    private String fundingStage;
    //标签（如：互联网、大厂）
    private String tags;
    //公司官网
    private String website;
    //公司环境图
    private String images;

    public static <T> CompanyEntity ofReq(T req){
        CompanyEntity entity = new CompanyEntity();
        BeanUtils.copyProperties(req,entity);
        return entity;
    }
}