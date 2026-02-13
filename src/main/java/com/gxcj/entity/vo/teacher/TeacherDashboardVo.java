package com.gxcj.entity.vo.teacher;

import lombok.Data;

import java.util.List;

@Data
public class TeacherDashboardVo {
    private TeacherInfoVo teacherInfo;
    private TeacherStatsVo stats;
    private List<TeacherPendingProjectVo> pendingProjects;
    private List<TeacherRecentGuidanceVo> recentGuidance;
}
