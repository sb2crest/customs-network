package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.ExcelBatchResponse;

import java.util.List;
import java.util.concurrent.Future;

public interface AnalyzeFuture {
    void ofExcelBatchResponse(List<Future<ExcelBatchResponse>> futures);
}
