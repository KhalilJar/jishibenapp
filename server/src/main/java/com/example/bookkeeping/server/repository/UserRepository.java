package com.example.bookkeeping.server.repository;

import com.example.bookkeeping.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 根据用户名查找用户
    // JPA 根据方法名 自动生成SQL：
    // SELECT * FORM users WHERE username = ？
    Optional<User> findByUsername(String username);

    // 判断用户名是否存在
    // 自动生成 SQL
    // SELECT COUNT(*) > 0 FROM users WHERE username = ？
    boolean existsByUsername(String username);

}
