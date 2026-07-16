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


}
