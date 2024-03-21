package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.*;
import com.customs.network.fdapn.exception.NotFoundException;
import com.customs.network.fdapn.exception.RecordNotFoundException;
import com.customs.network.fdapn.model.*;
import com.customs.network.fdapn.repository.TransactionRepository;
import com.customs.network.fdapn.utils.DateUtils;
import com.customs.network.fdapn.utils.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.customs.network.fdapn.utils.JsonUtils.*;
import static java.util.Objects.isNull;


@Component
@AllArgsConstructor
@Slf4j
public class FdaPnRecordSaver {
    private final TransactionRepository transactionRepository;
    private final AtomicInteger sequentialNumber = new AtomicInteger(0);

    @Transactional
    public void save(ExcelResponse excelResponse) {
        TrackingDetails customerDetails = excelResponse.getTrackingDetails();
        if (customerDetails == null) {
            throw new NotFoundException("CustomerDetails cannot be null");
        }
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String formattedDate = dateFormat.format(date);
        CustomsFdapnSubmit customsFdapnSubmit = new CustomsFdapnSubmit();

        String batchId = customerDetails.getUserId() + formattedDate;
        customsFdapnSubmit.setBatchId(batchId);

        String traceId = formattedDate + generateSequentialNumber();
        customsFdapnSubmit.setTraceId(traceId);
        customsFdapnSubmit.setUserId(customerDetails.getUserId());
        customsFdapnSubmit.setAccountId(customerDetails.getAccountId());
        customsFdapnSubmit.setEnvelopNumber("ENV001");
        customsFdapnSubmit.setCreatedOn(new Date());
        customsFdapnSubmit.setUpdatedOn(new Date());
        customsFdapnSubmit.setStatus(String.valueOf(Status.ACCEPTED));
        JsonNode jsonNode = JsonUtils.convertCustomerDetailsToJson(customerDetails);
        customsFdapnSubmit.setRequestJson(jsonNode);
        JsonNode response = JsonUtils.convertResponseToJson(getResponse(excelResponse,true));
        customsFdapnSubmit.setResponseJson(response);
        CustomsFdapnSubmit record = transactionRepository.saveTransaction(customsFdapnSubmit);
        excelResponse.getTrackingDetails().setReferenceIdentifierNo(record.getReferenceId());
        log.info("submit saved in Data base : {}", customsFdapnSubmit);

    }
    public CustomerFdaPnFailure failureRecords(ExcelResponse excelResponse) {
        TrackingDetails customerDetails = excelResponse.getTrackingDetails();
        if (isNull(customerDetails) || StringUtils.isBlank(customerDetails.getUserId())) {
            throw new NotFoundException(isNull(customerDetails) ? "CustomerDetails cannot be null" : "User ID cannot be null or empty");
        }
        CustomsFdapnSubmit customsFdapnSubmit = new CustomsFdapnSubmit();
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String formattedDate = dateFormat.format(date);

        String batchId = customerDetails.getUserId() + formattedDate;
        customsFdapnSubmit.setBatchId(batchId);
        String traceId = formattedDate + generateSequentialNumber();
        customsFdapnSubmit.setTraceId(traceId);
        customsFdapnSubmit.setUserId(customerDetails.getUserId());
        customsFdapnSubmit.setAccountId(customerDetails.getAccountId());
        customsFdapnSubmit.setEnvelopNumber("ENV003");
        customsFdapnSubmit.setCreatedOn(new Date());
        customsFdapnSubmit.setUpdatedOn(new Date());
        customsFdapnSubmit.setStatus(String.valueOf(Status.REJECTED));
        JsonNode jsonNode = JsonUtils.convertCustomerDetailsToJson(customerDetails);
        customsFdapnSubmit.setRequestJson(jsonNode);
        JsonNode saveResponse = convertResponseToJson(getResponse(excelResponse, false));
        customsFdapnSubmit.setResponseJson(saveResponse);
        CustomsFdapnSubmit record = transactionRepository.saveTransaction(customsFdapnSubmit);
        CustomerFdaPnFailure dto = new CustomerFdaPnFailure();
        dto.setBatchId(record.getBatchId());
        dto.setUserId(record.getUserId());
        dto.setReferenceIdentifierNo(record.getReferenceId());
        dto.setCreatedOn(DateUtils.formatDate(record.getCreatedOn()));
        dto.setStatus(record.getStatus());
        dto.setResponseJson(getResponse(excelResponse, false));
        dto.setRequestJson(convertJsonNodeToCustomerDetails(record.getRequestJson()));
        return dto;
    }
    private SuccessOrFailureResponse getResponse(ExcelResponse excelResponse,boolean isSuccess){
        SuccessOrFailureResponse response = new SuccessOrFailureResponse();
        String messageCode = isSuccess ? MessageCode.SUCCESS_SUBMIT.getCode() : MessageCode.VALIDATION_ERRORS.getCode();
        String status = isSuccess ? MessageCode.SUCCESS_SUBMIT.getStatus() : MessageCode.VALIDATION_ERRORS.getStatus();
        String envelopNumber = isSuccess ? "ENV001" : "ENV003";
        response.setMessageCode(messageCode);
        response.setStatus(status);
        response.setEnvelopNumber(envelopNumber);
        if (!isSuccess) {
            JsonNode validationError = convertValidationErrorListToJson(excelResponse.getValidationErrors());
            response.setMessage(validationError);
        }
        return response;
    }
    private String generateSequentialNumber() {
        return String.valueOf(sequentialNumber.getAndIncrement());
    }

    public CustomsFdapnSubmit getFdaPn(String referenceId) {
        CustomsFdapnSubmit customsFdapnSubmit = transactionRepository.fetchTransaction(referenceId);
        if (isNull(customsFdapnSubmit)) {
            throw new RecordNotFoundException("Record not found for referenceId = " + referenceId);
        }
        return customsFdapnSubmit;
    }

    private List<CustomsFdaPnSubmitDTO> mapCustomsFdaPnSubmitsToDTOs(List<CustomsFdapnSubmit> customsFdaPnSubmits){
        List<CustomsFdaPnSubmitDTO> customsFdaPnSubmitDTOs = new ArrayList<>();
        for (CustomsFdapnSubmit record : customsFdaPnSubmits) {
            CustomsFdaPnSubmitDTO dto = new CustomsFdaPnSubmitDTO();
            dto.setBatchId(record.getBatchId());
            dto.setTraceId(record.getTraceId());
            dto.setUserId(record.getUserId());
            dto.setAccountId(record.getAccountId());
            dto.setReferenceId(record.getReferenceId());
            dto.setEnvelopNumber(record.getEnvelopNumber());
            dto.setCreatedOn(DateUtils.formatDate(record.getCreatedOn()));
            dto.setUpdatedOn(DateUtils.formatDate(record.getUpdatedOn()));
            dto.setStatus(record.getStatus());
            dto.setRequestJson(convertJsonNodeToCustomerDetails(record.getRequestJson()));
            dto.setResponseJson((record.getResponseJson()));
            customsFdaPnSubmitDTOs.add(dto);
        }
        return customsFdaPnSubmitDTOs;
    }

}