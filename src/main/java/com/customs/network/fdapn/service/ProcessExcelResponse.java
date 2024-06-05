package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.CustomerFdaPnFailure;
import com.customs.network.fdapn.dto.ExcelTransactionInfo;
import com.customs.network.fdapn.dto.UserProductInfoDto;
import com.customs.network.fdapn.dto.ValidationResponse;
import com.customs.network.fdapn.exception.BatchInsertionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class ProcessExcelResponse {
    private final UserProductInfoServicesImpl userInfoServices;
    private final FdapnRecordProcessor fdapnRecordProcessor;
    public ProcessExcelResponse(UserProductInfoServicesImpl userInfoServices, FdapnRecordProcessor fdapnRecordProcessor) {
        this.userInfoServices = userInfoServices;
        this.fdapnRecordProcessor = fdapnRecordProcessor;
    }

    public List<ValidationResponse> processExcelData(List<ValidationResponse> validationResponseList) throws BatchInsertionException {
        List<ValidationResponse> successfulTransaction=new ArrayList<>();
        List<ValidationResponse> failedTransaction=new ArrayList<>();
        validationResponseList.stream()
                .filter(Objects::nonNull)
               .forEach(obj->{
                   Map<Boolean, List<UserProductInfoDto>> booleanListMap = fetchAllProducts(obj.getExcelTransactionInfo().getProductCode(),
                           obj.getExcelTransactionInfo().getUniqueUserIdentifier());
                   if (booleanListMap.size() == 1 && booleanListMap.containsKey(true) && obj.getValidationErrorList().isEmpty()) {
                       obj.getExcelTransactionInfo().getPriorNoticeData().setProducts(booleanListMap.get(true).stream()
                               .map(UserProductInfoDto::getProductInfo).toList());
                       successfulTransaction.add(obj);
                   }else {
                       failedTransaction.add(obj);
                   }
               });
        if(!successfulTransaction.isEmpty()){
            fdapnRecordProcessor.saveSuccessInfo(successfulTransaction);
        }
        if (!failedTransaction.isEmpty()) {
            List<CustomerFdaPnFailure> failures = fdapnRecordProcessor.failureRecords(failedTransaction);
        }
            
        return failedTransaction;
    }

    private Map<Boolean, List<UserProductInfoDto>> fetchAllProducts(List<String> productCodes, String uniqueUserIdentifier) {
        Map<Boolean, List<UserProductInfoDto>> allProducts = new HashMap<>();
        List<UserProductInfoDto> validProducts = new ArrayList<>();
        List<UserProductInfoDto> invalidProducts = new ArrayList<>();
        productCodes.stream()
                .filter(Objects::nonNull)
                .forEach(obj -> {
                    UserProductInfoDto userProductInfoDto = userInfoServices.getProductByProductCode(uniqueUserIdentifier, obj);
                    if (userProductInfoDto.isValid()) {
                        validProducts.add(userProductInfoDto);
                    } else {
                        invalidProducts.add(userProductInfoDto);
                    }
                });
        if (!invalidProducts.isEmpty()) {
            allProducts.put(false, invalidProducts);
            return allProducts;
        }
        allProducts.put(true, validProducts);
        return allProducts;
    }
}
