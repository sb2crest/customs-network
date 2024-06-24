package com.customs.network.fdapn.controller;

import com.customs.network.fdapn.dto.*;
import com.customs.network.fdapn.model.TransactionInfo;
import com.customs.network.fdapn.orchestrator.TransactionOrchestrator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;


@RestController
@RequestMapping("/convert")
@Slf4j
@AllArgsConstructor
@CrossOrigin("http://localhost:5173")
public class ConverterController {
    private final TransactionOrchestrator orchestrator;

    @PostMapping("/process-excel")
    public String convertExcelToXml(@RequestParam("file") MultipartFile file) {
        return orchestrator.processExcel(file);
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