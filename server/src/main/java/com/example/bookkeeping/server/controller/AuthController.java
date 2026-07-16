package com.example.bookkeeping.server.controller;

import com.example.bookkeeping.server.dto.ApiResponse;
import com.example.bookkeeping.server.dto.LoginRequest;
import com.example.bookkeeping.server.dto.RegisterRequest;
import com.example.bookkeeping.server.dto.TokenResponse;
import com.example.bookkeeping.server.security.JwtUtil;
import com.example.bookkeeping.server.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    /**
     * 注册接口
     *
     * 用 Postman 测试：POST http://localhost:8080/api/auth/register
     * Body (JSON): { "username": "zhangsan", "password": "123456" }
     */
    @PostMapping("/register")
    public ApiResponse<Void> register(@RequestBody RegisterRequest registerRequest) {
        //@RequestBody  的意思是：“请把 HTTP 请求体（Request Body）里面的 JSON 字符串，自动帮我转换成 Java 对象 LoginRequest。”
        //
        //底层反射干了啥？（超硬核！）
        //
        //Spring 通过反射读取 LoginRequest 类，得知它有哪些字段（username、password）。
        //
        //利用 Jackson（JSON 工具库） 通过反射调用 LoginRequest 的 setUsername() 和 setPassword() 方法，把 JSON 里的值塞进去。
        //
        //最终，一个活的 Java 对象就诞生了，直接塞到你的 request 参数里。
        authService.register(registerRequest.getUsername(), registerRequest.getPassword());
        return ApiResponse.success(); // 这里返回一个ApiResponse的实例（四个属性都赋值了或者有一个是null），然后 因为  @RestController  这个注解，spring mvc会把 返回的Java对象转成 JSON
    }

    /**
     * 登录接口
     *
     * 用 Postman 测试：POST http://localhost:8080/api/auth/login
     * Body (JSON): { "username": "zhangsan", "password": "123456" }
     *
     * 返回 accessToken 和 refreshToken
     */
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@RequestBody LoginRequest loginRequest) {
        String[] tokens = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
        String accessToken = tokens[0];
        String refreshToken = tokens[1];

        TokenResponse tokenResponse = TokenResponse.of(accessToken, refreshToken, jwtUtil.getAccessTokenExpiration(), jwtUtil.getRefreshTokenExpiration()) ;
        return ApiResponse.success(tokenResponse);

    }


    /**
     * 刷新 Token 接口
     *
     * 用 Postman 测试：POST http://localhost:8080/api/auth/refresh
     * Body (JSON): { "refreshToken": "eyJhbGciOi..." }
     *
     * 注意：这里直接用 Map 接收，因为 refreshToken 不是 LoginRequest 的字段
     */
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        String[] tokens = authService.refreshToken(refreshToken);

        String newAccess = tokens[0];
        String newRefresh = tokens[1];

        TokenResponse tokenResponse = TokenResponse.of(newAccess, newRefresh, jwtUtil.getAccessTokenExpiration(), jwtUtil.getRefreshTokenExpiration());
        return ApiResponse.success(tokenResponse);
    }

}
