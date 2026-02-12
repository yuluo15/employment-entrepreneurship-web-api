package com.gxcj.controller.student;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxcj.context.UserContext;
import com.gxcj.entity.*;
import com.gxcj.entity.vo.job.SearchResultVo;
import com.gxcj.entity.vo.job.StudentDeliveryVo;
import com.gxcj.mapper.*;
import com.gxcj.result.PageResult;
import com.gxcj.result.Result;
import com.gxcj.service.InteractionService;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.apache.catalina.security.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mobile/interaction")
public class InteractionController {

    @Autowired
    private InteractionService interactionService;
    @Autowired
    private CollectionMapper collectionMapper;
    @Autowired
    private JobMapper jobMapper;
    @Autowired
    private JobDeliveryMapper jobDeliveryMapper;
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private CompanyMapper companyMapper;


    @PostMapping("/collection/toggle")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<Boolean> toggleCollection(@RequestBody CollectionReq req){
        Boolean isCollection = interactionService.toggleCollection(req);
        return Result.success(isCollection);
    }

    @PostMapping("/apply/job")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<Boolean> applyJob(@RequestBody ApplyJobReq req){
        Boolean isApply = interactionService.applyJob(req);
        return Result.success(isApply);
    }

    @GetMapping("/collection/list")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<PageResult<SearchResultVo>> getMyCollectionList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String type) {

        String userId = UserContext.getUserId();

        Page<CollectionEntity> page = collectionMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<CollectionEntity>()
                        .eq(CollectionEntity::getUserId, userId)
                        .eq(StringUtils.isNotEmpty(type), CollectionEntity::getType, type)
                        .orderByDesc(CollectionEntity::getCreateTime)
        );

        List<SearchResultVo> list = page.getRecords().stream().map(item -> {
            SearchResultVo vo = new SearchResultVo();
            vo.setId(item.getTargetId());
            vo.setType(item.getType());
            vo.setTitle(item.getTitle());
            vo.setAvatar(item.getImage());
            vo.setSubTitle(item.getSubTitle());
            vo.setTags(Collections.singletonList("收藏于 " + item.getCreateTime().toLocalDateTime().toString()));
            return vo;
        }).collect(Collectors.toList());

        return Result.success(new PageResult<>(page.getTotal(), list));
    }

    @GetMapping("/delivery/list")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Result<PageResult<StudentDeliveryVo>> getMyDeliveryList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {

        StudentEntity studentEntity = studentMapper.selectOne(new LambdaQueryWrapper<StudentEntity>()
                .eq(StudentEntity::getUserId, UserContext.getUserId()));

        Page<JobDeliveryEntity> page = jobDeliveryMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<JobDeliveryEntity>()
                        .eq(JobDeliveryEntity::getStudentId, studentEntity.getStudentId())
                        .eq(StringUtils.isNotEmpty(status), JobDeliveryEntity::getStatus, status)
                        .orderByDesc(JobDeliveryEntity::getCreateTime)
        );

        if (page.getRecords().isEmpty()) {
            return Result.success(new PageResult<>(0L, new ArrayList<>()));
        }

        Set<String> jobIds = page.getRecords().stream().map(JobDeliveryEntity::getJobId).collect(Collectors.toSet());
        Set<String> compIds = page.getRecords().stream().map(JobDeliveryEntity::getCompanyId).collect(Collectors.toSet());

        Map<String, JobEntity> jobMap;
        if (!jobIds.isEmpty()) {
            jobMap = jobMapper.selectList(
                    new LambdaQueryWrapper<JobEntity>()
                            .in(JobEntity::getId, jobIds))
                    .stream()
                    .collect(Collectors.toMap(JobEntity::getId, Function.identity()));
        } else {
            jobMap = new HashMap<>();
        }

        Map<String, CompanyEntity> compMap;
        if (!compIds.isEmpty()) {
            compMap = companyMapper.selectList(
                    new LambdaQueryWrapper<CompanyEntity>()
                            .in(CompanyEntity::getId, compIds))
                    .stream()
                    .collect(Collectors.toMap(CompanyEntity::getId, Function.identity()));
        } else {
            compMap = new HashMap<>();
        }

        List<StudentDeliveryVo> list = page.getRecords().stream().map(d -> {
            StudentDeliveryVo vo = new StudentDeliveryVo();
            vo.setId(d.getId());
            vo.setJobId(d.getJobId());
            vo.setStatus(d.getStatus());
            vo.setCreateTime(d.getCreateTime().toString());

            vo.setStatusText(convertStatus(d.getStatus()));

            JobEntity job = jobMap.get(d.getJobId());
            if (job != null) {
                vo.setJobName(job.getJobName());
                vo.setSalary(job.getSalaryRange());
            }

            CompanyEntity comp = compMap.get(d.getCompanyId());
            if (comp != null) {
                vo.setCompanyId(comp.getId());
                vo.setCompanyName(comp.getName());
                vo.setCompanyLogo(comp.getLogo());
            }
            return vo;
        }).collect(Collectors.toList());

        return Result.success(new PageResult<>(page.getTotal(), list));
    }

    private String convertStatus(String status) {
        switch (status) {
            case "DELIVERED": return "已投递";
            case "VIEWED": return "被查看";
            case "INTERVIEW": return "面试中";
            case "OFFER": return "录用";
            case "REJECT": return "不合适";
            default: return "参数错误";
        }
    }

    @Data
    public static class ApplyJobReq{
        @NotBlank(message = "职位id不能为空")
        private String JobId;
    }

    @Data
    public static class CollectionReq{
        @NotBlank(message = "目标id不能为空")
        private String targetId;
        @NotBlank(message = "类型不能为空")
        private String type;
    }

}
