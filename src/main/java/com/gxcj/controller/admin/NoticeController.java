package com.gxcj.controller.admin;

import com.gxcj.entity.NoticeEntity;
import com.gxcj.entity.query.NoticeQuery;
import com.gxcj.result.PageResult;
import com.gxcj.result.Result;
import com.gxcj.service.NoticeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 公告管理接口
 */
@RestController
@RequestMapping("/api/notice")
@Validated
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    /**
     * 1. 获取公告列表（分页）
     */
    @GetMapping("/list")
    public Result<PageResult<NoticeEntity>> list(NoticeQuery query) {
        PageResult<NoticeEntity> pageResult = noticeService.list(query);
        return Result.success(pageResult);
    }

    /**
     * 2. 新增公告
     */
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    public Result<Void> add(@RequestBody @Valid NoticeAddReq req) {
        NoticeEntity notice = new NoticeEntity();
        notice.setNoticeTitle(req.getNoticeTitle());
        notice.setNoticeType(req.getNoticeType());
        notice.setNoticeContent(req.getNoticeContent());
//        notice.setAttachments(req.getAttachments());
        notice.setIsTop(req.getIsTop() != null ? req.getIsTop() : 0);
        notice.setStatus(req.getStatus() != null ? req.getStatus() : 0);
        
        noticeService.add(notice);
        return Result.success();
    }

    /**
     * 3. 更新公告
     */
    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    public Result<Void> update(@RequestBody @Valid NoticeUpdateReq req) {
        NoticeEntity notice = new NoticeEntity();
        notice.setNoticeId(req.getNoticeId());
        notice.setNoticeTitle(req.getNoticeTitle());
        notice.setNoticeType(req.getNoticeType());
        notice.setNoticeContent(req.getNoticeContent());
//        notice.setAttachments(req.getAttachments());
        notice.setIsTop(req.getIsTop());
        notice.setStatus(req.getStatus());
        
        noticeService.update(notice);
        return Result.success();
    }

    /**
     * 4. 删除公告
     */
    @DeleteMapping("/delete/{noticeId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    public Result<Void> delete(@PathVariable("noticeId") String noticeId) {
        noticeService.delete(noticeId);
        return Result.success();
    }

    /**
     * 5. 发布公告
     */
    @PutMapping("/publish/{noticeId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    public Result<Void> publish(@PathVariable("noticeId") String noticeId) {
        noticeService.publish(noticeId);
        return Result.success();
    }

    /**
     * 6. 停用公告
     */
    @PutMapping("/unpublish/{noticeId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    public Result<Void> unpublish(@PathVariable("noticeId") String noticeId) {
        noticeService.unpublish(noticeId);
        return Result.success();
    }

    /**
     * 7. 置顶/取消置顶
     */
    @PutMapping("/top/{noticeId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    public Result<Void> setTop(@PathVariable("noticeId") String noticeId, 
                                @RequestBody @Valid TopReq req) {
        noticeService.setTop(noticeId, req.getIsTop());
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

        private String attachments;
        private Integer isTop;
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

        private String attachments;
        private Integer isTop;
        private Integer status;
    }

    @Data
    public static class TopReq {
        @NotNull(message = "置顶状态不能为空")
        private Integer isTop;
    }
}
