package com.sebastian.bloghub.interceptor;

import com.sebastian.bloghub.context.UserContext;
import com.sebastian.bloghub.exception.BusinessException;
import com.sebastian.bloghub.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 文章路径的请求鉴权入口：公开读取请求，校验写请求的 Bearer JWT，并向业务层提供当前用户。
 * {@code @Component} 使其可被 WebMvcConfig 注入；具体拦截路径由该配置类注册，本类按请求方法决定是否校验。
 */
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 在 Controller 执行前检查请求，返回 true 才继续；鉴权失败抛业务异常，由统一处理器转换为 401。
     * 当前只直接放行 GET，HEAD、OPTIONS 等方法也会继续走凭证检查，不能把“读接口公开”泛化为所有方法。
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if("GET".equals(request.getMethod())){
            return true;
        }
        // 前缀按当前实现精确匹配 Bearer 加一个空格；只支持该位置的 Authorization 请求头。
        String authorization = request.getHeader("Authorization");
        if(authorization == null || !authorization.startsWith("Bearer ")){
            throw new BusinessException(401, "请先登录");
        }
        // 去掉长度为 7 的前缀后检查是否全为空白，避免拿空凭证调用 JWT 解析。
        String token = authorization.substring(7);
        if(token.isBlank()){
            throw new BusinessException(401, "请先登录");
        }
        try {
            // getUserId 先验签和检查过期，再转换 subject；不能只对 JWT 解码就信任身份。
            Integer userId = jwtUtil.getUserId(token);
            if(userId == null || userId <= 0){
                throw new BusinessException(401, "请先登录");
            }
            // 仅在凭证与用户 ID 均校验通过后写入线程上下文，Service 据此确定作者。
            UserContext.setCurrentUserId(userId);
            // JWT 无效、过期及 subject 无法转为整数等情况统一为 401，不向客户端暴露解析细节。
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(401, "登录凭证无效或已过期");
        }
        return true;
    }

    /**
     * 同步 MVC 请求完成（含控制器处理异常）后清理当前线程身份，避免 Servlet 线程复用导致用户串用。
     * 只有 preHandle 正常放行后 MVC 才为本拦截器调用此回调；本实现鉴权失败时尚未写入身份。
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        UserContext.removeCurrentUserId();
    }
}
