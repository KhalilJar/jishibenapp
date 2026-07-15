package com.example.bookkeeping.server.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

import static com.fasterxml.jackson.databind.type.LogicalType.Collection;

@Component
@RequiredArgsConstructor   // Lombok 自动生成 “包含所有 final字段 和 @notnull  的构造器
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil; // 这里不需要 @Autowired,因为又@RequiredArgsConstructor
    // 所以该类只有一个构造器
    // 而从 Spring 4.3 开始，如果一个类只有一个构造方法（哪怕是隐式生成的），Spring 在实例化这个 Bean 时，会自动把构造方法参数需要的依赖注入进去，不再需要显式写 @Autowired。

    /**
     * 这个方法在 每个 HTTP 请求到达 Controller之前被调用
     * OncePerRequestFilter：保证每个请求只被这个过滤器处理一次
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        // 1.从请求头中取出 Authorization 字段
        String authHeader = request.getHeader("Authorization");

        // 2. 如果没有 Authorization 头，或者不是 "Bearer " 开头，直接放行
        //   （放行后由 Spring Security 配置决定是否需要登录）
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        // 3.去掉 “Bearer ” 前缀，得到 纯token
        String token = authHeader.substring(7);

        // 4.校验 token是否有效
        if (token.isEmpty()  || !jwtUtil.validate(token)) {
            chain.doFilter(request, response);
            return;
        }

        // 5.Token有效， 提取信息
        Long userId = jwtUtil.getUserIdFromToken(token);
        String userName = jwtUtil.getUsernameFromToken(token);

        // 6.把用户信息 “注入” 到Spring Security 的上下文中
        // 后续代码可通过 SecurityContextHolder 拿到当前登录用户
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userId,
                null,
                Collections.emptyList()
        );
        authentication.setDetails(userName);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 7.放行，请求继续向下传递到 Controller 层
        chain.doFilter(request, response);

    }

}
