package com.gxcj.controller.school;

import com.gxcj.context.UserContext;
import com.gxcj.entity.NoticeEntity;
import com.gxcj.entity.query.NoticeQuery;
import com.gxcj.result.PageResult;
import com.gxcj.result.Result;
import com.gxcj.service.SchoolNoticeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 学校端通知公告接口
 */
@Slf4j
@RestController
@RequestMapping("/api/school/notice")
@Validated
public class SchoolNoticeController {

    @Autowired
    private SchoolNoticeService schoolNoticeService;

    /**
     * 1. 获取学校通知列表（分页）
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ROLE_SCHOOL', 'ROLE_SCHOOL_ADMIN', 'ROLE_TEACHER')")
    public Result<PageResult<NoticeEntity>> list(NoticeQuery query) {
        log.info("获取学校通知列表: query={}", query);
        
        String userId = UserContext.getUserId();
        
        PageResult<NoticeEntity> pageResult = schoolNoticeService.list(query, userId);
        return Result.success(pageResult);
    }

    /**
     * 2. 新增学校通知
     */
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('ROLE_SCHOOL', 'ROLE_SCHOOL_ADMIN')")
    public Result<Void> add(@RequestBody @Valid NoticeAddReq req) {
        log.info("新增学校通知: req={}", req);
        
        String userId = UserContext.getUserId();
        
        NoticeEntity notice = new NoticeEntity();
        notice.setNoticeTitle(req.getNoticeTitle());
        notice.setNoticeType(req.getNoticeType());
        notice.setNoticeContent(req.getNoticeContent());
        notice.setTargetAudience(req.getTargetAudience());
        notice.setStatus(req.getStatus() != null ? req.getStatus() : 0);
        
        schoolNoticeService.add(notice, userId);
        return Result.success();
    }

    /**
     * 3. 更新学校通知
     */
    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('ROLE_SCHOOL', 'ROLE_SCHOOL_ADMIN')")
    public Result<Void> update(@RequestBody @Valid NoticeUpdateReq req) {
        log.info("更新学校通知: req={}", req);
        
        String userId = UserContext.getUserId();
        
        NoticeEntity notice = new NoticeEntity();
        notice.setNoticeId(req.getNoticeId());
        notice.setNoticeTitle(req.getNoticeTitle());
        notice.setNoticeType(req.getNoticeType());
        notice.setNoticeContent(req.getNoticeContent());
        notice.setTargetAudience(req.getTargetAudience());
        notice.setStatus(req.getStatus());
        
        schoolNoticeService.update(notice, userId);
        return Result.success();
    }

    /**
     * 4. 删除学校通知
     */
    @DeleteMapping("/delete/{noticeId}")
    @PreAuthorize("hasAnyRole('ROLE_SCHOOL', 'ROLE_SCHOOL_ADMIN')")
    public Result<Void> delete(@PathVariable("noticeId") String noticeId) {
        log.info("删除学校通知: noticeId={}", noticeId);
        
        String userId = UserContext.getUserId();
        
        schoolNoticeService.delete(noticeId, userId);
        return Result.success();
    }

    /**
     * 5. 发布学校通知
     */
    @PutMapping("/publish/{noticeId}")
    @PreAuthorize("hasAnyRole('ROLE_SCHOOL', 'ROLE_SCHOOL_ADMIN')")
    public Result<Void> publish(@PathVariable("noticeId") String noticeId) {
        log.info("发布学校通知: noticeId={}", noticeId);
        
        String userId = UserContext.getUserId();
        
        schoolNoticeService.publish(noticeId, userId);
        return Result.success();
    }

    /**
     * 6. 停用学校通知
     */
    @PutMapping("/unpublish/{noticeId}")
    @PreAuthorize("hasAnyRole('ROLE_SCHOOL', 'ROLE_SCHOOL_ADMIN')")
    public Result<Void> unpublish(@PathVariable("noticeId") String noticeId) {
        log.info("停用学校通知: noticeId={}", noticeId);
        
        String userId = UserContext.getUserId();
        
        schoolNoticeService.unpublish(noticeId, userId);
        return Result.success();
    }

    // ==================== 请求对象 ====================

    @Data
    public static class NoticeAddReq {
        @NotBlank(message = "标题不能为空")
        private String noticeTitle;

        @NotBlank(message = "类型不能为空")
        private String noticeType;

        @NotBlank(message = "内容不能为空")
        private String noticeContent;

        @NotBlank(message = "目标受众不能为空")
        private String targetAudience;

        private Integer status;
    }

    @Data
    public static class NoticeUpdateReq {
        @NotBlank(message = "公告ID不能为空")
        private String noticeId;

        @NotBlank(message = "标题不能为空")
        private String noticeTitle;

        @NotBlank(message = "类型不能为空")
        private String noticeType;

        @NotBlank(message = "内容不能为空")
        private String noticeContent;

        @NotBlank(message = "目标受众不能为空")
        private String targetAudience;

        private Integer status;
    }
}
