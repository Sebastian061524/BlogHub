package com.sebastian.bloghub.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.time.Instant;

@Component
public class JwtUtil {
    private final SecretKey secretKey;
    private final long expirationSeconds;

    public JwtUtil(
            @Value("${jwt.secret-base64}") String secretBase64,
            @Value("${jwt.expiration-seconds}") long expirationSeconds) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretBase64));
        this.expirationSeconds = expirationSeconds;
    }

    /**
     * 生成 Token：把身份和时间信息放进载荷，再签名，生成凭证。
     * 1. 将用户 ID 存入 Payload 的 subject，同时放入签发时间和过期时间。
     *    expirationSeconds 是有效期，用于计算过期时间，并非直接存入 Token。
     * 2. 将 Header 和 Payload 分别做 Base64URL 编码。
     * 3. 用服务端密钥对编码后的 Header.Payload 计算 HS256 签名。
     * 4. 拼成三段式 Token：编码后的 Header.编码后的 Payload.Base64URL 编码后的签名。
     * Base64URL 编码不是加密，Payload 可以被读取，因此不能放入密码等秘密信息。
     * 密钥保存在服务端，不会放入 Token；本方法本身不验证账号密码或查询用户是否存在。
     */
    public String generateToken(Integer userId){
        if(userId == null || userId <= 0){
            throw new IllegalArgumentException("用户ID必须为正整数");
        }
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 解析 Token：验证凭证的签名和有效期，通过后返回载荷 Claims。
     * 解析过程会解码读取内容，使用服务端密钥验签，并检查是否过期。
     * verifyWith(secretKey) 指定验签使用的密钥，不是比较 Token 中的密钥是否相同。
     * 如果 Payload 被篡改，原签名将无法通过验证；如果 Token 已过期，也会被拒绝。
     * 验证失败时抛出异常，不会正常返回 Claims，不能只解码后就信任其中的用户 ID。
     */
    public Claims parseToken(String token){
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getExpirationSeconds() {
        return expirationSeconds;
    }

    /**
     * 先通过 parseToken 验证凭证，再读取 subject，并将字符串用户 ID 转为整数。
     * 例如 generateToken(3) 存入字符串 "3"，此方法验证并解析后返回整数 3。
     */
    public Integer getUserId(String token){
        Claims claims = parseToken(token);
        String userId = claims.getSubject();
        return Integer.parseInt(userId);
    }
}
