package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.CustomerFdaPnFailure;
import com.customs.network.fdapn.dto.ExcelResponse;
import com.customs.network.fdapn.exception.BatchInsertionException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.*;

import static com.customs.network.fdapn.model.MessageCode.*;

@Component
@AllArgsConstructor
@Slf4j
public class ExcelJsonProcessor {
    private final FdaPnRecordSaver fdaPnRecordSaver;
    public List<CustomerFdaPnFailure> processResponses(List<ExcelResponse> excelResponses) throws BatchInsertionException {
        Map<String, List<Object>> result = new HashMap<>();
        result.put(VALIDATION_ERRORS.getStatus(), new ArrayList<>());
        result.put(INVALID_USER.getStatus(), new ArrayList<>());

        List<ExcelResponse> successList = new ArrayList<>();
        List<ExcelResponse> failedList=new ArrayList<>();
        List<ExcelResponse> incorrectUserIdList=new ArrayList<>();
        List<CustomerFdaPnFailure> failures=new ArrayList<>();

        excelResponses.stream().filter(Objects::nonNull).forEach(excelResponse -> {
            if (excelResponse.getValidationErrors().isEmpty()) {
                successList.add(excelResponse);
            } else {
                boolean userIdExists = excelResponse.getValidationErrors().stream()
                        .anyMatch(error -> "User ID".equals(error.getFieldName()));
                if (!userIdExists) {
                    failedList.add(excelResponse);
                }else {
                    incorrectUserIdList.add(excelResponse);
                }
            }
        });
        if(!successList.isEmpty()){
            fdaPnRecordSaver.save(successList);        }
        if(!failedList.isEmpty()){
             failures = fdaPnRecordSaver.failureRecords(failedList);

        }
        if(!incorrectUserIdList.isEmpty()){
            result.get(INVALID_USER.getStatus()).add(incorrectUserIdList);
        }
        return failures;
    }
}
