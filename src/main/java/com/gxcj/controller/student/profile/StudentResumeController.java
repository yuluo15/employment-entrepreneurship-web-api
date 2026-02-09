package com.gxcj.controller.student.profile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.gxcj.context.UserContext;
import com.gxcj.entity.StudentEntity;
import com.gxcj.entity.StudentResumeEntity;
import com.gxcj.mapper.StudentMapper;
import com.gxcj.mapper.StudentResumeMapper;
import com.gxcj.result.Result;
import com.gxcj.utils.EntityHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/student/resume")
public class StudentResumeController {

    @Autowired
    private StudentResumeMapper studentResumeMapper;
    @Autowired
    private StudentMapper studentMapper;

    @GetMapping("/detail")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<StudentResumeEntity> getResumeDetail() {

        StudentEntity studentEntity = studentMapper.selectOne(new LambdaQueryWrapper<StudentEntity>()
                .eq(StudentEntity::getUserId, UserContext.getUserId()));
        String studentId = studentEntity.getStudentId();

        StudentResumeEntity resume = studentResumeMapper.selectOne(new LambdaQueryWrapper<StudentResumeEntity>()
                .eq(StudentResumeEntity::getStudentId, studentId));

        if (resume == null) {
            resume = new StudentResumeEntity();
            resume.setResumeId(EntityHelper.uuid());
            resume.setStudentId(studentId);

            // resume.setName(student.getName()); // 假设简历表里有冗余name
            // resume.setPhone(student.getPhone());

            // 初始化 JSON 列表防止前端空指针
            resume.setEducationHistory(new ArrayList<>());
            resume.setInternshipExp(new ArrayList<>());
            resume.setProjectExp(new ArrayList<>());
            resume.setCertificates(new ArrayList<>());

            resume.setResumeScore(0);
            studentResumeMapper.insert(resume);
        }

        return Result.success(resume);
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<Void> saveResume(@RequestBody StudentResumeEntity resume) {

        StudentEntity studentEntity = studentMapper.selectOne(new LambdaQueryWrapper<StudentEntity>()
                .eq(StudentEntity::getUserId, UserContext.getUserId()));
        resume.setStudentId(studentEntity.getStudentId());

        int score = calculateCompleteness(resume);
        resume.setResumeScore(score);

        // 更新数据库
        // 因为用了 @TableField(typeHandler = JacksonTypeHandler.class)
        // MyBatis-Plus 会自动把 List 转成 JSONB 存进去
        studentResumeMapper.update(resume, new LambdaQueryWrapper<StudentResumeEntity>()
                .eq(StudentResumeEntity::getStudentId, studentEntity.getStudentId()));

        return Result.success();
    }


    private int calculateCompleteness(StudentResumeEntity r) {
        int score = 0;
        if (StringUtils.isNotBlank(r.getExpectedPosition())) score += 20; // 意向
        if (StringUtils.isNotBlank(r.getPersonalSummary())) score += 20;  // 优势
        if (r.getEducationHistory() != null && !r.getEducationHistory().isEmpty()) score += 20; // 学历
        if (r.getInternshipExp() != null && !r.getInternshipExp().isEmpty()) score += 20; // 实习
        if (r.getProjectExp() != null && !r.getProjectExp().isEmpty()) score += 20; // 项目
        return Math.min(score, 100);
    }
}
