package com.gxcj.controller.admin;

import cn.hutool.core.date.DateUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.gxcj.entity.StudentEntity;
import com.gxcj.entity.query.StudentQuery;
import com.gxcj.entity.vo.DictDataVo;
import com.gxcj.entity.vo.SchoolNameVo;
import com.gxcj.entity.vo.StudentExportVo;
import com.gxcj.mapper.StudentMapper;
import com.gxcj.result.PageResult;
import com.gxcj.result.Result;
import com.gxcj.service.SchoolService;
import com.gxcj.service.StudentService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/studentDocMgr")
public class StudentDocMgrController {

    @Autowired
    private SchoolService schoolService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private StudentMapper studentMapper;

    @GetMapping("/schoolList")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<List<SchoolNameVo>> getSchoolList() {
        List<SchoolNameVo> list = schoolService.listSchoolName();
        return Result.success(list);
    }

    @GetMapping("/studentList")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<PageResult<StudentEntity>> getStudentList(@ParameterObject StudentQuery studentQuery) {
        PageResult<StudentEntity> list = studentService.list(studentQuery);
        return Result.success(list);
    }

    @GetMapping("/getDictData")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<List<DictDataVo>> getDictData(@RequestParam("dictType") String dictType) {
        List<DictDataVo> dictDataVo = studentService.getDictDataByDictType(dictType);
        return Result.success(dictDataVo);
    }

    @PostMapping("/export")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void export(@RequestParam(required = false) String schoolId, HttpServletResponse response) throws Exception {
        List<StudentEntity> studentEntityList = studentMapper.selectList(new LambdaQueryWrapper<StudentEntity>()
                .eq(StringUtils.isNotEmpty(schoolId), StudentEntity::getSchoolId, schoolId));
        List<SchoolNameVo> schoolNameList = null;
        if (schoolId == null) {
            schoolNameList = schoolService.listSchoolName();
        }else {
            schoolNameList = schoolService.listSchoolName(schoolId);
        }

        Map<String, String> schoolMap = schoolNameList.stream().collect(Collectors.toMap(SchoolNameVo::getId, SchoolNameVo::getSchoolName, (x, y) -> x));

        List<StudentExportVo> studentExportVoList = studentEntityList.stream().map(studentEntity -> StudentExportVo.builder()
                .studentNo(studentEntity.getStudentNo())
                .studentName(studentEntity.getStudentName())
                .schoolName(schoolMap.get(studentEntity.getSchoolId()))
                .collegeName(studentEntity.getCollegeName())
                .majorName(studentEntity.getMajorName())
                .className(studentEntity.getClassName())
                .education(studentEntity.getEducation())
                .graduationYear(studentEntity.getGraduationYear())
                .employmentStatus(studentEntity.getEmploymentStatus())
                .phone(studentEntity.getPhone())
                .build()).toList();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("学生信息_" + DateUtil.formatDate(new Date()), "UTF-8")
                .replaceAll("\\+", "%20");
//        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), StudentExportVo.class)
                        .sheet("学生档案信息")
                        .doWrite(studentExportVoList);
    }

}
