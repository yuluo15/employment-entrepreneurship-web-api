package com.gxcj.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.sql.Timestamp;

@Data
@TableName("biz_project_comment")
public class ProjectCommentEntity {
    @TableId
    private String id;
    private String projectId;
    private String teacherId;

    private String content;

    private Timestamp createTime;

    // 冗余字段
    private String projectName;
    private String teacherName;
}