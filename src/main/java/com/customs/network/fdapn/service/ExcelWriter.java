package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.CustomerFdaPnFailure;
import com.customs.network.fdapn.utils.UtilMethods;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Service
public class ExcelWriter {
    private final ObjectMapper objectMapper;
    private final MailService mailService;
    private final UtilMethods utilMethods;

    public ExcelWriter(ObjectMapper objectMapper, MailService mailService, UtilMethods utilMethods) {
        this.objectMapper = objectMapper;
        this.mailService = mailService;
        this.utilMethods = utilMethods;
    }

    public void writeExcel(List<CustomerFdaPnFailure> failures) {
        String userId = failures.get(0).getUserId();
        List<String> recipients=utilMethods.getNotificationEmailsByUserIdentifier(userId);
        if(recipients.isEmpty()){
            recipients= Collections.singletonList(utilMethods.getEmailByUserIdentifier(userId));
        }

        try (Workbook workbook = new SXSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            SXSSFSheet sheet = (SXSSFSheet) workbook.createSheet("Failures");
            sheet.trackAllColumnsForAutoSizing();
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("SlNo");
            headerRow.createCell(1).setCellValue("BatchId");
            headerRow.createCell(2).setCellValue("ReferenceId");
            headerRow.createCell(3).setCellValue("ValidationErrors");

            int rowNum = 1;
            for (CustomerFdaPnFailure failure : failures) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(failure.getRequestJson().getSNo());
                row.createCell(1).setCellValue(failure.getBatchId());
                row.createCell(2).setCellValue(failure.getReferenceIdentifierNo());

                JsonNode jsonNode = failure.getResponseJson().getMessage();
                String prettyPrintedJson = prettyPrintJson(jsonNode);
                row.createCell(3).setCellValue(prettyPrintedJson);
            }

            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            mailService.sendEmailWithAttachment(out.toByteArray(),recipients);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String prettyPrintJson(JsonNode json) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
}
