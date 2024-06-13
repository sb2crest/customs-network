package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.TransactionFailureResponse;
import com.customs.network.fdapn.utils.UtilMethods;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class ExcelWriter {
    private final ObjectMapper objectMapper;
    private final MailService mailService;
    private final UtilMethods utilMethods;

    public ExcelWriter(ObjectMapper objectMapper, MailService mailService, UtilMethods utilMethods) {
        this.objectMapper = objectMapper;
        this.mailService = mailService;
        this.utilMethods = utilMethods;
    }

    public void writeExcel(List<TransactionFailureResponse> failures) {
        long start = System.currentTimeMillis();
        String userId = failures.get(0).getUniqueUserIdentifier();
        log.info("Creating validation error report for customer {} started", userId);
        List<String> recipients = utilMethods.getNotificationEmailsByUserIdentifier(userId);
        if (recipients.isEmpty()) {
            log.info("No notification email found for customer {}", userId);
            recipients = Collections.singletonList(utilMethods.getEmailByUserIdentifier(userId));
            log.info("Fetched Registered mail :{}, for further processing", recipients);
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
            for (TransactionFailureResponse failure : failures) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(failure.getRequestInfo().getSlNo());
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
            long end = System.currentTimeMillis();
            log.info("Creating validation error report for customer {} completed in {} seconds", userId, (end - start) / 1000);
            mailService.sendEmailWithAttachment(out.toByteArray(), recipients);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private String prettyPrintJson(JsonNode json) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
        }
        return json.toString();
    }
}
