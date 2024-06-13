package com.customs.network.fdapn.service.impl;

import com.customs.network.fdapn.dto.ExcelBatchResponse;
import com.customs.network.fdapn.dto.TransactionFailureResponse;
import com.customs.network.fdapn.exception.ExceptionDuringExecution;
import com.customs.network.fdapn.service.AnalyzeFuture;
import com.customs.network.fdapn.service.ExcelWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@Slf4j
public class AnalyzeFutureImpl implements AnalyzeFuture {
    private final CBPServiceImpl cbpservice;
    private final ExcelWriter excelWriter;

    public AnalyzeFutureImpl(CBPServiceImpl cbpservice, ExcelWriter excelWriter) {
        this.cbpservice = cbpservice;
        this.excelWriter = excelWriter;
    }

    @Override
    public void ofExcelBatchResponse(List<Future<ExcelBatchResponse>> futures){
        log.info("Analyzing futures started");
        executeFailureRecords(futures);
        executeSuccessRecords(futures);
    }

    private void executeFailureRecords(List<Future<ExcelBatchResponse>> futures) {
        new Thread(() -> {
            List<TransactionFailureResponse> failedTransactionHolder = new ArrayList<>();
            for (Future<ExcelBatchResponse> future : futures) {
                ExcelBatchResponse response = getExcelBatchResponseFromFuture(future);
                if (!response.getFailedList().isEmpty()) {
                    failedTransactionHolder.addAll(response.getFailedList());
                }
            }
            if (!failedTransactionHolder.isEmpty()) {
                excelWriter.writeExcel(failedTransactionHolder);
            }
        }).start();
    }

    private void executeSuccessRecords(List<Future<ExcelBatchResponse>> futures) {
        ExecutorService executor = null;
        try {
            executor = Executors.newFixedThreadPool(10);
            if (executor != null) {
                for (Future<ExcelBatchResponse> future : futures) {
                    ExcelBatchResponse response = getExcelBatchResponseFromFuture(future);
                    if (!response.getSuccessList().isEmpty()) {
                        executor.execute(() -> cbpservice.executeFinalProcessingAndSendToCBP(response.getSuccessList()));
                    }
                }
            } else {
                log.error("Failed to initialize ExecutorService");
                throw new IllegalStateException("Failed to initialize ExecutorService");
            }
        } catch (Exception e) {
            log.error("Error during ExecutorService initialization: {}", e.getMessage(), e);
        } finally {
            if (executor != null) {
                executor.shutdown();
            }
        }
    }

    private ExcelBatchResponse getExcelBatchResponseFromFuture(Future<ExcelBatchResponse> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExceptionDuringExecution(e.getMessage());
        } catch (ExecutionException e) {
            throw new ExceptionDuringExecution(e.getMessage());
        }
    }
}
