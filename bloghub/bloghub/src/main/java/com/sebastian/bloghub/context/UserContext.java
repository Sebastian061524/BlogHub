package com.sebastian.bloghub.context;

/**
 * 在当前处理线程内保存已验证的用户 ID，让业务层无需依赖 HttpServletRequest。
 * 适用于当前同步 Spring MVC 调用链：拦截器写入，Service 读取，请求结束时移除。
 * ThreadLocal 不是全局登录状态，也不会自动传递到异步线程；线程池会复用线程，必须及时清理。
 */
public class UserContext {
    // static 共享容器对象，但每个线程拥有独立的值，避免并发请求直接共用一个用户 ID。
    private static final ThreadLocal<Integer> USER_ID= new ThreadLocal<>();

    /**
     * 由鉴权拦截器在 JWT 验证成功后写入可信身份；不能使用客户端自行提交的作者 ID。
     */
    public static void setCurrentUserId(Integer userId) {
        USER_ID.set(userId);
    }

    /**
     * 供业务层获取当前线程的用户 ID；没有已写入的身份时返回 null，调用方据此返回 401。
     */
    public static Integer getCurrentUserId() {
        return USER_ID.get();
    }

    /**
     * 由请求完成回调移除当前线程的值，避免线程复用时把上一次请求的身份带给下一次请求。
     */
    public static void removeCurrentUserId() {
        USER_ID.remove();
    }
}
