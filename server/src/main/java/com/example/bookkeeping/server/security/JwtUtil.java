package com.example.bookkeeping.server.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    // @Value 注解： 从 application.yml 里读取配置注入到这里
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    /**
     * 把配置文件里的 secret 转成 加密算法所需的 SecretKey 对象
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 accessToken （短期， 用于访问业务接口）
     */
    public String generateAccessToken(Long userId, String username ) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder().subject(String.valueOf(userId))
                .claim("username", username)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     *
     */
    public String generateRefreshToken(Long userId, String username ) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder().subject(String.valueOf(userId))
                .claim("username", username)
                .claim("type", "refresh") // 标记这是 refresh
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从 Token 中提取所有 Claims（载荷信息）
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Token中获取用户ID
     */
//    public Long getUserIdFromToken(String token) {
//        String subject = extractAllClaims(token)
//    }









}
