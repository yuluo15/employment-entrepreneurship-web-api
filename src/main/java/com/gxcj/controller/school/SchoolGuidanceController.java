package com.gxcj.controller.school;

import com.gxcj.context.UserContext;
import com.gxcj.entity.query.school.SchoolGuidanceQuery;
import com.gxcj.entity.vo.school.SchoolGuidanceDetailVo;
import com.gxcj.entity.vo.school.SchoolGuidanceVo;
import com.gxcj.result.PageResult;
import com.gxcj.result.Result;
import com.gxcj.service.SchoolGuidanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/school/guidance")
@PreAuthorize("hasAnyRole('ROLE_SCHOOL', 'ROLE_SCHOOL_ADMIN', 'ROLE_TEACHER')")
public class SchoolGuidanceController {

    @Autowired
    private SchoolGuidanceService guidanceService;

    /**
     * 获取指导记录列表
     */
    @GetMapping("/list")
    public Result<PageResult<SchoolGuidanceVo>> getGuidanceList(SchoolGuidanceQuery query) {
        String userId = UserContext.getUserId();
        PageResult<SchoolGuidanceVo> result = guidanceService.getGuidanceList(query, userId);
        return Result.success(result);
    }

    /**
     * 获取指导记录详情
     */
    @GetMapping("/detail/{id}")
    public Result<SchoolGuidanceDetailVo> getGuidanceDetail(@PathVariable String id) {
        String userId = UserContext.getUserId();
        SchoolGuidanceDetailVo detail = guidanceService.getGuidanceDetail(id, userId);
        return Result.success(detail);
    }
}
