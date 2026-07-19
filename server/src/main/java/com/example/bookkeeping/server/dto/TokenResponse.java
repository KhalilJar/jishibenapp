package com.example.bookkeeping.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;          //固定值 "Bearer"
    private long expiresIn;              //单位：秒 accessToken 有效期
    private long refreshExpiresIn;

    /**
     * 使用建造者模式为其赋值
     *
     * @param accessToken
     * @param refreshToken
     * @param expiresIn
     * @param refreshExpiresIn
     * @return
     */
    public static TokenResponse of(String accessToken, String refreshToken, long expiresIn, long refreshExpiresIn) {
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .refreshExpiresIn(refreshExpiresIn)
                .build();
    }
    /**
     * 建造者模式的优势：（相比用全参构造器或者一个个调用setter() ）
     * 1.相比全参构造器：不容易出现参数位置错乱，可读性也更强
     * 2.相比setter，builder()  是把参数一个个先摆好，最后用 builder()  直接构造出来；而setter  是一个个set，中间存在一个半成品状态，这个状态可能被别的线程打断
     *
     */
}