package com.example.bookkeeping.server.controller;

import com.example.bookkeeping.server.entity.Record;
import com.example.bookkeeping.server.dto.ApiResponse;
import com.example.bookkeeping.server.dto.RecordRequest;
import com.example.bookkeeping.server.service.RecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class RecordController {
    private final RecordService recordService;

    /**
     * 通用方法：从 JWT 中获取当前登录用户的 ID
     * 这个 ID 是在 JwtAuthenticationFilter 里放进去的
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }


    /**
     * 新增记账记录
     *
     * 用 Postman 测试：POST http://localhost:7070/api/records
     * Header: Authorization: Bearer <accessToken>
     * Body (JSON): { "type": "EXPENSE", "amount": 35.5, "tag": "餐饮", "note": "午餐", ... }
     */
    @PostMapping
    public ApiResponse<Record> createRecord(@RequestBody RecordRequest recordRequest) {
        Long userId  = getCurrentUserId();

        Record record = Record.builder()
                .userId(userId)
                .type(recordRequest.getType() )
                .tag(recordRequest.getTag() )
                .amount(recordRequest.getAmount())
                .timestamp(recordRequest.getTimestamp())
                .accountId(recordRequest.getAccountId())
                .note(recordRequest.getNote())
                .build();

        Record saved = recordService.createRecord(record);
        return ApiResponse.success(saved);   // 要先用createRecord方法  调用 老祖宗传下来的 save方法保存进数据库，再返回 一条json信息
    }

    /**
     * 查询当前用户的所有记录
     */
    @GetMapping
    public ApiResponse<List<Record>> getRecords() {
        Long userId = getCurrentUserId();
        List<Record> records = recordService.getUserRecords(userId);

        return ApiResponse.success(records);
    }

    /**
     * 按时间范围查询记录
     */
    @GetMapping("/range")
    public ApiResponse<List<Record>> getRecordsByTime(@RequestParam Long start, @RequestParam Long end) {
        Long userId  = getCurrentUserId();
        List<Record> records = recordService.getUserRecordsByTimeRange(userId, start, end);

        return ApiResponse.success(records);
    }

    /**
     * 更新记录
     */
    @PutMapping("/{id}")
    public ApiResponse<Record> updateRecord(@PathVariable Long id, @RequestBody RecordRequest recordRequest) {
        Long userId = getCurrentUserId();

        Record updated = Record.builder()
                .id(id)
                .note(recordRequest.getNote())
                .tag(recordRequest.getTag())
                .timestamp(recordRequest.getTimestamp())
                .amount(recordRequest.getAmount())
                .type(recordRequest.getType())
                .accountId(recordRequest.getAccountId())
                .build();
        Record result = recordService.updateRecord(id, userId, updated);
        return ApiResponse.success(result);
    }

    /**
     * 删除记录
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRecord(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        recordService.deleteRecord(id, userId);
        return ApiResponse.success();
    }

}
