package com.customs.network.fdapn.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.customs.network.fdapn.dto.*;
import com.customs.network.fdapn.exception.ErrorResCodes;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
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

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.customs.network.fdapn.model.MessageCode.SUCCESS_SUBMIT;
import static com.customs.network.fdapn.utils.JsonUtils.*;
import static java.util.Objects.isNull;


@Component
@AllArgsConstructor
@Slf4j
public class FdaPnRecordSaver {
    private final TransactionRepository transactionRepository;
    private final AtomicInteger sequentialNumber = new AtomicInteger(0);
//    @Value("${aws.bucketName}")
//    private String s3BucketName;
//    @Value("${aws.region}")
//    private String s3Region;

    @Transactional
    public void save(ExcelResponse excelResponse) {
        TrackingDetails customerDetails = excelResponse.getTrackingDetails();
        if (customerDetails == null) {
            throw new FdapnCustomExceptions(ErrorResCodes.EMPTY_DETAILS,"CustomerDetails cannot be null");
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
        customsFdapnSubmit.setStatus(SUCCESS_SUBMIT.getStatus());
        JsonNode jsonNode = JsonUtils.convertCustomerDetailsToJson(customerDetails);
        customsFdapnSubmit.setRequestJson(jsonNode);
        JsonNode response = JsonUtils.convertResponseToJson(getResponse(excelResponse,true));
        customsFdapnSubmit.setResponseJson(response);
        CustomsFdapnSubmit record = transactionRepository.saveTransaction(customsFdapnSubmit);
        excelResponse.getTrackingDetails().setReferenceIdentifierNo(record.getReferenceId());
        log.info("submit saved in Data base : {}", customsFdapnSubmit);
        hitCbp(record);

    }
    public void hitCbp(CustomsFdapnSubmit customsFdapnSubmit) {
        String convertToTxt = null;
        try {
            convertToTxt = convertToEdi(customsFdapnSubmit);
            throw new NullPointerException("Simulated NullPointerException while hitting CBP");//cbp down
        } catch (Exception e) {
            log.error("Exception occurred while hitting CBP: " + e.getMessage());
            assert convertToTxt != null;
            saveToS3(convertToTxt,customsFdapnSubmit.getUserId());
        }
    }
    private void saveToS3(String ediContent, String userId) {
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion("ap-south-1")
                .build();

        String currentDateFolder = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String folderKey = currentDateFolder + "/fdapn_" + userId + "/";
        String key = folderKey + "customs_fdaPn_submit_" + userId + ".txt";
        try {
            byte[] contentBytes = ediContent.getBytes();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(contentBytes.length);
            s3Client.putObject(new PutObjectRequest("fdapn-submit-cbp-down-records", key, new ByteArrayInputStream(contentBytes), metadata));
            System.out.println("EDI content saved to S3 bucket: " + "fdapn-submit-cbp-down-records" + "/" + key);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error occurred while saving EDI content to S3: " + e.getMessage());
        }
    }
    public static String convertToEdi(CustomsFdapnSubmit obj) {
        return "SLNO:" + obj.getSlNo() + "|" +
                "BATCH_ID:" + obj.getBatchId() + "|" +
                "TRACE_ID:" + obj.getTraceId() + "|" +
                "USER_ID:" + obj.getUserId() + "|" +
                "ACCOUNT_ID:" + obj.getAccountId() + "|" +
                "REFERENCE_ID:" + obj.getReferenceId() + "|" +
                "ENVELOP_NUMBER:" + obj.getEnvelopNumber() + "|" +
                "CREATED_ON:" + formatDate(obj.getCreatedOn()) + "|" +
                "UPDATED_ON:" + formatDate(obj.getUpdatedOn()) + "|" +
                "STATUS:" + obj.getStatus() + "|" +
                "REQUEST_JSON:" + obj.getRequestJson().toString() + "|" +
                "RESPONSE_JSON:" + obj.getResponseJson().toString() + "|";
    }
    private static String formatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return dateFormat.format(date);
    }
    @Transactional
    public CustomerFdaPnFailure failureRecords(ExcelResponse excelResponse) {
        TrackingDetails customerDetails = excelResponse.getTrackingDetails();
        if (isNull(customerDetails) || StringUtils.isBlank(customerDetails.getUserId())) {
            throw new FdapnCustomExceptions(ErrorResCodes.EMPTY_DETAILS,isNull(customerDetails) ? "CustomerDetails cannot be null" : "User ID cannot be null or empty");
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
        customsFdapnSubmit.setStatus(MessageCode.VALIDATION_ERRORS.getStatus());
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
            throw new FdapnCustomExceptions(ErrorResCodes.INVALID_REFERENCE_ID,"Invalid Reference id "+referenceId);
        }
        return customsFdapnSubmit;
    }

}