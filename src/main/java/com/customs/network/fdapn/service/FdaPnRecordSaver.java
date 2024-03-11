package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.*;
import com.customs.network.fdapn.exception.NotFoundException;
import com.customs.network.fdapn.exception.RecordNotFoundException;
import com.customs.network.fdapn.model.*;
import com.customs.network.fdapn.repository.CustomsFdapnSubmitRepository;
import com.customs.network.fdapn.utils.DateUtils;
import com.customs.network.fdapn.utils.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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


@Component
@AllArgsConstructor
@Slf4j
public class FdaPnRecordSaver {
    private final CustomsFdapnSubmitRepository customsFdapnSubmitRepository;
    private final ObjectMapper objectMapper;
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
        if (customerDetails == null || customerDetails.getUserId() == null || customerDetails.getUserId().isEmpty()) {
            throw new NotFoundException(customerDetails == null ? "CustomerDetails cannot be null" : "User ID cannot be null or empty");
        }
        CustomsFdapnSubmit customsFdapnSubmit = new CustomsFdapnSubmit();
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
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
        dto.setResponseJson(getResponse(excelResponse, false)); //Success/FailureResponse
        dto.setRequestJson(convertJsonNodeToCustomerDetails(record.getRequestJson()));
        return dto;
    }
    private SuccessOrFailureResponse getResponse(ExcelResponse excelResponse,boolean isSuccess){
        SuccessOrFailureResponse response = new SuccessOrFailureResponse();
        if (isSuccess) {
            response.setMessageCode(MessageCode.SUCCESS_SUBMIT.getCode());
            response.setStatus(MessageCode.SUCCESS_SUBMIT.getStatus());
            response.setEnvelopNumber("ENV001");
        } else {
            response.setMessageCode(MessageCode.VALIDATION_ERRORS.getCode());
            response.setStatus(MessageCode.VALIDATION_ERRORS.getStatus());
            response.setEnvelopNumber("ENV003");
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
        if (Objects.isNull(records)) {
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
                    ObjectNode responseJsonNode = objectMapper.createObjectNode();
                    responseJsonNode.put("message", "successfully Submit to MQ..Waiting for the Response");
                    dto.setResponseJson(record.getStatus().equals(Status.SUCCESS) ? responseJsonNode : record.getResponseJson());
                    customsFdaPnSubmitDTOList.add(dto);
                });
        return customsFdaPnSubmitDTOList;
    }
    public PageDTO<CustomsFdaPnSubmitDTO> filterByCriteria(Date createdOn, String status, String referenceId,String userId, Pageable pageable) {
        Page<CustomsFdapnSubmit> page = customsFdapnSubmitRepository.findAll((Root<CustomsFdapnSubmit> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (createdOn != null) {
                predicates.add(criteriaBuilder.equal(root.get("createdOn"), createdOn));
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
        pageDTO.setData(dtos);
        pageDTO.setTotalRecords(pageRecords.getTotalElements());
        pageDTO.setPage(page);
        pageDTO.setPageSize(size);
        return pageDTO;
    }

}