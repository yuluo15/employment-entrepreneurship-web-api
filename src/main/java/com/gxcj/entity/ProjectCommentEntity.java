package com.gxcj.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("biz_project_comment")
public class ProjectCommentEntity {
    private String id;
    private String projectId;
    private String stageId;
    private String teacherId;
    private String content;
    private Timestamp createTime;
    private String projectName;
    private String teacherName;
}
