package com.customs.network.fdapn.controller;

import com.customs.network.fdapn.dto.*;
import com.customs.network.fdapn.model.TrackingDetails;
import com.customs.network.fdapn.model.TransactionInfo;
import com.customs.network.fdapn.orchestrator.TransactionOrchestrator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/convert")
@Slf4j
@AllArgsConstructor
@CrossOrigin("http://localhost:5173")
public class ConverterController {
    private final TransactionOrchestrator orchestrator;

    @PostMapping("/excel-to-xml")
    public String convertExcelToXml(@RequestParam("file") MultipartFile file) {
        long startTime = System.currentTimeMillis();
        String result = orchestrator.processExcelFile(file);
        long endTime = System.currentTimeMillis();
        double executionTimeSeconds = (endTime - startTime) / 1000.0;
        log.info("Execution time: {} seconds", executionTimeSeconds);
        return result;
    }

    @PostMapping("/excel-to-xml-product")
    public String read(@RequestParam("file") MultipartFile file) throws Exception {
        return orchestrator.processExcel(file);
    }
    @PostMapping("/json-to-xml")
    public String convertXmlFromJson(@RequestBody List<TrackingDetails> trackingDetails) {
        return orchestrator.convertJsonToXml(trackingDetails);
    }

    @PostMapping("/json-file-to-xml")
    public String convertJsonToXml(@RequestParam("file") MultipartFile file) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<List<TrackingDetails>> typeReference = new TypeReference<>() {};
        List<TrackingDetails> trackingDetailsList = objectMapper.readValue(file.getInputStream(), typeReference);
        return orchestrator.convertJsonToXml(trackingDetailsList);
    }
    @GetMapping("/getFdaPn-record")
    public TransactionInfo getFdaRecordByReferenceId(@RequestParam String referenceId) {
        return orchestrator.getFdapnTransaction(referenceId);
    }

    @PostMapping("/getFdaPn-records")
    public PageDTO<TransactionInfo> filterByFdaPnRecords(@RequestBody FilterCriteriaDTO criteriaDTO) {
        return orchestrator.fetchByFilter(criteriaDTO);
    }
    @PostMapping("/fetchDataByColValue")
    public PageDTO<TransactionInfo> fetchDataByCustomized(@RequestBody ScanSchema scan) {
        return orchestrator.scanSchemaByColValue(scan);
    }
    @GetMapping("/execute")
    public List<String> getTextFilesInFolder(@RequestParam(required = false) String folderKey) {
        return orchestrator.getTextFilesInFolder(folderKey);
    }
    @GetMapping("/s3-folders")
    public List<String> getFoldersInBucket() {
        return orchestrator.getFoldersInBucket();
    }

}