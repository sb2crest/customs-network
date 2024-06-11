package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.ExcelValidationResponse;
import com.customs.network.fdapn.exception.BatchInsertionException;

import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.service.impl.UserProductInfoServicesImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@Slf4j
public class TransactionSegregator {
    private final UserProductInfoServicesImpl userInfoServices;
    private final FdapnRecordProcessor fdapnRecordProcessor;

    public TransactionSegregator(UserProductInfoServicesImpl userInfoServices, FdapnRecordProcessor fdapnRecordProcessor) {
        this.userInfoServices = userInfoServices;
        this.fdapnRecordProcessor = fdapnRecordProcessor;
    }

    public List<ExcelValidationResponse> segregateExcelResponse(List<ExcelValidationResponse> excelValidationResponseList) throws BatchInsertionException {
        List<ExcelValidationResponse> successfulTransaction = new ArrayList<>();
        List<ExcelValidationResponse> failedTransaction = new ArrayList<>();
        excelValidationResponseList.stream()
                .filter(Objects::nonNull)
                .forEach(obj -> {
                    List<String> productCodes=obj.getExcelTransactionInfo().getProductCode();
                    String uniqueUserIdentifier=obj.getExcelTransactionInfo().getUniqueUserIdentifier();
                    List<ValidationError> productValidationErrors=userInfoServices.getProductValidationErrors(productCodes,uniqueUserIdentifier);
                    if (productValidationErrors.isEmpty() && obj.getValidationErrorList().isEmpty()) {
                        successfulTransaction.add(obj);
                    } else {
                        obj.getValidationErrorList().addAll(productValidationErrors);
                        failedTransaction.add(obj);
                    }
                });
        if (!successfulTransaction.isEmpty()) {
            fdapnRecordProcessor.saveSuccessInfo(successfulTransaction);
            successfulTransaction.clear();
        }
        if (!failedTransaction.isEmpty()) {
            fdapnRecordProcessor.failureRecords(failedTransaction);
        }
        return failedTransaction;
    }
}
