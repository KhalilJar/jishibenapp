package com.example.bookkeeping.java.security;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class TestJwtUtil {
    public static void main(String[] args) {
        TestJwtUtil.testJwt();
    }


    private static SecretKey getKey(String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    // 这里用JWT加密

    /**
     * header：
     * {
     *     'typ': 'JWT',
     *     'alg': 'HS256'  // alg（签名算法）是由 .signWith() 方法根据你传入的 SecretKey 自动推导并写入 Header 的。手动添加 alg 会被库自动覆盖或忽略
     * }
     * payload:
     * {
     *     "sub": 'JayJar',
     *     "username": 'jay',
     *     "password": 'jayjar050620'
     * }
     * signature：
     * {
     *
     * }
     */
//    @Test
    public static void testJwt() {
        String userName;
        String rawPassWord;
        long time =1000 * 60 * 60 * 24;
        SecretKey  key = Jwts.SIG.HS256.key().build();


        JwtBuilder jwtBuilder = Jwts.builder();
        String jwtToken = jwtBuilder.header()
                .add("typ","JWT")
//                .add("alg", "HS256")
                .and()
                .subject("JayJar")
                .claim("username", "jay")
                .claim("password", "jayjar050620")
                .signWith(key)
                .expiration(new Date(System.currentTimeMillis()+ time))
                .compact();

        System.out.println(jwtToken);
    }
}
