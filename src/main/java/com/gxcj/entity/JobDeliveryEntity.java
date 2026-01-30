package com.gxcj.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.sql.Timestamp;

@Data
@TableName("biz_job_delivery")
public class JobDeliveryEntity {
    private String id;
    private String studentId;
    private String jobId;
    private String companyId;
    private String resumeId;
    private String status;
    private Timestamp createTime;
    private Timestamp updateTime;
}
