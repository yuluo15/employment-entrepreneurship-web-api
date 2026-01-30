package com.gxcj.service.impl;

import com.alibaba.fastjson2.JSON;
import com.gxcj.constant.SysConstant;
import com.gxcj.controller.AuthController;
import com.gxcj.entity.dto.LoginUser;
import com.gxcj.entity.vo.UserVo;
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

    public UserVo login(AuthController.UserReq userReq) {

        String s = EntityHelper.encodedPassword("123456");
        // 1. 使用 AuthenticationManager 进行认证
        // 该方法会自动调用 UserDetailsServiceImpl.loadUserByUsername
        // 并且会自动比对密码（它知道如何处理 BCrypt 加密）
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userReq.getLoginName(), userReq.getPassword());

        Authentication authenticate = authenticationManager.authenticate(authenticationToken);

        // If code reaches here, login is successful. If failed, it throws an exception automatically.
        if (authenticate == null) {
            throw new RuntimeException("登录失败");
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
}