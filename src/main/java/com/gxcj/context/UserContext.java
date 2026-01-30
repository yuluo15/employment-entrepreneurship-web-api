package com.gxcj.context;

public class UserContext {
    public static ThreadLocal<String> userId = new ThreadLocal<>();

    public static String getUserId() {
        return userId.get();
    }

    public static void setUserId(String userId) {
        UserContext.userId.set(userId);
    }

    public static void removeUserId() {
        UserContext.userId.remove();
    }
}
