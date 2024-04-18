package com.customs.network.fdapn.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.customs.network.fdapn.dto.*;
import com.customs.network.fdapn.exception.ErrorResCodes;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.model.*;
import com.customs.network.fdapn.repository.TransactionRepository;
import com.customs.network.fdapn.utils.CustomIdGenerator;
import com.customs.network.fdapn.utils.DateUtils;
import com.customs.network.fdapn.utils.JsonUtils;
import com.customs.network.fdapn.utils.UtilMethods;
import com.fasterxml.jackson.databind.JsonNode;
import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.customs.network.fdapn.model.MessageCode.SUCCESS_SUBMIT;
import static com.customs.network.fdapn.utils.JsonUtils.*;
import static java.util.Objects.isNull;


@Component
@AllArgsConstructor
@Slf4j
public class FdaPnRecordSaver {
    private final TransactionRepository transactionRepository;
    private final AtomicInteger sequentialNumber = new AtomicInteger(0);
    private final CustomIdGenerator idGenerator;
    private final UtilMethods utilMethods;
    private final AmazonS3 s3Client;

    private final Lock lock = new ReentrantLock();
    @Transactional
    public List<CustomsFdapnSubmit> save(List<ExcelResponse> excelResponseList)  {

        List<CustomsFdapnSubmit> list = excelResponseList.stream()
                .filter(Objects::nonNull)
                .map(excelResponse -> {
                    TrackingDetails customerDetails = excelResponse.getTrackingDetails();
                    if (customerDetails == null) {
                        throw new FdapnCustomExceptions(ErrorResCodes.EMPTY_DETAILS, "CustomerDetails cannot be null");
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
                    JsonNode jsonNode = convertCustomerDetailsToJson(customerDetails);
                    customsFdapnSubmit.setRequestJson(jsonNode);
                    JsonNode response = convertResponseToJson(getResponse(excelResponse, true));
                    customsFdapnSubmit.setResponseJson(response);
                    return customsFdapnSubmit;
                }).toList();

        List<CustomsFdapnSubmit> records;
        lock.lock();
        try {
             records = transactionRepository.saveTransaction(list);
        } finally {
            lock.unlock();
        }

        return records;
    }

    public void hitCbp(String xmlData) {
        //CBP
        String referenceId = extractReferenceFromXml(xmlData);
        assert referenceId != null;
        Long sNo=idGenerator.parseIdFromRefId(referenceId);
        if(sNo%17==0){
            saveToS3(xmlData);
        }
    }
    private void saveToS3(String ediContent) {
        String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String userId = extractUserIdFromXml(ediContent);
        String referenceId = extractReferenceFromXml(ediContent);
        String folderKey = currentDate + "/" + "fdapn_" + userId + "/";
        String key = folderKey + referenceId + ".txt" ;
        transactionRepository.changeTransactionStatus(referenceId, MessageCode.CBP_DOWN.getStatus());
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
    public List<String> getTextFilesInFolder(String folderKey) {
        if(StringUtils.isNotBlank(folderKey)){
            folderKey = utilMethods.getFormattedDate(folderKey);
        }else {
            folderKey = utilMethods.getFormattedDate();
        }
        List<String> textFiles = new ArrayList<>();
        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName("fdapn-submit-cbp-down-records")
                .withPrefix(folderKey);

        ListObjectsV2Result result;
        do {
            result = s3Client.listObjectsV2(request);

            for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                String key = objectSummary.getKey();
                if (key.endsWith(".txt")) {
                    textFiles.add(key);
                }
            }
            request.setContinuationToken(result.getNextContinuationToken());
        } while (result.isTruncated());
        textFiles.forEach(txt->{
            String refId  = txt.substring(txt.lastIndexOf("/") + 1, txt.lastIndexOf("."));
            transactionRepository.changeTransactionStatus(refId, MessageCode.SUCCESS_SUBMIT.getStatus());
            s3Client.deleteObject(new DeleteObjectRequest("fdapn-submit-cbp-down-records", txt));
        });
        return textFiles;
    }
    public List<String> getFoldersInBucket(String bucketName) {
        List<String> folderKeys = new ArrayList<>();
        ListObjectsV2Request request = new ListObjectsV2Request().withBucketName(bucketName).withDelimiter("/");
        ListObjectsV2Result result;
        do {
            result = s3Client.listObjectsV2(request);
            folderKeys.addAll(result.getCommonPrefixes());
            request.setContinuationToken(result.getNextContinuationToken());
        } while (result.isTruncated());
        return folderKeys;
    }
    private String extractUserIdFromXml(String xmlContent) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlContent.getBytes()));
            Element root = doc.getDocumentElement();
            NodeList userIdNodeList = root.getElementsByTagName("userId");
            if (userIdNodeList.getLength() > 0) {
                return ((Element) userIdNodeList.item(0)).getTextContent();
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private String extractReferenceFromXml(String xmlContent) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlContent.getBytes()));
            Element root = doc.getDocumentElement();
            NodeList userIdNodeList = root.getElementsByTagName("referenceIdentifierNo");
            if (userIdNodeList.getLength() > 0) {
                return ((Element) userIdNodeList.item(0)).getTextContent();
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Transactional
    public List<CustomerFdaPnFailure> failureRecords(List<ExcelResponse> excelResponseList) {
        List<CustomerFdaPnFailure> fdaPnFailures=new ArrayList<>();
        List<CustomsFdapnSubmit> failedList = excelResponseList.stream()
                .filter(Objects::nonNull)
                .map(
                        excelResponse -> {
                            TrackingDetails customerDetails = excelResponse.getTrackingDetails();
                            if (isNull(customerDetails) || StringUtils.isBlank(customerDetails.getUserId())) {
                                throw new FdapnCustomExceptions(ErrorResCodes.EMPTY_DETAILS, isNull(customerDetails) ? "CustomerDetails cannot be null" : "User ID cannot be null or empty");
                            }
                            CustomerFdaPnFailure dto = new CustomerFdaPnFailure();
                            dto.setResponseJson(getResponse(excelResponse,false));
                            fdaPnFailures.add(dto);
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
                            JsonNode jsonNode = convertCustomerDetailsToJson(customerDetails);
                            customsFdapnSubmit.setRequestJson(jsonNode);
                            JsonNode saveResponse = convertResponseToJson(getResponse(excelResponse, false));
                            customsFdapnSubmit.setResponseJson(saveResponse);
                            return customsFdapnSubmit;
                        }
                ).toList();

        List<CustomsFdapnSubmit> records=null;
        lock.lock();
        try {
            records = transactionRepository.saveTransaction(failedList);
        } finally {
            lock.unlock();
        }
        List<CustomsFdapnSubmit> finalRecords = records;

        assert fdaPnFailures != null;
        return IntStream.range(0, Math.min(fdaPnFailures.size(), records.size()))
                .mapToObj(i -> {
                    CustomerFdaPnFailure dto = fdaPnFailures.get(i);
                    CustomsFdapnSubmit record = finalRecords.get(i);
                    dto.setBatchId(record.getBatchId());
                    dto.setUserId(record.getUserId());
                    dto.setReferenceIdentifierNo(record.getReferenceId());
                    dto.setCreatedOn(DateUtils.formatDate(record.getCreatedOn()));
                    dto.setStatus(record.getStatus());
                    dto.setRequestJson(convertJsonNodeToCustomerDetails(record.getRequestJson()));
                    return dto;
                })
                .collect(Collectors.toList());
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