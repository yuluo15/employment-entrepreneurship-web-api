package com.gxcj.service;

import com.gxcj.entity.query.school.SchoolStudentQuery;
import com.gxcj.entity.vo.school.SchoolStudentImportResultVo;
import com.gxcj.entity.vo.school.SchoolStudentResumeVo;
import com.gxcj.entity.vo.school.SchoolStudentVo;
import com.gxcj.result.PageResult;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 学校端学生档案管理服务
 */
public interface SchoolStudentService {
    
    /**
     * 获取学生列表
     */
    PageResult<SchoolStudentVo> getStudentList(SchoolStudentQuery query, String userId);
    
    /**
     * 获取学生详情
     */
    SchoolStudentVo getStudentDetail(String studentId, String userId);
    
    /**
     * 获取学生简历
     */
    SchoolStudentResumeVo getStudentResume(String studentId, String userId);
    
    /**
     * 下载导入模板
     */
    void downloadTemplate(HttpServletResponse response);
    
    /**
     * 批量导入学生
     */
    SchoolStudentImportResultVo importStudents(MultipartFile file, String userId);
    
    /**
     * 导出学生数据
     */
    void exportStudentData(SchoolStudentQuery query, String userId, HttpServletResponse response);
}
