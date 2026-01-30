package com.gxcj.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.UUID;


public class JwtUtil {

    // 随便写一串复杂的字符串
    public static final String JWT_KEY = "GouXiaoChuangYeJiuYePingTaiZhangPengKey123456";

    // 过期时间：默认 24 小时 (毫秒单位)
    public static final Long TTL = 24 * 60 * 60 * 1000L;

    /**
     * 生成token
     * @param userId
     * @return
     */
    public static String createJWT(String userId) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        long expMillis = nowMillis + TTL;
        Date expDate = new Date(expMillis);

        return Jwts.builder()
                .setId(EntityHelper.uuid())
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expDate)
                .signWith(SignatureAlgorithm.HS256, JWT_KEY)
                .compact();
    }

    /**
     * 解析 Token
     */
    public static Claims parseJWT(String token) {
        return Jwts.parser()
                .setSigningKey(JWT_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

}
