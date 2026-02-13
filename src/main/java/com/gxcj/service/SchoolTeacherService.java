package com.gxcj.service;

import com.gxcj.entity.query.school.SchoolTeacherQuery;
import com.gxcj.entity.vo.school.SchoolTeacherImportResultVo;
import com.gxcj.entity.vo.school.SchoolTeacherVo;
import com.gxcj.result.PageResult;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

public interface SchoolTeacherService {
    
    /**
     * 获取教师列表
     */
    PageResult<SchoolTeacherVo> getTeacherList(SchoolTeacherQuery query, String userId);
    
    /**
     * 获取教师详情
     */
    SchoolTeacherVo getTeacherDetail(String teacherId, String userId);
    
    /**
     * 下载导入模板
     */
    void downloadTemplate(HttpServletResponse response);
    
    /**
     * 批量导入教师
     */
    SchoolTeacherImportResultVo importTeachers(MultipartFile file, String userId);
}
