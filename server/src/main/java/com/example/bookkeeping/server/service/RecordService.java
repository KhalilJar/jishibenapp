package com.example.bookkeeping.server.service;

import com.example.bookkeeping.server.entity.Record;
import com.example.bookkeeping.server.repository.RecordRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service  //把类 收进ioc，跟 @Autowired  或者 @RequiedArgsConstructor+ final 打配合
@RequiredArgsConstructor
public class RecordService {
    private final RecordRepository recordRepository;

    /** 新增一条记录
     *
     * save方法来自老祖宗接口 CrudRepository，
     *
     * 作用是 保存实体（插入/更新） 到数据库
     *
     * 情况 A（插入）：如果你传入的 Record 对象里的 id（主键）是 null 或者 0，Hibernate 认为这是一条新数据，会自动执行 INSERT（插入） SQL。
     * 情况 B（更新）：如果你传入的 Record 对象里的 id 有具体的值（比如 id=5），Hibernate 会先去数据库查一下有没有 id=5 的数据，如果有，就执行 UPDATE（更新） SQL；如果没有，就报错或插入（取决于特定策略）。
     */
    @Transactional  // 声明式事务：这个方法里的数据库操作在一个事务里执行
    public Record createRecord(Record record) {
        return recordRepository.save(record);
    }

    /** 查询用户的所有记录 */
    public List<Record> getUserRecords(Long userId) {
        return recordRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    /** 按时间范围查询记录 */
    public List<Record> getUserRecordsByTimeRange(Long userId, Long startTime, Long endTime) {
        return recordRepository.findByUserIdAndTimeRange(userId, startTime, endTime);
    }

    /** 更新记录 */
    @Transactional
    public Record updateRecord(Long recordId, Long userId, Record updated) {
        // 先根据传入的 recordId 查询 是否存在这条记录
        Record existing = recordRepository.findById(recordId).orElseThrow(() -> new RuntimeException("记录不存在"));
        // 等后面把自定义异常写了后 来改 runtimeException
        // 再看 是否是自己的userId
        if (!existing.getUserId().equals(userId))
            throw new RuntimeException("无权修改");
        // 更新字段
        existing.setType(updated.getType());
        existing.setNote(updated.getNote());
        existing.setAmount(updated.getAmount());
        existing.setTag(updated.getTag());
        existing.setAccountId(updated.getAccountId());
        existing.setTimestamp(updated.getTimestamp());

        // 返回值
        return recordRepository.save(existing);
    }

    /** 删除记录 */
    @Transactional
    public void deleteRecord(Long userId, Long recordId) {
        // 依旧根据 主键 id 先检验是否存在这条记录
        Record exsiting = recordRepository.findById(recordId).orElseThrow(() -> new RuntimeException("记录不存在"));

        // 依旧再根据 recordId上的userid 与原userId 做对比
        if (!exsiting.getUserId().equals(userId))
            throw new RuntimeException("nmd权限不对啊");

        recordRepository.deleteByIdAndUserId(recordId, userId);
    }

    /**
     * 批量同步记录（带去重）
     * 客户端首次登录或离线恢复时调用
     */
    @Transactional
    public SyncResult batchSync(Long userId, List<Record> records) {
        int inserted = 0;
        int skipped = 0;

        for (Record record : records) {
            // 检查是否已存在（去重）
            long count = recordRepository.countDuplicate(
                userId,
                record.getTimestamp(),
                record.getType(),
                record.getAmount(),
                record.getTag(),
                record.getNote(),
                record.getAccountId()
            );

            if (count > 0) {
                skipped++;
                continue;
            }

            // 不存在则插入
            record.setId(null);
            record.setUserId(userId);
            recordRepository.save(record);
            inserted++;
        }

        return new SyncResult(inserted, skipped);
    }

    @Data
    @AllArgsConstructor
    public static class SyncResult {
        private int inserted;
        private int skipped;
    }
}
