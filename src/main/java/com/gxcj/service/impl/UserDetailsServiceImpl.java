package com.gxcj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxcj.entity.RoleEntity;
import com.gxcj.entity.UserEntity;
import com.gxcj.entity.dto.LoginUser;
import com.gxcj.exception.BusinessException;
import com.gxcj.mapper.RoleMapper;
import com.gxcj.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public UserDetails loadUserByUsername(String inputAccount) throws UsernameNotFoundException {
        UserEntity userEntity = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getLoginIdentity, inputAccount)
        );

        if (userEntity == null) {
            throw new UsernameNotFoundException("账号不存在: " + inputAccount);
        }

        if (userEntity.getStatus() != 1) {
            throw new BusinessException("账号已被冻结或正在审核中");
        }

        String roleKey = userEntity.getRoleKey();
        RoleEntity role = roleMapper.selectById(roleKey);
        String realRoleName = (role != null) ? role.getRoleName() : "ROLE_USER"; // 防空
        List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(realRoleName);

        return new LoginUser(userEntity, authorities);
    }
}