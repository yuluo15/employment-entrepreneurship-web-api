package com.gxcj.controller.school;

import com.gxcj.context.UserContext;
import com.gxcj.entity.query.school.SchoolTeacherQuery;
import com.gxcj.entity.vo.school.SchoolTeacherImportResultVo;
import com.gxcj.entity.vo.school.SchoolTeacherVo;
import com.gxcj.result.PageResult;
import com.gxcj.result.Result;
import com.gxcj.service.SchoolTeacherService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/school/teacher")
@PreAuthorize("hasAnyRole('ROLE_SCHOOL', 'ROLE_SCHOOL_ADMIN', 'ROLE_TEACHER')")
public class SchoolTeacherController {

    @Autowired
    private SchoolTeacherService teacherService;

    /**
     * 获取教师列表
     */
    @GetMapping("/list")
    public Result<PageResult<SchoolTeacherVo>> getTeacherList(SchoolTeacherQuery query) {
        String userId = UserContext.getUserId();
        PageResult<SchoolTeacherVo> result = teacherService.getTeacherList(query, userId);
        return Result.success(result);
    }

    /**
     * 获取教师详情
     */
    @GetMapping("/detail/{teacherId}")
    public Result<SchoolTeacherVo> getTeacherDetail(@PathVariable String teacherId) {
        String userId = UserContext.getUserId();
        SchoolTeacherVo teacher = teacherService.getTeacherDetail(teacherId, userId);
        return Result.success(teacher);
    }

    /**
     * 下载导入模板
     */
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) {
        teacherService.downloadTemplate(response);
    }

    /**
     * 批量导入教师
     */
    @PostMapping("/import")
    public Result<SchoolTeacherImportResultVo> importTeachers(@RequestParam("file") MultipartFile file) {
        String userId = UserContext.getUserId();
        SchoolTeacherImportResultVo result = teacherService.importTeachers(file, userId);
        return Result.success(result);
    }

    /**
     * 导出教师数据
     */
    @GetMapping("/export")
    public void exportTeacherData(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String employeeNo,
            @RequestParam(required = false) String collegeName,
            @RequestParam(required = false) String title,
            HttpServletResponse response) {
        
        SchoolTeacherQuery query = new SchoolTeacherQuery();
        query.setName(name);
        query.setEmployeeNo(employeeNo);
        query.setCollegeName(collegeName);
        query.setTitle(title);
        
        teacherService.exportTeacherData(query, UserContext.getUserId(), response);
    }
}
