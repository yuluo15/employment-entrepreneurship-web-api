package com.gxcj.controller.admin;

import cn.hutool.core.date.DateUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxcj.entity.TeacherEntity;
import com.gxcj.entity.query.TeacherQuery;
import com.gxcj.entity.vo.DictDataVo;
import com.gxcj.entity.vo.SchoolNameVo;
import com.gxcj.entity.vo.TeacherExportVo;
import com.gxcj.mapper.TeacherMapper;
import com.gxcj.result.PageResult;
import com.gxcj.result.Result;
import com.gxcj.service.SchoolService;
import com.gxcj.service.TeacherService;
import jakarta.servlet.http.HttpServletResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacherDbMgr")
public class TeacherDbMgrController {

    @Autowired
    private TeacherService teacherService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private TeacherMapper teacherMapper;

    @GetMapping("/schoolList")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<List<SchoolNameVo>> getSchoolList() {
        List<SchoolNameVo> list = schoolService.listSchoolName();
        return Result.success(list);
    }

    @GetMapping("/teacherList")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<PageResult<TeacherEntity>> getStudentList(@ParameterObject TeacherQuery teacherQuery) {
        PageResult<TeacherEntity> list = teacherService.list(teacherQuery);
        return Result.success(list);
    }

    @GetMapping("/getDictData")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<List<DictDataVo>> getDictData(@RequestParam("dictType") String dictType) {
        List<DictDataVo> dictDataVo = teacherService.getDictDataByDictType(dictType);
        return Result.success(dictDataVo);
    }

    @PostMapping("/export")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void export(@RequestParam(required = false) String schoolId, HttpServletResponse response) throws Exception {
        List<TeacherEntity> teacherList = teacherMapper.selectList(new LambdaQueryWrapper<TeacherEntity>()
                .eq(StringUtils.isNotEmpty(schoolId), TeacherEntity::getSchoolId, schoolId));
        List<SchoolNameVo> schoolNameVos = null;
        if(schoolId != null){
            schoolNameVos = schoolService.listSchoolName(schoolId);
        }else {
            schoolNameVos = schoolService.listSchoolName();
        }

        Map<String, String> map = schoolNameVos.stream().collect(Collectors.toMap(SchoolNameVo::getId, SchoolNameVo::getSchoolName, (x, y) -> x));

        List<TeacherExportVo> list = teacherList.stream().map(teacherEntity -> TeacherExportVo.builder()
                .employeeNo(teacherEntity.getEmployeeNo())
                .schoolName(map.get(teacherEntity.getSchoolId()))
                .name(teacherEntity.getName())
                .collegeName(teacherEntity.getCollegeName())
                .title(teacherEntity.getTitle())
                .expertise(teacherEntity.getExpertise())
                .guidanceCount(teacherEntity.getGuidanceCount())
                .phone(teacherEntity.getPhone())
                .email(teacherEntity.getEmail()).build()).toList();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("教师信息_" + DateUtil.formatDate(new Date()), StandardCharsets.UTF_8);
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), TeacherExportVo.class)
                .sheet("教师信息")
                .doWrite(list);
    }

    public void export2(@RequestParam String schoolId, HttpServletResponse response) throws Exception {

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("教师信息_" + DateUtil.formatDate(new Date()), StandardCharsets.UTF_8);
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        List<SchoolNameVo> schoolNameVos = null;
        if(schoolId != null){
            schoolNameVos = schoolService.listSchoolName(schoolId);
        }else {
            schoolNameVos = schoolService.listSchoolName();
        }
        Map<String, String> map = schoolNameVos.stream().collect(Collectors.toMap(SchoolNameVo::getId, SchoolNameVo::getSchoolName, (x, y) -> x));
        ExcelWriter excelWriter = null;
        try {
            excelWriter = EasyExcel.write(response.getOutputStream(), TeacherExportVo.TeacherExportVoBuilder.class).build();
            WriteSheet sheet = EasyExcel.write().sheet("教师信息").build();
            int pageNum = 1;
            int pageSize = 5000;
            while (true) {
                Page<TeacherEntity> page = teacherMapper.selectPage(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<TeacherEntity>()
                        .eq(StringUtils.isNotEmpty(schoolId), TeacherEntity::getSchoolId, schoolId));
                if (page.getRecords().isEmpty()) {
                    break;
                }
                List<TeacherExportVo.TeacherExportVoBuilder> list = page.getRecords().stream().map(teacherEntity -> TeacherExportVo.builder()
                        .employeeNo(teacherEntity.getEmployeeNo())
                        .schoolName(map.get(teacherEntity.getSchoolId()))
                        .name(teacherEntity.getName())
                        .collegeName(teacherEntity.getCollegeName())
                        .title(teacherEntity.getTitle())
                        .expertise(teacherEntity.getExpertise())
                        .guidanceCount(teacherEntity.getGuidanceCount())
                        .phone(teacherEntity.getPhone())
                        .email(teacherEntity.getEmail())).toList();
                excelWriter.write(list, sheet);

                pageNum++;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(excelWriter != null){
                excelWriter.finish();
            }
        }
    }

}
