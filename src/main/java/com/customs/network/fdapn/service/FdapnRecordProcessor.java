package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.*;
import com.customs.network.fdapn.exception.BatchInsertionException;
import com.customs.network.fdapn.model.CustomsFdapnSubmit;
import com.customs.network.fdapn.model.MessageCode;
import com.customs.network.fdapn.model.TransactionInfo;
import com.customs.network.fdapn.repository.TransactionManagerRepo;
import com.customs.network.fdapn.utils.DateUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
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

    public List<TransactionInfo> saveSuccessInfo(List<ValidationResponse> validationResponses) throws BatchInsertionException {
        List<TransactionInfo> transactionInfos=validationResponses.stream()
                .filter(Objects::nonNull)
                .map(obj->{
                    TransactionInfo transactionInfo=new TransactionInfo();
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
                    JsonNode jsonNode = convertExcelResponse(obj.getExcelTransactionInfo());
                    transactionInfo.setRequestJson(jsonNode);
                    JsonNode response = convertResponseToJson(getResponse(obj, true));
                    transactionInfo.setResponseJson(response);
                    return transactionInfo;
                })
                .toList();

        List<TransactionInfo> records;
        long start = System.currentTimeMillis();
        lock.lock();
        try {
            records = transactionManagerRepo.saveTransaction(transactionInfos);
        } catch (BatchInsertionException e) {
            log.error("Error during batch insertion, resubmitting batch...");
            records = transactionManagerRepo.saveTransaction(transactionInfos);
        } finally {
            lock.unlock();
        }
        long end = System.currentTimeMillis();
        log.info("Time to execute lock operation ->{}", (end - start) / 1000.0);

        return records;
    }


    public List<CustomerFdaPnFailure> failureRecords(List<ValidationResponse> validationResponses) throws BatchInsertionException {
        List<CustomerFdaPnFailure> fdaPnFailures=new ArrayList<>();
        List<TransactionInfo> failedList = validationResponses.stream()
                .filter(Objects::nonNull)
                .map(
                        obj -> {
                            TransactionInfo transactionInfos = new TransactionInfo();
                            CustomerFdaPnFailure dto = new CustomerFdaPnFailure();
                            dto.setResponseJson(getResponse(obj,false));
                            fdaPnFailures.add(dto);
                            Date date = new Date();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                            String formattedDate = dateFormat.format(date);
                            String uuid=obj.getExcelTransactionInfo().getUniqueUserIdentifier();
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
                            return transactionInfos;
                        }
                ).toList();

        List<TransactionInfo> records=null;
        lock.lock();
        try {
            records = transactionManagerRepo.saveTransaction(failedList);
        }catch (BatchInsertionException e) {
            log.error("Error during batch insertion, resubmitting batch...");
            records = transactionManagerRepo.saveTransaction(failedList);
        } finally {
            lock.unlock();
        }
        List<TransactionInfo> finalRecords = records;

        return IntStream.range(0, Math.min(fdaPnFailures.size(), records.size()))
                .mapToObj(i -> {
                    CustomerFdaPnFailure dto = fdaPnFailures.get(i);
                    TransactionInfo record = finalRecords.get(i);
                    dto.setBatchId(record.getBatchId());
                    dto.setUserId(record.getUniqueUserIdentifier());
                    dto.setReferenceIdentifierNo(record.getReferenceId());
                    dto.setCreatedOn(DateUtils.formatDate(record.getCreatedOn()));
                    dto.setStatus(record.getStatus());
                    dto.setRequestJson(convertJsonNodeToCustomerDetails(record.getRequestJson()));
                    return dto;
                })
                .collect(Collectors.toList());
    }
    private String generateSequentialNumber() {
        return String.valueOf(sequentialNumber.getAndIncrement());
    }
    private SuccessOrFailureResponse getResponse(ValidationResponse excelResponse, boolean isSuccess){
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

}
