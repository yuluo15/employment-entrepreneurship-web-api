package com.gxcj.service;

import com.gxcj.controller.admin.CompanyController;
import com.gxcj.entity.CompanyEntity;
import com.gxcj.entity.vo.job.CompanyDetailVo;
import com.gxcj.result.PageResult;

public interface CompanyService {
    PageResult<CompanyEntity> list(Integer pageNum, Integer pageSize, String name, String code, Integer status);

//    void create(CompanyController.CompanyCreateReq req);

    void auditCompany(CompanyController.AuditCompanyReq req);

    void update(CompanyController.CompanyCreateReq req);

    void delete(String id);

    CompanyDetailVo getCompanyDetail(String companyId);
}
