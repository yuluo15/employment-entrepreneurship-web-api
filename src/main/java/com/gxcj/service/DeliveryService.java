package com.gxcj.service;

import com.gxcj.controller.company.CompanyDeliveryController;
import com.gxcj.entity.StudentResumeEntity;
import com.gxcj.entity.query.DeliveryQuery;
import com.gxcj.entity.query.InterviewQuery;
import com.gxcj.entity.query.TalentQuery;
import com.gxcj.entity.vo.DeliveryVo;
import com.gxcj.entity.vo.InterviewVo;
import com.gxcj.result.PageResult;

import java.util.Map;

public interface DeliveryService {
    
    /**
     * 获取投递简历列表（分页）
     */
    PageResult<DeliveryVo> getDeliveryList(DeliveryQuery query, String userId);
    
    /**
     * 获取简历详情
     */
    StudentResumeEntity getResumeDetail(String resumeId, String userId);
    
    /**
     * 安排面试
     */
    String arrangeInterview(CompanyDeliveryController.InterviewArrangeReq req, String userId);
    
    /**
     * 拒绝简历
     */
    void rejectDelivery(String deliveryId, String reason, String userId);
    
    /**
     * 获取面试列表（分页）
     */
    PageResult<InterviewVo> getInterviewList(InterviewQuery query, String userId);
    
    /**
     * 完成面试
     */
    void completeInterview(CompanyDeliveryController.CompleteInterviewReq req, String userId);
    
    /**
     * 取消面试
     */
    void cancelInterview(String interviewId, String reason, String userId);
    
    /**
     * 评价面试
     */
    void evaluateInterview(CompanyDeliveryController.EvaluateInterviewReq req, String userId);
    
    /**
     * 获取人才库列表（分页）
     */
    PageResult<DeliveryVo> getTalentList(TalentQuery query, String userId);
    
    /**
     * 获取人才库统计
     */
    Map<String, Long> getTalentStatistics(String userId);
    
    /**
     * 发放Offer
     */
    void sendOffer(CompanyDeliveryController.SendOfferReq req, String userId);
}
