package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.ExcelResponse;
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
    public Map<String, List<Object>> processResponses(List<ExcelResponse> excelResponses) {
        Map<String, List<Object>> result = new HashMap<>();
        result.put(SUCCESS_SUBMIT.getStatus(), new ArrayList<>());
        result.put(VALIDATION_ERRORS.getStatus(), new ArrayList<>());
        result.put(INVALID_USER.getStatus(), new ArrayList<>());

        List<ExcelResponse> successList = new ArrayList<>();
        List<ExcelResponse> failedList=new ArrayList<>();
        List<ExcelResponse> incorrectUserIdList=new ArrayList<>();

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
            result.get(SUCCESS_SUBMIT.getStatus()).add(fdaPnRecordSaver.save(successList));
        }
        if(!failedList.isEmpty()){
            result.get(VALIDATION_ERRORS.getStatus()).add(fdaPnRecordSaver.failureRecords(failedList));
        }
        if(!incorrectUserIdList.isEmpty()){
            result.get(INVALID_USER.getStatus()).add(incorrectUserIdList);
        }

        return result;
    }
}
