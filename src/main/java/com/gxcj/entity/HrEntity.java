package com.gxcj.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("biz_hr")
public class HrEntity {
    @TableId(value = "hr_id")
    private String hrId;
    private String userId;
    private String companyId;
    private String name;
    private String avatar;
    private String position;
    private String workPhone;
    private String workEmail;
    private String wechatCode;
    private Integer status;
    private Timestamp createTime;
    private Timestamp updateTime;
}
