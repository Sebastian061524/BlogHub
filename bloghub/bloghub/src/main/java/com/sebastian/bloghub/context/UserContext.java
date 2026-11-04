package com.sebastian.bloghub.context;

public class UserContext {
    private static final ThreadLocal<Integer> USER_ID= new ThreadLocal<>();

    public static void setCurrentUserId(Integer userId) {
        USER_ID.set(userId);
    }

    public static Integer getCurrentUserId() {
        return USER_ID.get();
    }

    public static void removeCurrentUserId() {
        USER_ID.remove();
    }
}
