package com.customs.network.fdapn.controller;

import com.customs.network.fdapn.dto.*;
import com.customs.network.fdapn.exception.ErrorResCodes;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.model.TransactionInfo;
import com.customs.network.fdapn.orchestrator.TransactionOrchestrator;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

import static com.customs.network.fdapn.utils.ExcelStructureVerifier.isExcelFile;


@RestController
@RequestMapping("/convert")
@Slf4j
@AllArgsConstructor
@CrossOrigin("http://localhost:5173")
public class ConverterController {
    private final TransactionOrchestrator orchestrator;

    @PostMapping("/process-excel")
    public String convertExcelToXml(@RequestParam("file") MultipartFile file) {
        if(!isExcelFile(file)){
            throw new FdapnCustomExceptions(ErrorResCodes.UNSUPPORTED_FILE, "Only excel files are supported");
        }
        if (file.isEmpty()) {
            throw new FdapnCustomExceptions(ErrorResCodes.INVALID_DETAILS, "File is null or empty");
        }
        return orchestrator.processExcel(file);
    }
    @PostMapping("/process-request-json")
    public String processRequestJson(@RequestBody JsonNode requestJson){
        return orchestrator.processRequestJson(requestJson);
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