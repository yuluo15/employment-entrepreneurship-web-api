package com.gxcj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxcj.entity.SchoolEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SchoolMapper extends BaseMapper<SchoolEntity> {

    IPage<SchoolEntity> list(Page<SchoolEntity> page, @Param("name") String name, @Param("status") Integer status);

    List<SchoolEntity> deleteList();
}
