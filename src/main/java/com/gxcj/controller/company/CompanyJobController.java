package com.gxcj.controller.company;

import com.gxcj.context.UserContext;
import com.gxcj.entity.JobEntity;
import com.gxcj.entity.query.CompanyJobQuery;
import com.gxcj.result.PageResult;
import com.gxcj.result.Result;
import com.gxcj.service.JobService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 企业端职位管理接口
 */
@RestController
@RequestMapping("/api/company/job")
@Validated
public class CompanyJobController {

    @Autowired
    private JobService jobService;

    /**
     * 1. 发布新职位
     */
    @PostMapping("/add")
    @PreAuthorize("hasRole('ROLE_COMPANY')")
    public Result<Map<String, String>> addJob(@RequestBody @Valid JobAddReq req) {
        // 构建JobEntity
        JobEntity jobEntity = new JobEntity();
        jobEntity.setJobName(req.getJobName());
        jobEntity.setSalaryRange(req.getSalaryRange());
        jobEntity.setCity(req.getCity());
        jobEntity.setEducation(req.getEducation());
        jobEntity.setExperience(req.getExperience());
        jobEntity.setTags(req.getTags());
        jobEntity.setDescription(req.getDescription());
        jobEntity.setRequirement(req.getRequirement());
        jobEntity.setContactPhone(req.getContactPhone());
        
        // 调用服务层发布职位
        String jobId = jobService.addJob(jobEntity, UserContext.getUserId());
        
        // 返回职位ID
        Map<String, String> data = new HashMap<>();
        data.put("jobId", jobId);
        
        return Result.success(data);
    }

    /**
     * 2. 更新职位
     */
    @PutMapping("/update")
    @PreAuthorize("hasRole('ROLE_COMPANY')")
    public Result<Void> updateJob(@RequestBody @Valid JobUpdateReq req) {
        // 构建JobEntity
        JobEntity jobEntity = new JobEntity();
        jobEntity.setId(req.getJobId());
        jobEntity.setJobName(req.getJobName());
        jobEntity.setSalaryRange(req.getSalaryRange());
        jobEntity.setCity(req.getCity());
        jobEntity.setEducation(req.getEducation());
        jobEntity.setExperience(req.getExperience());
        jobEntity.setTags(req.getTags());
        jobEntity.setDescription(req.getDescription());
        jobEntity.setRequirement(req.getRequirement());
        jobEntity.setContactPhone(req.getContactPhone());
        
        // 调用服务层更新职位
        jobService.updateJob(jobEntity, UserContext.getUserId());
        
        return Result.success();
    }

    /**
     * 3. 获取职位详情（用于编辑）
     */
    @GetMapping("/detail/{jobId}")
    @PreAuthorize("hasRole('ROLE_COMPANY')")
    public Result<JobEntity> getJobDetail(@PathVariable("jobId") String jobId) {
        JobEntity jobEntity = jobService.getJobForEdit(jobId, UserContext.getUserId());
        return Result.success(jobEntity);
    }

    /**
     * 4. 获取职位列表（分页）
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ROLE_COMPANY')")
    public Result<PageResult<JobEntity>> getJobList(CompanyJobQuery query) {
        PageResult<JobEntity> pageResult = jobService.getCompanyJobList(query, UserContext.getUserId());
        return Result.success(pageResult);
    }

    /**
     * 5. 删除职位
     */
    @DeleteMapping("/delete/{jobId}")
    @PreAuthorize("hasRole('ROLE_COMPANY')")
    public Result<Void> deleteJob(@PathVariable("jobId") String jobId) {
        jobService.deleteJob(jobId, UserContext.getUserId());
        return Result.success();
    }

    /**
     * 6. 下架职位
     */
    @PutMapping("/offline/{jobId}")
    @PreAuthorize("hasRole('ROLE_COMPANY')")
    public Result<Void> offlineJob(@PathVariable("jobId") String jobId) {
        jobService.offlineJob(jobId, UserContext.getUserId());
        return Result.success();
    }

    /**
     * 7. 上架职位
     */
    @PutMapping("/online/{jobId}")
    @PreAuthorize("hasRole('ROLE_COMPANY')")
    public Result<Void> onlineJob(@PathVariable("jobId") String jobId) {
        jobService.onlineJob(jobId, UserContext.getUserId());
        return Result.success();
    }

    // ==================== 请求对象 ====================

    @Data
    public static class JobAddReq {
        @NotBlank(message = "职位名称不能为空")
        @Size(min = 2, max = 100, message = "职位名称长度为2-100字符")
        private String jobName;

        @NotBlank(message = "薪资范围不能为空")
        private String salaryRange;

        @NotBlank(message = "工作城市不能为空")
        private String city;

        @NotBlank(message = "学历要求不能为空")
        private String education;

        @NotBlank(message = "工作经验不能为空")
        private String experience;

        private String tags;

        @NotBlank(message = "职位描述不能为空")
        @Size(min = 10, message = "职位描述至少10个字符")
        private String description;

        @NotBlank(message = "任职要求不能为空")
        @Size(min = 10, message = "任职要求至少10个字符")
        private String requirement;

        @NotBlank(message = "联系电话不能为空")
        @jakarta.validation.constraints.Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String contactPhone;
    }

    @Data
    public static class JobUpdateReq {
        @NotBlank(message = "职位ID不能为空")
        private String jobId;

        @NotBlank(message = "职位名称不能为空")
        @Size(min = 2, max = 100, message = "职位名称长度为2-100字符")
        private String jobName;

        @NotBlank(message = "薪资范围不能为空")
        private String salaryRange;

        @NotBlank(message = "工作城市不能为空")
        private String city;

        @NotBlank(message = "学历要求不能为空")
        private String education;

        @NotBlank(message = "工作经验不能为空")
        private String experience;

        private String tags;

        @NotBlank(message = "职位描述不能为空")
        @Size(min = 10, message = "职位描述至少10个字符")
        private String description;

        @NotBlank(message = "任职要求不能为空")
        @Size(min = 10, message = "任职要求至少10个字符")
        private String requirement;

        @NotBlank(message = "联系电话不能为空")
        @jakarta.validation.constraints.Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String contactPhone;
    }
}
