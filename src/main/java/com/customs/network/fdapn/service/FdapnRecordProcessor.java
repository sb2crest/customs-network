package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.*;
import com.customs.network.fdapn.exception.BatchInsertionException;
import com.customs.network.fdapn.model.MessageCode;
import com.customs.network.fdapn.model.TransactionInfo;
import com.customs.network.fdapn.repository.TransactionManagerRepo;
import com.customs.network.fdapn.utils.DateUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import java.util.stream.IntStream;

import static com.customs.network.fdapn.model.MessageCode.SUCCESS_SUBMIT;
import static com.customs.network.fdapn.utils.JsonUtils.*;

@Component
@Slf4j
public class FdapnRecordProcessor {
    private final AtomicInteger sequentialNumber = new AtomicInteger(0);
    private final TransactionManagerRepo transactionManagerRepo;
    private final Lock lock = new ReentrantLock();


    public FdapnRecordProcessor(TransactionManagerRepo transactionManagerRepo) {
        this.transactionManagerRepo = transactionManagerRepo;
    }

    public List<ExcelValidationResponse> saveSuccessInfo(List<ExcelValidationResponse> excelValidationResponse){
        List<TransactionInfo> successList = excelValidationResponse.stream()
                .filter(Objects::nonNull)
                .map(obj -> {
                    TransactionInfo transactionInfo = new TransactionInfo();
                    Date date = new Date();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                    String formattedDate = dateFormat.format(date);
                    transactionInfo.setUniqueUserIdentifier(obj.getExcelTransactionInfo().getUniqueUserIdentifier());
                    String batchId = transactionInfo.getUniqueUserIdentifier() + formattedDate;
                    transactionInfo.setBatchId(batchId);
                    String traceId = formattedDate + generateSequentialNumber();
                    transactionInfo.setTraceId(traceId);
                    transactionInfo.setEnvelopNumber("ENV001");
                    transactionInfo.setCreatedOn(new Date());
                    transactionInfo.setUpdatedOn(new Date());
                    transactionInfo.setStatus(SUCCESS_SUBMIT.getStatus());
                    obj.getExcelTransactionInfo().getPriorNoticeData().setProducts(new ArrayList<>());
                    JsonNode jsonNode = convertExcelResponse(obj.getExcelTransactionInfo());
                    transactionInfo.setRequestJson(jsonNode);
                    JsonNode response = convertResponseToJson(getResponse(obj, true));
                    transactionInfo.setResponseJson(response);
                    return transactionInfo;
                })
                .toList();
        List<TransactionInfo> transactionInfos = submitToDbProcessing(successList);
        if(!transactionInfos.isEmpty()){
             assignProcessedRefIDs(transactionInfos,excelValidationResponse);
        }
        return excelValidationResponse;
    }


    public List<TransactionFailureResponse> failureRecords(List<ExcelValidationResponse> excelValidationResponse) {
        List<TransactionFailureResponse> fdaPnFailures = new ArrayList<>();
        List<TransactionInfo> failedList = excelValidationResponse.stream()
                .filter(Objects::nonNull)
                .map(
                        obj -> {
                            TransactionInfo transactionInfos = new TransactionInfo();
                            TransactionFailureResponse dto = new TransactionFailureResponse();
                            dto.setResponseJson(getResponse(obj, false));

                            Date date = new Date();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                            String formattedDate = dateFormat.format(date);
                            String uuid = obj.getExcelTransactionInfo().getUniqueUserIdentifier();
                            String batchId = uuid + formattedDate;
                            transactionInfos.setBatchId(batchId);
                            String traceId = formattedDate + generateSequentialNumber();
                            transactionInfos.setTraceId(traceId);
                            transactionInfos.setUniqueUserIdentifier(uuid);
                            transactionInfos.setEnvelopNumber("ENV003");
                            transactionInfos.setCreatedOn(new Date());

                            transactionInfos.setUpdatedOn(new Date());
                            transactionInfos.setStatus(MessageCode.VALIDATION_ERRORS.getStatus());
                            JsonNode jsonNode = convertExcelResponse(obj.getExcelTransactionInfo());
                            transactionInfos.setRequestJson(jsonNode);
                            JsonNode saveResponse = convertResponseToJson(getResponse(obj, false));
                            transactionInfos.setResponseJson(saveResponse);
                            fdaPnFailures.add(dto);
                            return transactionInfos;
                        }
                ).toList();
        List<TransactionInfo> transactionInfos = submitToDbProcessing(failedList);
        if(!transactionInfos.isEmpty()){
          return generateFailureRespose(transactionInfos,fdaPnFailures);
        }
        return new ArrayList<>();
    }

    private List<TransactionInfo> submitToDbProcessing(List<TransactionInfo> processedDate) {
        List<TransactionInfo> records=new ArrayList<>();
        int retryCount = 0;
        while (retryCount < 3) {
            lock.lock();
            try {
                records = transactionManagerRepo.saveTransaction(processedDate);
                break;
            } catch (BatchInsertionException e) {
                retryCount++;
                if (retryCount < 3) {
                    log.error("Error during batch insertion, retrying...");
                } else {
                    log.error("Error during batch insertion after 3 attempts, giving up.");
                }
            } finally {
                lock.unlock();
            }
        }
        return records;
    }

    private String generateSequentialNumber() {
        return String.valueOf(sequentialNumber.getAndIncrement());
    }

    private SuccessOrFailureResponse getResponse(ExcelValidationResponse excelResponse, boolean isSuccess) {
        SuccessOrFailureResponse response = new SuccessOrFailureResponse();
        String messageCode = isSuccess ? MessageCode.SUCCESS_SUBMIT.getCode() : MessageCode.VALIDATION_ERRORS.getCode();
        String status = isSuccess ? MessageCode.SUCCESS_SUBMIT.getStatus() : MessageCode.VALIDATION_ERRORS.getStatus();
        String envelopNumber = isSuccess ? "ENV001" : "ENV003";
        response.setMessageCode(messageCode);
        response.setStatus(status);
        response.setEnvelopNumber(envelopNumber);
        if (!isSuccess) {
            JsonNode validationError = convertValidationErrorListToJson(excelResponse.getValidationErrorList());
            response.setMessage(validationError);
        }
        return response;
    }

    private void assignProcessedRefIDs(List<TransactionInfo> savedRecords, List<ExcelValidationResponse> excelValidationResponse) {
        IntStream.range(0, Math.min(savedRecords.size(), excelValidationResponse.size()))
                .forEach(i -> excelValidationResponse.get(i).getExcelTransactionInfo()
                        .setReferenceId(savedRecords.get(i).getReferenceId()));
    }

    private List<TransactionFailureResponse> generateFailureRespose(List<TransactionInfo> savedRecords, List<TransactionFailureResponse> fdaPnFailures) {
       return IntStream.range(0, fdaPnFailures.size())
                .mapToObj(i -> {
                    TransactionFailureResponse dto = fdaPnFailures.get(i);
                    TransactionInfo savedInfo = savedRecords.get(i);
                    dto.setBatchId(savedInfo.getBatchId());
                    dto.setUniqueUserIdentifier(savedInfo.getUniqueUserIdentifier());
                    dto.setReferenceIdentifierNo(savedInfo.getReferenceId());
                    dto.setCreatedOn(DateUtils.formatDate(savedInfo.getCreatedOn()));
                    dto.setStatus(savedInfo.getStatus());
                    dto.setRequestInfo(convertJsonNodeToExcelResponseInfo(savedInfo.getRequestJson()));
                    return dto;
                })
                .toList();
    }

}
