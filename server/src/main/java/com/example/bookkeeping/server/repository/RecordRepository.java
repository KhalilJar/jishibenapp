package com.example.bookkeeping.server.repository;

import com.example.bookkeeping.server.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecordRepository extends JpaRepository<Record, Long> {

    /**
     * 查询某个用户在 某个时间范围内的所有记录
     */
    @Query("SELECT r FROM Record r WHERE r.userId = :uid " + "AND r.timestamp >= :startTime AND r.timestamp <= :endTime " +
    "ORDER BY r.timestamp DESC") //这里jpql 中 = 后面的是一个占位符，跟@Param打配合，  比如占位符 :uid  ,就去找  @Param("uid")后面的 那个参数
    List<Record> findByUserIdAndTimeRange(@Param("uid") Long userId,
                                          @Param("startTime") Long startMiles,
                                          @Param("endTime") Long endMills);
    /** 查询某个用户的所有记录  （Desc） */
    List<Record> findByUserIdOrderByTimestampDesc(Long userId);

    /**  删除某条记录 */
    void deleteByIdAndUserId(Long id, Long userId);

    /**
     * 查询某个用户是否已有完全相同的记录（用于去重）
     * 按 (timestamp + type + amount + tag + note + accountId) 判断
     */
    @Query("SELECT COUNT(r) FROM Record r WHERE r.userId = :userId " +
           "AND r.timestamp = :timestamp " +
           "AND r.type = :type " +
           "AND r.amount = :amount " +
           "AND r.tag = :tag " +
           "AND (r.note IS NULL AND :note IS NULL OR r.note = :note) " +
           "AND (r.accountId IS NULL AND :accountId IS NULL OR r.accountId = :accountId)")
    long countDuplicate(@Param("userId") Long userId,
                        @Param("timestamp") Long timestamp,
                        @Param("type") String type,
                        @Param("amount") Double amount,
                        @Param("tag") String tag,
                        @Param("note") String note,
                        @Param("accountId") Long accountId);
}
