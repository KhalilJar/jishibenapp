package com.example.bookkeeping.server.dto;

import lombok.Data;
import java.util.List;

@Data
public class BatchSyncRequest {
    private List<RecordRequest> records;
}
