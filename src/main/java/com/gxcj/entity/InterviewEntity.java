package com.gxcj.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

/**
 * 面试日程安排表
 */
@Data
@TableName("biz_interview")
public class InterviewEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId
    private String id;

    /**
     * 关联的投递记录ID
     */
    private String deliveryId;

    /**
     * 公司ID (冗余字段，方便查询)
     */
    private String companyId;

    /**
     * 学生ID (冗余字段，方便查询)
     */
    private String studentId;

    /**
     * 职位ID (冗余字段，方便查询)
     */
    private String jobId;

    /**
     * 发起面试的HR ID
     */
    private String hrId;

    /**
     * 面试时间
     */
    private Timestamp interviewTime;

    /**
     * 预计时长(分钟)
     */
    private Integer duration;

    /**
     * 面试形式 (1:线下, 2:视频面试, 3:电话面试)
     */
    private Integer type;

    /**
     * 线下地址 或 线上会议链接
     */
    private String location;

    /**
     * 给学生的注意事项/备注
     */
    private String notes;

    /**
     * 状态 (0:待面试, 1:已完成, 2:已取消, 3:学生缺席)
     */
    private Integer status;

    /**
     * 面试评分 (1-10分)
     */
    private Integer interviewScore;

    /**
     * 面试官评价
     */
    private String interviewComment;

    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 更新时间
     */
    private Timestamp updateTime;
}
