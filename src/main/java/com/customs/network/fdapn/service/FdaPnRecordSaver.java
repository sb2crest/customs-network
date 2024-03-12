package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.*;
import com.customs.network.fdapn.exception.NotFoundException;
import com.customs.network.fdapn.exception.RecordNotFoundException;
import com.customs.network.fdapn.model.*;
import com.customs.network.fdapn.repository.CustomsFdapnSubmitRepository;
import com.customs.network.fdapn.utils.DateUtils;
import com.customs.network.fdapn.utils.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final CustomsFdapnSubmitRepository customsFdapnSubmitRepository;
    private final AtomicInteger sequentialNumber = new AtomicInteger(0);

    @Transactional
    public void save(ExcelResponse excelResponse) {
        CustomerDetails customerDetails = excelResponse.getCustomerDetails();
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
        customsFdapnSubmit.setReferenceId(customerDetails.getReferenceIdentifierNo());
        customsFdapnSubmit.setEnvelopNumber("ENV001");
        customsFdapnSubmit.setCreatedOn(new Date());
        customsFdapnSubmit.setUpdatedOn(new Date());
        customsFdapnSubmit.setStatus(String.valueOf(Status.SUCCESS));
        JsonNode jsonNode = JsonUtils.convertCustomerDetailsToJson(customerDetails);
        customsFdapnSubmit.setRequestJson(jsonNode);
        JsonNode response = JsonUtils.convertResponseToJson(getResponse(excelResponse,true));
        customsFdapnSubmit.setResponseJson(response);
        customsFdapnSubmitRepository.save(customsFdapnSubmit);
        log.info("submit saved in Data base : {}", customsFdapnSubmit);
    }
    public CustomerFdaPnFailure failureRecords(ExcelResponse excelResponse) {
        CustomerDetails customerDetails = excelResponse.getCustomerDetails();
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
        customsFdapnSubmit.setReferenceId(customerDetails.getReferenceIdentifierNo());
        customsFdapnSubmit.setEnvelopNumber("ENV003");
        customsFdapnSubmit.setCreatedOn(new Date());
        customsFdapnSubmit.setUpdatedOn(new Date());
        customsFdapnSubmit.setStatus(String.valueOf(Status.FAILED));
        JsonNode jsonNode = JsonUtils.convertCustomerDetailsToJson(customerDetails);
        customsFdapnSubmit.setRequestJson(jsonNode);
        JsonNode saveResponse = convertResponseToJson(getResponse(excelResponse, false));
        customsFdapnSubmit.setResponseJson(saveResponse);
        CustomsFdapnSubmit record = customsFdapnSubmitRepository.save(customsFdapnSubmit);
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

    public List<CustomsFdaPnSubmitDTO> getFdaPn(Date createdOn, String referenceId) {
        List<CustomsFdaPnSubmitDTO> customsFdaPnSubmitDTOList = new ArrayList<>();
        List<CustomsFdapnSubmit> records = customsFdapnSubmitRepository.findByCreatedOnAndReferenceId(createdOn, referenceId);
        if (isNull(records)) {
            throw new RecordNotFoundException("Record not found for createdOn=" + createdOn + " and referenceId=" + referenceId);
        }
        records.stream()
                .filter(Objects::nonNull)
                .forEach(record -> {
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
                    dto.setResponseJson(record.getResponseJson());
                    customsFdaPnSubmitDTOList.add(dto);
                });
        return customsFdaPnSubmitDTOList;
    }
    public PageDTO<CustomsFdaPnSubmitDTO> filterByCriteria(Date createdOn, String status, String referenceId, String userId, Pageable pageable) {
        try {
            Page<CustomsFdapnSubmit> page = customsFdapnSubmitRepository.findAll((Root<CustomsFdapnSubmit> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();
                if (createdOn != null) {
                    Calendar startDate = new GregorianCalendar();
                    startDate.setTime(createdOn);
                    startDate.set(Calendar.HOUR_OF_DAY, 0);
                    startDate.set(Calendar.MINUTE, 0);
                    startDate.set(Calendar.SECOND, 0);
                    Calendar endDate = new GregorianCalendar();
                    endDate.setTime(createdOn);
                    endDate.set(Calendar.HOUR_OF_DAY, 23);
                    endDate.set(Calendar.MINUTE, 59);
                    endDate.set(Calendar.SECOND, 59);

                    predicates.add(criteriaBuilder.between(root.get("createdOn"), startDate.getTime(), endDate.getTime()));
                }
                if (StringUtils.isNotBlank(status)) {
                    predicates.add(criteriaBuilder.equal(root.get("status"), status));
                }
                if (StringUtils.isNotBlank(referenceId)) {
                    predicates.add(criteriaBuilder.equal(root.get("referenceId"), referenceId));
                }
                if (StringUtils.isNotBlank(userId)) {
                    predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }, pageable);

            List<CustomsFdapnSubmit> results = page.getContent();
            List<CustomsFdaPnSubmitDTO> data = mapCustomsFdaPnSubmitsToDTOs(results);

            PageDTO<CustomsFdaPnSubmitDTO> pageDTO = new PageDTO<>();
            pageDTO.setPage(page.getNumber());
            pageDTO.setPageSize(page.getSize());
            pageDTO.setTotalRecords(page.getTotalElements());
            pageDTO.setData(data);

            return pageDTO;
        } catch (Exception e) {
            log.error("Error occurred while filtering records");
            throw new RuntimeException("Error occurred while filtering records: " + e.getMessage(), e);
        }
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

    public PageDTO<CustomsFdaPnSubmitDTO> getAllByUserId(String userId, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdOn");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CustomsFdapnSubmit> pageRecords = customsFdapnSubmitRepository.findByUserId(userId, pageable);
        if (pageRecords.isEmpty()) {
            throw new RecordNotFoundException("No records found for userId " + userId + " on page " + page);
        }
        List<CustomsFdaPnSubmitDTO> dtos = mapCustomsFdaPnSubmitsToDTOs(pageRecords.getContent());
        PageDTO<CustomsFdaPnSubmitDTO> pageDTO = new PageDTO<>();
        pageDTO.setPage(page);
        pageDTO.setPageSize(size);
        pageDTO.setTotalRecords(pageRecords.getTotalElements());
        pageDTO.setData(dtos);
        return pageDTO;
    }

}