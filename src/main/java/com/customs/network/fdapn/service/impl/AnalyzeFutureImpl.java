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
import java.util.concurrent.*;

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
        log.info("Total number of processors available: {}", Runtime.getRuntime().availableProcessors());
        List<CompletableFuture<Void>> tasks = futures.stream()
                .map(this::getExcelBatchResponseFromFuture)
                .filter(response -> !response.getSuccessList().isEmpty())
                .map(response -> CompletableFuture.runAsync(() -> cbpservice.executeFinalProcessingAndSendToCBP(response.getSuccessList())))
                .toList();

        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]))
                .join();
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
