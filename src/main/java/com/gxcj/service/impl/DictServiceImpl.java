package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxcj.controller.admin.DictController;
import com.gxcj.entity.DictDataEntity;
import com.gxcj.entity.DictTypeEntity;
import com.gxcj.mapper.DictDataMapper;
import com.gxcj.mapper.DictTypeMapper;
import com.gxcj.result.PageResult;
import com.gxcj.service.DictService;
import com.gxcj.stutas.DeleteStatusEnum;
import com.gxcj.utils.EntityHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DictServiceImpl implements DictService {
    @Autowired
    private DictTypeMapper dictTypeMapper;
    @Autowired
    private DictDataMapper dictDataMapper;

    public PageResult<DictTypeEntity> listType(Integer pageNum, Integer pageSize, String dictName) {
//        PageHelper.startPage(pageNum, pageSize);
        Page<DictTypeEntity> page = dictTypeMapper.selectPage(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<DictTypeEntity>()
                .eq(StringUtils.isNotEmpty(dictName), DictTypeEntity::getDictName, dictName)
                .eq(DictTypeEntity::getIsDeleted, DeleteStatusEnum.NOT_DELETE.getCode()));
//        PageInfo<DictTypeEntity> pageInfo = new PageInfo<>(list);
//        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
        return new PageResult<>(page.getTotal(), page.getRecords());
    }


    public void createType(DictController.CreTypeReq req) {
        DictTypeEntity dictTypeEntity = DictTypeEntity.ofReq(req);
        dictTypeEntity.setId(EntityHelper.uuid());
        dictTypeMapper.insert(dictTypeEntity);
    }


    public void updateType(DictController.CreTypeReq req) {
        DictTypeEntity dictTypeEntity = DictTypeEntity.ofReq(req);
        dictTypeMapper.insertOrUpdate(dictTypeEntity);
    }


    public void deleteType(String id) {
        dictTypeMapper.update(new LambdaUpdateWrapper<DictTypeEntity>()
                .set(DictTypeEntity::getIsDeleted, DeleteStatusEnum.DELETE.getCode())
                .eq(DictTypeEntity::getId, id));
    }


    public PageResult<DictDataEntity> listData(Integer pageNum, Integer pageSize, String dictType) {
//        PageHelper.startPage(pageNum, pageSize);
        Page<DictDataEntity> page = dictDataMapper.selectPage(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<DictDataEntity>()
                .eq(StringUtils.isNotEmpty(dictType), DictDataEntity::getDictType, dictType)
                .eq(DictDataEntity::getIsDeleted, DeleteStatusEnum.NOT_DELETE.getCode()));
//        PageInfo<DictDataEntity> pageInfo = new PageInfo<>(list);
//        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    public void createDate(DictController.CreDataReq req) {
        DictDataEntity dictDataEntity = DictDataEntity.ofReq(req);
        dictDataEntity.setId(EntityHelper.uuid());
        dictDataMapper.insert(dictDataEntity);
    }

    public void updateData(DictController.CreDataReq req) {
        DictDataEntity dictDataEntity = DictDataEntity.ofReq(req);
        dictDataMapper.updateById(dictDataEntity);
    }

    public void deleteData(String id) {
        dictDataMapper.update(new LambdaUpdateWrapper<DictDataEntity>()
                .set(DictDataEntity::getIsDeleted, DeleteStatusEnum.DELETE.getCode())
                .eq(DictDataEntity::getId, id));
    }
}
