package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxcj.constant.SysConstant;
import com.gxcj.context.UserContext;
import com.gxcj.controller.admin.SchoolMgrController;
import com.gxcj.entity.SchoolEntity;
import com.gxcj.entity.UserEntity;
import com.gxcj.entity.vo.SchoolNameVo;
import com.gxcj.mapper.SchoolMapper;
import com.gxcj.mapper.UserMapper;
import com.gxcj.result.PageResult;
import com.gxcj.service.SchoolService;
import com.gxcj.stutas.DeleteStatusEnum;
import com.gxcj.stutas.StatusEnum;
import com.gxcj.utils.EntityHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SchoolServiceImpl implements SchoolService {
    @Autowired
    private SchoolMapper schoolMapper;
    @Autowired
    private UserMapper userMapper;

    @Transactional
    public void createSchool(SchoolMgrController.SchoolCreateReq req) {
        String schoolId = EntityHelper.uuid();
        //创建学校默认账号
        UserEntity userEntity = new UserEntity();
        userEntity.setId(EntityHelper.uuid());
        userEntity.setRoleKey(SysConstant.ROLE_SCHOOL);
        userEntity.setLoginIdentity(req.getEmail());
        userEntity.setPassword(EntityHelper.encodedPassword(SysConstant.DEFAULT_PASSWORD));
        userEntity.setPhone(req.getContactPhone());
        userEntity.setEmail(req.getEmail());
        userEntity.setOwnerId(schoolId);
        userEntity.setCreateTime(EntityHelper.now());
        userMapper.insert(userEntity);

        SchoolEntity schoolEntity = SchoolEntity.ofRep(req);
        schoolEntity.setId(EntityHelper.uuid());
        schoolEntity.setDefaultAccountId(userEntity.getId());
        schoolEntity.setUpdateTime(EntityHelper.now());
        schoolEntity.setCreateTime(EntityHelper.now());
        schoolEntity.setCreateBy(UserContext.getUserId());
        schoolMapper.insert(schoolEntity);

    }


//    public PageResult<SchoolEntity> list(Integer pageNum, Integer pageSize, String name, Integer status) {
//        PageHelper.startPage(pageNum, pageSize);
//        List<SchoolEntity> schoolEntities = schoolMapper.selectList(new LambdaQueryWrapper<SchoolEntity>()
//                .like(!StringUtils.isEmpty(name), SchoolEntity::getName, name)
//                .eq(status != null, SchoolEntity::getStatus, status));
//        PageInfo<SchoolEntity> pageInfo = new PageInfo<>(schoolEntities);
//        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
//    }

    public PageResult<SchoolEntity> list(Integer pageNum, Integer pageSize, String name, Integer status) {
//        PageHelper.startPage(pageNum, pageSize);
        Page<SchoolEntity> page = new Page<>(pageNum, pageSize);
        IPage<SchoolEntity> list = schoolMapper.list(page, name, status);
//        PageInfo<SchoolEntity> pageInfo = new PageInfo<>(list);
//        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
        return new PageResult<>(list.getTotal(), list.getRecords());
    }


    public void updateSchool(SchoolMgrController.SchoolUpdateReq req) {
        SchoolEntity schoolEntity = SchoolEntity.ofRep(req);
        schoolEntity.setUpdateTime(EntityHelper.now());
        schoolMapper.updateById(schoolEntity);
    }

    public void resetPassword(SchoolMgrController.IdReq req) {
        //加密新密码
        String password = req.getNewPassword();
        String encodedPassword = EntityHelper.encodedPassword(password);

        SchoolEntity schoolEntity = schoolMapper.selectById(req.getId());
        String defaultAccountId = schoolEntity.getDefaultAccountId();
        UserEntity userEntity = userMapper.selectById(defaultAccountId);
        userEntity.setPassword(encodedPassword);
        userMapper.updateById(userEntity);
    }

    public void deleteSchool(String id) {
        schoolMapper.update(new LambdaUpdateWrapper<SchoolEntity>()
                .set(SchoolEntity::getStatus, StatusEnum.DISABLED.getCode())
                .set(SchoolEntity::getIsDeleted, DeleteStatusEnum.DELETE.getCode())
                .eq(SchoolEntity::getId, id));
    }

    public void updateStatus(SchoolMgrController.StatusUpdateReq req) {
        LambdaUpdateWrapper<SchoolEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SchoolEntity::getId, req.getId())
                .set(SchoolEntity::getStatus, req.getStatus())
                .set(SchoolEntity::getUpdateTime, EntityHelper.now());

        schoolMapper.update(null, updateWrapper);
    }

    @Override
    public PageResult<SchoolEntity> deleteList() {
        List<SchoolEntity> list = schoolMapper.deleteList();
//        PageInfo<SchoolEntity> pageInfo = new PageInfo<>(list);
//        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
        return new PageResult<>((long) list.size(), list);
    }

    @Override
    public void restoreSchool(String id) {
        schoolMapper.update(new LambdaUpdateWrapper<SchoolEntity>()
                        .set(SchoolEntity::getStatus, StatusEnum.NORMAL.getCode())
                        .set(SchoolEntity::getIsDeleted, DeleteStatusEnum.NOT_DELETE.getCode())
                        .eq(SchoolEntity::getId, id));
    }

    @Override
    public List<SchoolNameVo> listSchoolName() {
        List<SchoolEntity> schoolEntityList = schoolMapper.selectList(new LambdaQueryWrapper<SchoolEntity>()
                .eq(SchoolEntity::getStatus, StatusEnum.NORMAL.getCode())
                .eq(SchoolEntity::getIsDeleted, DeleteStatusEnum.NOT_DELETE.getCode()));
        List<SchoolNameVo> list = schoolEntityList.stream().map(schoolEntity -> {
            SchoolNameVo schoolNameVo = new SchoolNameVo();
            schoolNameVo.setId(schoolEntity.getId());
            schoolNameVo.setSchoolName(schoolEntity.getName());
            return schoolNameVo;
        }).toList();
        return list;
    }

    public List<SchoolNameVo> listSchoolName(String schoolId) {
        SchoolEntity schoolEntity = schoolMapper.selectById(schoolId);

        SchoolNameVo schoolNameVo = new SchoolNameVo();
        schoolNameVo.setId(schoolEntity.getId());
        schoolNameVo.setSchoolName(schoolEntity.getName());
        return List.of(schoolNameVo);
    }
}
