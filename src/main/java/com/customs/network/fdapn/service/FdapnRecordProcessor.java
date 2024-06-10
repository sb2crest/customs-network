package com.customs.network.fdapn.service;

import com.converter.exceptions.InvalidDataException;
import com.converter.service.*;
import com.customs.network.fdapn.dto.*;
import com.customs.network.fdapn.exception.BatchInsertionException;
import com.customs.network.fdapn.model.MessageCode;
import com.customs.network.fdapn.model.TransactionInfo;
import com.customs.network.fdapn.repository.TransactionManagerRepo;
import com.customs.network.fdapn.service.impl.CBPServiceImpl;
import com.customs.network.fdapn.utils.DateUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
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
    private final UserProductInfoServices userProductInfoServices;
    private final ConverterService converterService;
    private final Lock lock = new ReentrantLock();

    private final ObjectMapper objectMapper;
    private final CBPServiceImpl cbpServiceImpl;

    public FdapnRecordProcessor(TransactionManagerRepo transactionManagerRepo, UserProductInfoServices userProductInfoServices, JsonToEdi jsonToEdi, ConverterService converterService, ObjectMapper objectMapper, CBPServiceImpl cbpServiceImpl) {
        this.transactionManagerRepo = transactionManagerRepo;
        this.userProductInfoServices = userProductInfoServices;
        this.converterService = converterService;
        this.objectMapper = objectMapper;
        this.cbpServiceImpl = cbpServiceImpl;
    }

    public List<TransactionInfo> saveSuccessInfo(List<ValidationResponse> validationResponses) throws BatchInsertionException {
        List<TransactionInfo> transactionInfos = validationResponses.stream()
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

        List<TransactionInfo> records;
        log.info("Batch is Ready to save");

        lock.lock();
        long start = System.currentTimeMillis();
        try {
            records = transactionManagerRepo.saveTransaction(transactionInfos);
            long end = System.currentTimeMillis();
        } catch (BatchInsertionException e) {
            log.error("Error during batch insertion, resubmitting batch...");
            records = transactionManagerRepo.saveTransaction(transactionInfos);
        } finally {
            lock.unlock();
            long end = System.currentTimeMillis();
            log.info("Total Locking time for the batch ->{}", (end - start) / 1000.0);
        }
        processEdi(records, validationResponses);
        return records;
    }

    private void processEdi(List<TransactionInfo> savedRecords, List<ValidationResponse> validationResponses) {
        IntStream.range(0, Math.min(savedRecords.size(), validationResponses.size()))
                .forEach(i -> validationResponses.get(i).getExcelTransactionInfo()
                        .setReferenceId(savedRecords.get(i).getReferenceId()));


        validationResponses.stream()
                .filter(Objects::nonNull)
                .forEach(
                        obj -> {
                            Map<Boolean, List<UserProductInfoDto>> booleanListMap = userProductInfoServices.fetchAllProducts(obj.getExcelTransactionInfo().getProductCode(),
                                    obj.getExcelTransactionInfo().getUniqueUserIdentifier());
                            if (booleanListMap.size() == 1 && booleanListMap.containsKey(true)) {
                                List<JsonNode> productInfo = booleanListMap.get(true)
                                        .stream()
                                        .map(UserProductInfoDto::getProductInfo)
                                        .toList();
                                obj.getExcelTransactionInfo().getPriorNoticeData().setProducts(productInfo);
                                JsonNode edi = objectMapper.valueToTree(obj.getExcelTransactionInfo().getPriorNoticeData());
                                try {
                                    String ediFile = converterService.convertToEdi(edi, "FDA");
                                    String refID = obj.getExcelTransactionInfo().getReferenceId();
                                    cbpServiceImpl.hitCbp(ediFile, refID);

                                    obj.getExcelTransactionInfo().getPriorNoticeData().setProducts(new ArrayList<>());
                                } catch (InvalidDataException | ExecutionException | InterruptedException e) {
                                    log.error(e.getMessage());
                                }

                            }
                        }
                );
    }


    public List<CustomerFdaPnFailure> failureRecords(List<ValidationResponse> validationResponses) throws BatchInsertionException {
        List<CustomerFdaPnFailure> fdaPnFailures = new ArrayList<>();
        List<TransactionInfo> failedList = validationResponses.stream()
                .filter(Objects::nonNull)
                .map(
                        obj -> {
                            TransactionInfo transactionInfos = new TransactionInfo();
                            CustomerFdaPnFailure dto = new CustomerFdaPnFailure();
                            dto.setResponseJson(getResponse(obj, false));
                            fdaPnFailures.add(dto);
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
                            return transactionInfos;
                        }
                ).toList();

        List<TransactionInfo> records = null;
        lock.lock();
        try {
            records = transactionManagerRepo.saveTransaction(failedList);
        } catch (BatchInsertionException e) {
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

    private SuccessOrFailureResponse getResponse(ValidationResponse excelResponse, boolean isSuccess) {
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
