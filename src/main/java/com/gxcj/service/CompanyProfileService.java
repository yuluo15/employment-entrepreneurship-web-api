package com.gxcj.service;

import com.gxcj.controller.company.CompanyProfileController;
import com.gxcj.entity.CompanyEntity;

public interface CompanyProfileService {
    
    /**
     * 获取企业信息
     */
    CompanyEntity getCompanyInfo(String userId);
    
    /**
     * 更新企业信息
     */
    void updateCompanyInfo(CompanyProfileController.CompanyUpdateReq req, String userId);
}
