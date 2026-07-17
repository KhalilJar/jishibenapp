package com.example.bookkeeping.server.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {
    // @Value 注解： 从 application.yml 里读取配置注入到这里
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;  //毫秒

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;  //毫秒

    // 新增：获取AccessToken 的有效秒数 （供前端使用）
    public long getAccessTokenExpiration() {
        return accessTokenExpiration / 1000;   // 单位： 秒
    }

    //  新增：获取RefreshToken 的有效秒数 （供前端使用）
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration / 1000;  // 单位： 秒
    }

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
     *生成 RefreshToken
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
    public Long getUserIdFromToken(String token) {
        String subject = extractAllClaims(token) // 对token 做校验，验签 + 过期
                .getSubject();              // subject 已指明返回类型
        return Long.parseLong(subject);  // 把String 强转为 Long
    }

    /**
     * 从 Token 中获取 用户名
     */
    public String getUsernameFromToken(String token) {
        return extractAllClaims(token).get("username", String.class);
    }

    /**
     * 校验 Token 是否合法 （签名是否准确，是否过期）
     *
     * @return true 表示 有效， false 无效
     */
    public boolean validate(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            // 不是 非法篡改， 只是时间到了
//            System.err.println();   // spring boot 中不推荐这么写，一般都是配合
            log.error("JWT 已过期：" + e);
            return  false;
        } catch (JwtException e) {
            // 签名不对  ，   格式错误等等
//            System.err.println();   // spring boot 中不推荐这么写，一般都是配合
            log.error("JWT 无效：" + e);
            return  false;
        }
    }








}
