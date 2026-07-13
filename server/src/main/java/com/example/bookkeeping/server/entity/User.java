package com.example.bookkeeping.server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id  //表明下面那个变量是主键
    @GeneratedValue(strategy = GenerationType.IDENTITY)  //主键自增
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    // @Column 用于配置列的属性
    private String username;

    @Column(name = "password_hash", nullable = false, length = 200)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String salt; //盐值  增强密码安全性

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }  // @PrePersist 是 JPA 的生命周期回调，在insert之前执行


}
