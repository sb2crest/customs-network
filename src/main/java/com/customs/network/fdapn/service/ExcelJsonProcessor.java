package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.CustomerFdaPnFailure;
import com.customs.network.fdapn.dto.ExcelResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBException;
import java.util.*;

import static com.customs.network.fdapn.model.MessageCode.*;

@Component
@AllArgsConstructor
public class ExcelJsonProcessor {
    private final FdaPnRecordSaver fdaPnRecordSaver;
    public Map<String, List<Object>> processResponses(List<ExcelResponse> excelResponses) {
        Map<String, List<Object>> result = new HashMap<>();
        result.put(SUCCESS_SUBMIT.getStatus(), new ArrayList<>());
        result.put(REJECT.getStatus(), new ArrayList<>());
        result.put(INVALID_USER.getStatus(), new ArrayList<>());
        result.put(PENDING.getStatus(), new ArrayList<>());

        excelResponses.stream().filter(Objects::nonNull).forEach(excelResponse -> {
            if (excelResponse.getValidationErrors().isEmpty()) {
                fdaPnRecordSaver.save(excelResponse);
                try {
                    result.get(SUCCESS_SUBMIT.getStatus()).add(XmlConverterService.convertToXml(excelResponse.getTrackingDetails()));
                } catch (JAXBException e) {
                    throw new RuntimeException(e);
                }
            } else {
                boolean userIdExists = excelResponse.getValidationErrors().stream()
                        .anyMatch(error -> "User ID".equals(error.getFieldName()));
                CustomerFdaPnFailure customerFdaPnFailure = null;
                if (!userIdExists) {
                    customerFdaPnFailure = fdaPnRecordSaver.failureRecords(excelResponse);
                    result.get(REJECT.getStatus()).add(customerFdaPnFailure);
                }else {
                    result.get(INVALID_USER.getStatus()).add(excelResponse);
                }

            }
        });
        return result;
    }

}
