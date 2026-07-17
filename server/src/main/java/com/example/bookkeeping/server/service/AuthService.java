package com.example.bookkeeping.server.service;

import com.example.bookkeeping.server.entity.User;
import com.example.bookkeeping.server.exception.BusinessException;
import com.example.bookkeeping.server.exception.UnauthorizedException;
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
    // 依旧 和 @RequiredArgsConstructor 打配合，省掉三个 @Autowired
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
            throw new BusinessException("用户名已被注册");
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
    public String[] login(String username, String rawPassword) {
        // 1.根据用户名找用户，没找到就抛出异常
        User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户名或者密码出错"));

        // 2.用salt + 明文密码 做 BCrypt 比对
        String saltedPassword = rawPassword + user.getSalt();
        if (!passwordEncoder.matches(saltedPassword, user.getPasswordHash())) {
            throw new BusinessException("用户名或者密码错误");
        }

        // 3.比对成功，生成JWT
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        return new String[]{accessToken, refreshToken};
    }

    /**
     * 刷新 Token
     * 用户 用旧的 refreshToken 换新的 accessToken + refreshToken
     */
    public String[] refreshToken(String refreshToken) {
        // 1.先校验 refreshToken 是否合法或者过期
        if (!jwtUtil.validate(refreshToken)) {
            throw new UnauthorizedException("请重新登录，bb"); // 原来sb 湖科大app是设置了refreshToken 双token机制
            // sb学校，设置得那么短
        }

        // 2.从token 中获取用户信息
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        String username = jwtUtil.getUsernameFromToken(refreshToken);

        // 3.重新签发 一对 token
        String newAccessToken = jwtUtil.generateAccessToken(userId, username);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId, username);

        return new String[]{newAccessToken, newRefreshToken};

    }
}

/**
 * BCrypt 自带盐值
 * 方案 A（推荐，删除 salt 字段）：既然用了 BCrypt，彻底删掉 User 实体里的 salt 字段，直接用 BCrypt 自带的盐。
 * public void register(String username, String rawPassword) {
 *     if (userRepository.existsByUsername(username)) {
 *         throw new RuntimeException("用户名已被注册");
 *     }
 *     // 直接加密，BCrypt 内部自动生成盐并拼进结果
 *     String passwordHash = passwordEncoder.encode(rawPassword);
 *
 *     User user = User.builder()
 *             .username(username)
 *             .passwordHash(passwordHash)
 *             // .salt(salt)  // 这一行删掉！
 *             .build();
 *     userRepository.save(user);
 * }
 */
