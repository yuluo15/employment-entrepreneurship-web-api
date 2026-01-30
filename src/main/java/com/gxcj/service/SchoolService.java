package com.gxcj.service;

import com.gxcj.controller.admin.SchoolMgrController;
import com.gxcj.entity.SchoolEntity;
import com.gxcj.entity.vo.SchoolNameVo;
import com.gxcj.result.PageResult;
import com.gxcj.result.Result;

import java.util.List;

public interface SchoolService {
    void createSchool(SchoolMgrController.SchoolCreateReq rep);

    PageResult<SchoolEntity> list(Integer pageNum, Integer pageSize, String name, Integer status);

    void updateSchool(SchoolMgrController.SchoolUpdateReq req);

    void resetPassword(SchoolMgrController.IdReq req);

    void deleteSchool(String id);

    void updateStatus(SchoolMgrController.StatusUpdateReq req);

    PageResult<SchoolEntity> deleteList();

    void restoreSchool(String id);

    List<SchoolNameVo> listSchoolName();

    List<SchoolNameVo> listSchoolName(String schoolId);

}
