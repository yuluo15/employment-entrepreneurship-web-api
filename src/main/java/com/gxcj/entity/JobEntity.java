package com.gxcj.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("biz_job")
public class JobEntity {
    @TableId(value = "job_id")
    private String id;
    private String companyId;
    private String jobName;
    private String salaryRange;
    private String city;
    private String education;
    private String tags;
    private String description;
    private String requirement;
    private String contactPhone;
    private Integer status;
    private Integer viewCount;
    private Timestamp createTime;
    private String hrId;
    private String experience;
}
