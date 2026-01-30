package com.gxcj.config;

import com.gxcj.filter.JwtAuthenticationTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; // 1. 引入这个
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // <--- 2. 必须加上这个注解，否则找不到 HttpSecurity
public class SpringSecurityConfig {

    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Bean
    public SecurityFilterChain springSecurityFilterChain1(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF (前后端分离必须)
                .csrf(csrf -> csrf.disable())
                // 禁用 Session (改为无状态)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 请求权限配置
                .authorizeHttpRequests(auth -> auth
                        // 放行不需要认证的接口
                        .requestMatchers("/api/auth/login", "/api/auth/logout", "/api/sendEmail/code","/register", "/doc.html", "/webjars/**", "/swagger-resources/**").permitAll()
                        // 其他所有请求必须认证
                        .anyRequest().authenticated()
                )
                // 添加 JWT 过滤器
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 3. 必须注入密码加密器 (你原本写的没问题，保留)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 4. 【额外补充】必须暴露 AuthenticationManager
    //    后续你在 LoginService 里进行“账号密码校验”时，需要注入这个Bean
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}