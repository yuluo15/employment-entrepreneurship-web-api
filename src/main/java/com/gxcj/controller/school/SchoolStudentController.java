package com.gxcj.controller.school;

import com.gxcj.context.UserContext;
import com.gxcj.entity.query.school.SchoolStudentQuery;
import com.gxcj.entity.vo.school.SchoolStudentImportResultVo;
import com.gxcj.entity.vo.school.SchoolStudentResumeVo;
import com.gxcj.entity.vo.school.SchoolStudentVo;
import com.gxcj.result.PageResult;
import com.gxcj.result.Result;
import com.gxcj.service.SchoolStudentService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 学校端学生档案管理接口
 */
@RestController
@RequestMapping("/api/school/student")
public class SchoolStudentController {

    @Autowired
    private SchoolStudentService schoolStudentService;

    /**
     * 1. 获取学生列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ROLE_SCHOOL', 'ROLE_SCHOOL_ADMIN')")
    public Result<PageResult<SchoolStudentVo>> getStudentList(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) String collegeName,
            @RequestParam(required = false) String majorName,
            @RequestParam(required = false) Integer enrollmentYear,
            @RequestParam(required = false) String employmentStatus) {
        
        SchoolStudentQuery query = new SchoolStudentQuery();
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);
        query.setStudentName(studentName);
        query.setStudentNo(studentNo);
        query.setCollegeName(collegeName);
        query.setMajorName(majorName);
        query.setEnrollmentYear(enrollmentYear);
        query.setEmploymentStatus(employmentStatus);
        
        PageResult<SchoolStudentVo> result = schoolStudentService.getStudentList(query, UserContext.getUserId());
        return Result.success(result);
    }

    /**
     * 2. 获取学生详情
     */
    @GetMapping("/detail/{studentId}")
    @PreAuthorize("hasAnyRole('ROLE_SCHOOL', 'ROLE_SCHOOL_ADMIN')")
    public Result<SchoolStudentVo> getStudentDetail(@PathVariable String studentId) {
        SchoolStudentVo student = schoolStudentService.getStudentDetail(studentId, UserContext.getUserId());
        return Result.success(student);
    }

    /**
     * 3. 获取学生简历
     */
    @GetMapping("/resume/{studentId}")
    @PreAuthorize("hasAnyRole('ROLE_SCHOOL', 'ROLE_SCHOOL_ADMIN')")
    public Result<SchoolStudentResumeVo> getStudentResume(@PathVariable String studentId) {
        SchoolStudentResumeVo resume = schoolStudentService.getStudentResume(studentId, UserContext.getUserId());
        return Result.success(resume);
    }

    /**
     * 4. 下载导入模板
     */
    @GetMapping("/template")
    @PreAuthorize("hasAnyRole('ROLE_SCHOOL', 'ROLE_SCHOOL_ADMIN')")
    public void downloadTemplate(HttpServletResponse response) {
        schoolStudentService.downloadTemplate(response);
    }

    /**
     * 5. 批量导入学生
     */
    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ROLE_SCHOOL', 'ROLE_SCHOOL_ADMIN')")
    public Result<SchoolStudentImportResultVo> importStudents(@RequestParam("file") MultipartFile file) {
        SchoolStudentImportResultVo result = schoolStudentService.importStudents(file, UserContext.getUserId());
        return Result.success(result);
    }

    /**
     * 6. 导出学生数据
     */
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ROLE_SCHOOL', 'ROLE_SCHOOL_ADMIN')")
    public void exportStudentData(
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) String collegeName,
            @RequestParam(required = false) String majorName,
            @RequestParam(required = false) Integer enrollmentYear,
            @RequestParam(required = false) String employmentStatus,
            HttpServletResponse response) {
        
        SchoolStudentQuery query = new SchoolStudentQuery();
        query.setStudentName(studentName);
        query.setStudentNo(studentNo);
        query.setCollegeName(collegeName);
        query.setMajorName(majorName);
        query.setEnrollmentYear(enrollmentYear);
        query.setEmploymentStatus(employmentStatus);
        
        schoolStudentService.exportStudentData(query, UserContext.getUserId(), response);
    }
}
