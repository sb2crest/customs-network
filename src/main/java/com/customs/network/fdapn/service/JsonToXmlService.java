package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.ExcelResponse;
import com.customs.network.fdapn.model.TrackingDetails;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@AllArgsConstructor
public class JsonToXmlService {

    private final ValidationService validationService;
    private final ExcelJsonProcessor excelJsonProcessor;

    public Map<String, List<Object>> convertJsonToXml(List<TrackingDetails> trackingDetails) {
        try {
            List<ExcelResponse> excelResponseList = new ArrayList<>();
            for (TrackingDetails details : trackingDetails) {
                ExcelResponse excelResponse = new ExcelResponse();
                excelResponse.setTrackingDetails(details);
                excelResponseList.add(excelResponse);
            }

            List<ExcelResponse> excelResponses = validationService.validateField(excelResponseList);
            return excelJsonProcessor.processResponses(excelResponses);
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON to XML: " + e.getMessage(), e);
        }
    }
}
