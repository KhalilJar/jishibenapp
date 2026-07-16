package com.example.bookkeeping.server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Record {
    @Id  // 主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 主键自增
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "type", nullable = false, length = 10)
    private String type;

    @Column(nullable = false)
    private Double amount;  //账户余额

    @Column(nullable = false)
    private String tag;

    @Column(length = 200)
    private String note;

    @Column(nullable = false)
    private Long timestamp;

    @Column(name = "account_id")
    private String accountId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    // @PrePersist 是 JPA 的生命周期回调，在insert之前执行
}
