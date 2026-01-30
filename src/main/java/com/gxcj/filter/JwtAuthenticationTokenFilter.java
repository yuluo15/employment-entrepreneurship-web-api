package com.gxcj.filter;

import com.alibaba.fastjson2.JSON;
import com.gxcj.constant.SysConstant;
import com.gxcj.context.UserContext;
import com.gxcj.entity.dto.LoginUser;
import com.gxcj.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");
        if (token == null){
            filterChain.doFilter(request,response);
            return;
        }
        String userId;
        try {
            Claims claims = JwtUtil.parseJWT(token);
            userId = claims.getSubject();
            UserContext.setUserId(userId);
        } catch (Exception e) {
            sendUnauthorized(response, "Token非法");
            return;
        }
        String jsonUser = stringRedisTemplate.opsForValue().get(SysConstant.REDIS_TOKEN_HAND + userId);

        if (Objects.isNull(jsonUser)) {
            sendUnauthorized(response, "用户未登录或Token已过期");
            return;
        }

        // 4. 将 JSON 转回 LoginUser 对象
        LoginUser loginUser = JSON.parseObject(jsonUser, LoginUser.class);

        // 5. 存入 SecurityContextHolder
        //以此告诉 Spring Security：这个人已经登录了，他是谁，他有什么权限
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // 6. 放行
        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new HashMap<>();
        result.put("code", 401);
        result.put("msg", message);
        result.put("data", null);

        response.getWriter().write(JSON.toJSONString(result));
    }
}
