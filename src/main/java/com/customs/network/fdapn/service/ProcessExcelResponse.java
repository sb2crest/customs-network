package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.CustomerFdaPnFailure;
import com.customs.network.fdapn.dto.UserProductInfoDto;
import com.customs.network.fdapn.dto.ValidationResponse;
import com.customs.network.fdapn.exception.BatchInsertionException;

import com.customs.network.fdapn.service.impl.UserProductInfoServicesImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@Slf4j
public class ProcessExcelResponse {
    private final UserProductInfoServicesImpl userInfoServices;
    private final FdapnRecordProcessor fdapnRecordProcessor;
    private static final List<String> ediFileList = new ArrayList<>();

    public ProcessExcelResponse(UserProductInfoServicesImpl userInfoServices, FdapnRecordProcessor fdapnRecordProcessor) {
        this.userInfoServices = userInfoServices;
        this.fdapnRecordProcessor = fdapnRecordProcessor;
    }

    public List<ValidationResponse> processExcelData(List<ValidationResponse> validationResponseList) throws BatchInsertionException {
        List<ValidationResponse> successfulTransaction = new ArrayList<>();
        List<ValidationResponse> failedTransaction = new ArrayList<>();
        validationResponseList.stream()
                .filter(Objects::nonNull)
                .forEach(obj -> {
                    Map<Boolean, List<UserProductInfoDto>> booleanListMap = userInfoServices.fetchAllProducts(obj.getExcelTransactionInfo().getProductCode(),
                            obj.getExcelTransactionInfo().getUniqueUserIdentifier());
                    if (booleanListMap.size() == 1 && booleanListMap.containsKey(true) && obj.getValidationErrorList().isEmpty()) {
                        obj.getExcelTransactionInfo().getPriorNoticeData().setProducts(new ArrayList<>());
                        successfulTransaction.add(obj);
                    } else {
                        failedTransaction.add(obj);
                    }
                });
        log.info("Number of edi files generated : {}", ediFileList.size());
        ediFileList.clear();
        if (!successfulTransaction.isEmpty()) {
            fdapnRecordProcessor.saveSuccessInfo(successfulTransaction);
            successfulTransaction.clear();
        }
        if (!failedTransaction.isEmpty()) {
            List<CustomerFdaPnFailure> failures = fdapnRecordProcessor.failureRecords(failedTransaction);
        }

        return failedTransaction;
    }
}
