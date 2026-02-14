package com.gxcj.controller.student;

import com.gxcj.context.UserContext;
import com.gxcj.entity.StudentEntity;
import com.gxcj.entity.vo.JobRecommendationVo;
import com.gxcj.mapper.StudentMapper;
import com.gxcj.result.Result;
import com.gxcj.service.AIRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mobile/ai")
public class AIRecommendationController {

    @Autowired
    private AIRecommendationService recommendationService;

    @Autowired
    private StudentMapper studentMapper;

    /**
     * 获取AI推荐职位
     */
    @GetMapping("/recommend/jobs")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<List<JobRecommendationVo>> recommendJobs(
            @RequestParam(defaultValue = "10") int limit) {
        String userId = UserContext.getUserId();
        
        // 获取学生ID
        StudentEntity student = studentMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<StudentEntity>()
                .eq(StudentEntity::getUserId, userId)
        );
        
        if (student == null) {
            return Result.fail("学生信息不存在");
        }
        
        List<JobRecommendationVo> recommendations = 
            recommendationService.recommendJobs(student.getStudentId(), limit);
        return Result.success(recommendations);
    }

    /**
     * 刷新简历向量（简历更新后调用）
     */
    @PostMapping("/refresh/resume")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<Void> refreshResumeEmbedding() {
        String userId = UserContext.getUserId();
        
        // 获取学生ID
        StudentEntity student = studentMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<StudentEntity>()
                .eq(StudentEntity::getUserId, userId)
        );
        
        if (student == null) {
            return Result.fail("学生信息不存在");
        }
        
        recommendationService.generateResumeEmbedding(student.getStudentId());
        return Result.success();
    }
}
