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

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if("GET".equals(request.getMethod())){
            return true;
        }
        String authorization = request.getHeader("Authorization");
        if(authorization == null || !authorization.startsWith("Bearer ")){
            throw new BusinessException(401, "请先登录");
        }
        String token = authorization.substring(7);
        if(token.isBlank()){
            throw new BusinessException(401, "请先登录");
        }
        try {
            Integer userId = jwtUtil.getUserId(token);
            if(userId == null || userId <= 0){
                throw new BusinessException(401, "请先登录");
            }
            UserContext.setCurrentUserId(userId);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(401, "登录凭证无效或已过期");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        UserContext.removeCurrentUserId();
    }
}
