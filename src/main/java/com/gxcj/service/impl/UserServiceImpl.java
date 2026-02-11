package com.gxcj.service.impl;

import com.alibaba.fastjson2.JSON;
import com.gxcj.constant.SysConstant;
import com.gxcj.controller.AuthController;
import com.gxcj.entity.UserEntity;
import com.gxcj.entity.dto.LoginUser;
import com.gxcj.entity.vo.UserVo;
import com.gxcj.exception.BusinessException;
import com.gxcj.mapper.UserMapper;
import com.gxcj.service.UserService;
import com.gxcj.utils.EntityHelper;
import com.gxcj.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UserMapper userMapper;

    public UserVo login(AuthController.UserReq userReq) {

        String s = EntityHelper.encodedPassword("12345678");
        // 1. 使用 AuthenticationManager 进行认证
        // 该方法会自动调用 UserDetailsServiceImpl.loadUserByUsername
        // 并且会自动比对密码（它知道如何处理 BCrypt 加密）
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userReq.getLoginName(), userReq.getPassword());

        Authentication authenticate = authenticationManager.authenticate(authenticationToken);

        // If code reaches here, login is successful. If failed, it throws an exception automatically.
        if (authenticate == null) {
            throw new BusinessException("登录失败");
        }

        // 2. 认证通过，拿到完整的用户信息 (我们在 UserDetailsServiceImpl 里返回的那个 LoginUser)
        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
        String userId = loginUser.getUserEntity().getId().toString();

        // 3. 生成 JWT Token
        String jwt = JwtUtil.createJWT(userId);

        // 4. 【核心差异点】要把完整的 loginUser 存入 Redis，而不是存 Token
        // 这里的 Key 必须和 Filter 里查的一样： "login_token:" + userId
        String redisKey = SysConstant.REDIS_TOKEN_HAND + userId;

        // 将 LoginUser 对象序列化为 JSON
        String jsonUser = JSON.toJSONString(loginUser);

        // 存入 Redis，设置过期时间（例如 24 小时）
        stringRedisTemplate.opsForValue().set(redisKey, jsonUser, 24, TimeUnit.HOURS);

        // 5. 封装返回给前端的 VO
        UserVo userVo = new UserVo();
        userVo.setUserId(userId);
        userVo.setNickname(loginUser.getUserEntity().getNickname());

        // 获取角色名称 (我们在 UserDetailsServiceImpl 里放入的 authorities)
        // 假设只有一个角色，取第一个即可
        if(!loginUser.getAuthorities().isEmpty()){
            userVo.setRole(loginUser.getAuthorities().iterator().next().getAuthority());
        }

        userVo.setToken(jwt);

        return userVo;
    }

    @Override
    public void changePassword(AuthController.PasswordReq passwordReq) {
        UserEntity userEntity = userMapper.selectById(passwordReq.getUserId());
        if(userEntity == null){
            throw new BusinessException("账号不存在");
        }
        if (!EntityHelper.matchesPassword(passwordReq.getOldPassword(), userEntity.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        if (passwordReq.getOldPassword().equals(passwordReq.getNewPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }

        userEntity.setPassword(EntityHelper.encodedPassword(passwordReq.getNewPassword()));
        userMapper.updateById(userEntity);
    }

    @Override
    public UserEntity getUserInfo(String userId) {
        UserEntity userEntity = userMapper.selectById(userId);
        if (userEntity == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 清空敏感信息
        userEntity.setPassword(null);
        
        return userEntity;
    }

    @Override
    public void updateUserInfo(UserEntity userEntity) {
        // 验证用户是否存在
        UserEntity existUser = userMapper.selectById(userEntity.getId());
        if (existUser == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 只更新允许修改的字段
        UserEntity updateEntity = new UserEntity();
        updateEntity.setId(userEntity.getId());
        
        if (userEntity.getEmail() != null) {
            updateEntity.setEmail(userEntity.getEmail());
        }
        if (userEntity.getPhone() != null) {
            updateEntity.setPhone(userEntity.getPhone());
        }
        if (userEntity.getNickname() != null) {
            updateEntity.setNickname(userEntity.getNickname());
        }
        if (userEntity.getRealName() != null) {
            updateEntity.setRealName(userEntity.getRealName());
        }
        if (userEntity.getAvatar() != null) {
            updateEntity.setAvatar(userEntity.getAvatar());
        }
        if (userEntity.getGender() != null) {
            updateEntity.setGender(userEntity.getGender());
        }
        
        updateEntity.setUpdateTime(EntityHelper.now());
        
        userMapper.updateById(updateEntity);
    }
}