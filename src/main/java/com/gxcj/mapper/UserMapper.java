package com.gxcj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gxcj.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
    UserEntity selectByLoginName(String loginName);
}
