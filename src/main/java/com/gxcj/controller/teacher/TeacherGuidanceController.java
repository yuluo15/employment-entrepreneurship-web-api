package com.gxcj.controller.teacher;

import com.gxcj.entity.dto.TeacherGuidanceAddDto;
import com.gxcj.entity.query.TeacherGuidanceQuery;
import com.gxcj.entity.vo.teacher.TeacherGuidanceStatsVo;
import com.gxcj.entity.vo.teacher.TeacherGuidanceVo;
import com.gxcj.result.PageResult;
import com.gxcj.result.Result;
import com.gxcj.service.TeacherGuidanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/guidance")
public class TeacherGuidanceController {

    @Autowired
    private TeacherGuidanceService teacherGuidanceService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public Result<TeacherGuidanceStatsVo> getStats() {
        TeacherGuidanceStatsVo stats = teacherGuidanceService.getStats();
        return Result.success(stats);
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public Result<PageResult<TeacherGuidanceVo>> getList(TeacherGuidanceQuery query) {
        PageResult<TeacherGuidanceVo> result = teacherGuidanceService.getList(query);
        return Result.success(result);
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public Result<Map<String, String>> addGuidance(@RequestBody TeacherGuidanceAddDto dto) {
        String guidanceId = teacherGuidanceService.addGuidance(dto);
        Map<String, String> data = new HashMap<>();
        data.put("guidanceId", guidanceId);
        return Result.success(data);
    }
}
