package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.CustomerFdaPnFailure;
import com.customs.network.fdapn.dto.ExcelResponse;
import com.customs.network.fdapn.exception.ErrorResCodes;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.model.TrackingDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@Slf4j
public class JsonToXmlService {

    private final ValidationService validationService;
    private final ExcelJsonProcessor excelJsonProcessor;
    private final ExcelWriter excelWriter;

    public JsonToXmlService(ValidationService validationService, ExcelJsonProcessor excelJsonProcessor, ExcelWriter excelWriter) {
        this.validationService = validationService;
        this.excelJsonProcessor = excelJsonProcessor;
        this.excelWriter = excelWriter;
    }

    public String convertJsonToXml(List<TrackingDetails> trackingDetails) {
        try {
            List<ExcelResponse> excelResponseList = new ArrayList<>();
            for (TrackingDetails details : trackingDetails) {
                ExcelResponse excelResponse = new ExcelResponse();
                excelResponse.setTrackingDetails(details);
                excelResponseList.add(excelResponse);
            }
            List<CustomerFdaPnFailure> failures = excelJsonProcessor.processResponses(validationService.validateField(excelResponseList));
            new Thread(() -> {
                if (!excelResponseList.isEmpty()) {
                    log.info("Validation errors are found");
                   // excelWriter.writeExcel(failures);
                }
            }).start();

            return "Uploaded Json successfully";
        } catch (Exception e) {
            throw new FdapnCustomExceptions(ErrorResCodes.CONVERSION_FAILURE, "Error converting Json to XML , " + e.getMessage());
        }
    }
}
