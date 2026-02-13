package com.gxcj.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("sys_notice")
public class NoticeEntity {

    @TableId
    private String noticeId;

    private String noticeTitle;

    /**
     * 字典类型: sys_notice_type
     * 1:通知, 2:公告, 3:政策, 4:新闻
     */
    private String noticeType;

    /**
     * 富文本 HTML 内容
     */
    private String noticeContent;

    /**
     * 0:草稿, 1:发布
     */
    private Integer status;

    /**
     * 0:否, 1:是
     */
    private Integer isTop;

    private Integer viewCount;

    /**
     * 附件 (建议前端传 JSON 字符串或逗号分隔)
     */
//    private String attachments;

    /**
     * 发布者类型: admin=管理员, school=学校
     */
    private String publisherType;

    /**
     * 发布者ID: 对于学校发布的公告，存储 school_id
     */
    private String publisherId;

    /**
     * 目标受众: all=所有人, school=学校教职工, student=学生
     */
    private String targetAudience;

    private String createBy;
    private Timestamp createTime;
    private Timestamp updateTime;
    private Timestamp publishTime;
}
