package com.gxcj.service;

import com.gxcj.controller.admin.DictController;
import com.gxcj.entity.DictDataEntity;
import com.gxcj.entity.DictTypeEntity;
import com.gxcj.result.PageResult;

public interface DictService {
    PageResult<DictTypeEntity> listType(Integer pageNum, Integer pageSize, String dictName);

    void createType(DictController.CreTypeReq req);

    void updateType(DictController.CreTypeReq req);

    void deleteType(String id);

    PageResult<DictDataEntity> listData(Integer pageNum, Integer pageSize, String dictType);

    void createDate(DictController.CreDataReq req);

    void updateData(DictController.CreDataReq req);

    void deleteData(String id);
}
