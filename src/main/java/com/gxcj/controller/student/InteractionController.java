package com.gxcj.controller.student;

import com.gxcj.result.Result;
import com.gxcj.service.InteractionService;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mobile/interaction")
public class InteractionController {

    @Autowired
    private InteractionService interactionService;

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
