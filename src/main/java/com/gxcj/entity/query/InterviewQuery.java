package com.gxcj.entity.query;

import lombok.Data;

@Data
public class InterviewQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String studentName;    // 学生姓名（模糊查询）
    private Integer status;        // 面试状态：0=待面试, 1=已完成, 2=已取消
    private String startDate;      // 面试开始日期 YYYY-MM-DD
    private String endDate;        // 面试结束日期 YYYY-MM-DD
}
