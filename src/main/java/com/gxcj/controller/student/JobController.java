package com.gxcj.controller.student;

import com.gxcj.entity.vo.job.JobDetailVo;
import com.gxcj.entity.vo.job.SearchResultVo;
import com.gxcj.result.PageResult;
import com.gxcj.result.Result;
import com.gxcj.service.JobService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mobile/job")
public class JobController {

    @Autowired
    private JobService jobService;

    @GetMapping("/search")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<PageResult<SearchResultVo>> search(@Valid SearchReq req) {
        PageResult<SearchResultVo> pageResult = jobService.search(req);
        if (pageResult == null) {
            return Result.fail("错误类型");
        }
        return Result.success(pageResult);
    }

    @GetMapping("detail/{id}")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<JobDetailVo> getJobDetail(@PathVariable("id") String jobId) {
        JobDetailVo jobDetailVo = jobService.getJobDetail(jobId);
        return Result.success(jobDetailVo);
    }


    @Data
    public static class SearchReq {
        private Integer pageNum = 1;
        private Integer pageSize = 10;
        private String keyword;
        @NotBlank
        private String type;
    }

}
