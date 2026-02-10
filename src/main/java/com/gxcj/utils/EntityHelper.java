package com.gxcj.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Timestamp;
import java.util.UUID;

public class EntityHelper {
    public static String uuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }

    public static String encodedPassword(String password) {
        return new BCryptPasswordEncoder().encode(password);
    }

    public static Boolean matchesPassword(String password, String encodedPassword) {
        return new BCryptPasswordEncoder().matches(password, encodedPassword);
    }
}
