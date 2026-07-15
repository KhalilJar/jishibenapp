package com.example.bookkeeping.server.service;

import com.example.bookkeeping.server.entity.User;
import com.example.bookkeeping.server.repository.UserRepository;
import com.example.bookkeeping.server.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.lang.model.element.NestingKind;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册
     * @param username 用户名
     * @param rawPassword 用户输入的 明文密码
     */
    public void register(String username, String rawPassword) {
        // 1.检查用户名是否存在
        if  (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已被注册");
        }

        // 2.使用UUID生成唯一盐值
        String salt = UUID.randomUUID().toString();

        // 3.将 salt 混入密码做 BCrypt 加密
        String saltedPassword = rawPassword + salt;
        String passwordHash = passwordEncoder.encode(saltedPassword);

        // 4.用builder（） 保存新注册的用户到数据库
        User user = User.builder().username(username).passwordHash(passwordHash).salt(salt).build();   // 定义一个user对象存入数据
        userRepository.save(user); // 把刚定义且初始化的 user对象存进 userRepository

    }

    /**
     * 用户登录
     * @param username 用户名
     * @param rawPassword 用户输入的 明文密码
     * @return TokenResponse 包含 accessToken 和 refreshToken
     */
    // String 为 String[]
    public String login(String username, String rawPassword) {
        // 1.根据用户名找用户，没找到就抛出异常
        User user = userRepository.findByUserName(username).orElseThrow(() -> new RuntimeException("用户名或者密码出错"));

        // 2.用salt + 明文密码 做 BCrypt 比对
        return "";
    }

    /**
     * 刷新 Token
     * 用户用旧的 refreshToken 换新的 accessToken + refreshToken
     */

}
