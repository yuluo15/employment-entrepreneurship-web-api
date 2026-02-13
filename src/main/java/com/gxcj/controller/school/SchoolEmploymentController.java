package com.gxcj.controller.school;

import com.gxcj.context.UserContext;
import com.gxcj.entity.dto.school.SchoolEmploymentUpdateDto;
import com.gxcj.entity.query.school.SchoolEmploymentQuery;
import com.gxcj.entity.vo.school.SchoolEmploymentStatsVo;
import com.gxcj.entity.vo.school.SchoolEmploymentVo;
import com.gxcj.result.PageResult;
import com.gxcj.result.Result;
import com.gxcj.service.SchoolEmploymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/school/employment")
@PreAuthorize("hasAnyRole('ROLE_SCHOOL', 'ROLE_SCHOOL_ADMIN', 'ROLE_TEACHER')")
public class SchoolEmploymentController {

    @Autowired
    private SchoolEmploymentService employmentService;

    /**
     * 获取就业列表
     */
    @GetMapping("/list")
    public Result<PageResult<SchoolEmploymentVo>> getEmploymentList(SchoolEmploymentQuery query) {
        String userId = UserContext.getUserId();
        PageResult<SchoolEmploymentVo> result = employmentService.getEmploymentList(query, userId);
        return Result.success(result);
    }

    /**
     * 获取就业详情
     */
    @GetMapping("/detail/{studentId}")
    public Result<SchoolEmploymentVo> getEmploymentDetail(@PathVariable String studentId) {
        String userId = UserContext.getUserId();
        SchoolEmploymentVo employment = employmentService.getEmploymentDetail(studentId, userId);
        return Result.success(employment);
    }

    /**
     * 更新就业信息
     */
    @PutMapping("/update")
    public Result<Void> updateEmploymentInfo(@RequestBody SchoolEmploymentUpdateDto dto) {
        String userId = UserContext.getUserId();
        employmentService.updateEmploymentInfo(dto, userId);
        return Result.success();
    }

    /**
     * 获取就业统计
     */
    @GetMapping("/stats")
    public Result<SchoolEmploymentStatsVo> getEmploymentStats(SchoolEmploymentQuery query) {
        String userId = UserContext.getUserId();
        SchoolEmploymentStatsVo stats = employmentService.getEmploymentStats(query, userId);
        return Result.success(stats);
    }
}
