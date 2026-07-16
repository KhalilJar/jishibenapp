package com.example.bookkeeping.server.config;

import com.example.bookkeeping.server.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    //  依旧单构造器
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    /**
     * 把BCryptPasswordEncoder  注册为一个Bean
     *  “@Bean"的含义：
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * SecurityFilterChain 是 Spring Security  的核心配置
     * 该方法定义了 ： 哪些请求需要认证、哪些不需要、使用什么过滤器
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) //关闭 csrf 保护 （前后端分离的 REST API 不需要 CSRF）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 设置为无状态模式 —— 不创建 Session，每次请求都通过 JWT 独立验证
                //配置接口访问权限
                .authorizeHttpRequests(auth -> auth
                // 以下路径不需要认证（公开访问）
                        .requestMatchers("/api/auth/register").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/refresh").permitAll()
//                        .requestMatchers("/h2-console/**").permitAll()  // H2 数据库控制台
                        .requestMatchers("/error").permitAll()           // Spring 默认错误页

                        // 其他所有接口都需要认证（头里必须带合法的 JWT Token）
                        .anyRequest().authenticated())
                // 允许 H2 控制台使用 iframe（默认被 Spring Security 阻止）
//                .headers(header -> headers.frameOptions(frame -> frame.sameOrigin()))
                // 把我们写的 JWT 过滤器插入到 Spring Security 过滤器链中
                // 位置：在 UsernamePasswordAuthenticationFilter 之前执行
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
