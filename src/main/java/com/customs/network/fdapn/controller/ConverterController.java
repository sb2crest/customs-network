package com.customs.network.fdapn.controller;

import com.customs.network.fdapn.dto.FilterCriteriaDTO;
import com.customs.network.fdapn.dto.PageDTO;
import com.customs.network.fdapn.dto.ScanSchema;
import com.customs.network.fdapn.model.CustomsFdapnSubmit;
import com.customs.network.fdapn.model.TrackingDetails;
import com.customs.network.fdapn.repository.TransactionRepository;
import com.customs.network.fdapn.service.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/convert")
@Slf4j
@AllArgsConstructor
@CrossOrigin("http://localhost:5173")
public class ConverterController {
    private final ExcelReaderService excelReaderService;
    private final FdaPnRecordSaver fdaPnRecordSaver;
    private final JsonToXmlService jsonToXmlService;
    private final TransactionRepository service;

    @PostMapping("/excel-to-xml")
    public Map<String, List<Object>> convertExcelToXml(@RequestParam("file") MultipartFile file) {
       return excelReaderService.processExcelFile(file);
    }
    @PostMapping("/json-to-xml")
    public Map<String, List<Object>> convertXmlFromJson(@RequestBody List<TrackingDetails> trackingDetails) {
        return jsonToXmlService.convertJsonToXml(trackingDetails);
    }

    @PostMapping("/json-file-to-xml")
    public Map<String, List<Object>> convertJsonToXml(@RequestParam("file") MultipartFile file) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<List<TrackingDetails>> typeReference = new TypeReference<List<TrackingDetails>>() {};
        List<TrackingDetails> trackingDetailsList = objectMapper.readValue(file.getInputStream(), typeReference);
        return jsonToXmlService.convertJsonToXml(trackingDetailsList);
    }
    @GetMapping("/getFdaPn-record")
    public CustomsFdapnSubmit getFdaRecordByReferenceId(@RequestParam String referenceId) {
        return fdaPnRecordSaver.getFdaPn(referenceId);
    }

    @PostMapping("/getFdaPn-records")
    public PageDTO<CustomsFdapnSubmit> filterByFdaPnRecords(@RequestBody FilterCriteriaDTO criteriaDTO) {
        return service.fetchByFilter(criteriaDTO);
    }
    @PostMapping("/fetchDataByColValue")
    public PageDTO<CustomsFdapnSubmit> fetchDataByCustomized(@RequestBody ScanSchema scan) {
        return service.scanSchemaByColValue(scan.getFieldName(), scan.getValue(), scan.getStartDate(), scan.getEndDate(), scan.getUserId(),scan.getPage(),scan.getSize());
    }
    @GetMapping("/execute")
    public List<String> getTextFilesInFolder(@RequestParam(required = false) String folderKey) {
        return fdaPnRecordSaver.getTextFilesInFolder(folderKey);
    }

}