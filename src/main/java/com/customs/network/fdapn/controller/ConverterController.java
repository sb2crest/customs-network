package com.customs.network.fdapn.controller;

import com.customs.network.fdapn.dto.CustomsFdaPnSubmitDTO;
import com.customs.network.fdapn.dto.ExcelResponse;
import com.customs.network.fdapn.dto.PageDTO;
import com.customs.network.fdapn.model.CustomerDetails;
import com.customs.network.fdapn.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;


@RestController
@RequestMapping("/convert")
@Slf4j
@AllArgsConstructor
@CrossOrigin("http://localhost:5173")
public class ConverterController {
    private final ExcelReaderService excelReaderService;
    private final FdaPnRecordSaver fdaPnRecordSaver;
    private final JsonToXmlService jsonToXmlService;

    @PostMapping("/excel-to-xml")
    public Object convertExcelToXml(@RequestParam("file") MultipartFile file) {
        try {
            Workbook workbook = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            log.info("sheet ->  {}", sheet);
            ExcelResponse excelResponse = excelReaderService.mapExcelToCustomerDetails(sheet);
            if(!excelResponse.getValidationErrors().isEmpty()){
                return fdaPnRecordSaver.failureRecords(excelResponse);
            }
            fdaPnRecordSaver.save(excelResponse);
            return XmlConverterService.convertToXml(excelResponse.getCustomerDetails());
        } catch (Exception e) {
            log.error("Error converting Excel to XML: -> {} ", e.getMessage());
            return "Error converting Excel to XML: " + e.getMessage();
        }
    }
    @PostMapping("/json-to-xml")
    public ResponseEntity<?> convertXmlFromJson(@RequestBody CustomerDetails customerDetails) {
        Object xml = jsonToXmlService.convertJsonToXml(customerDetails);
        return ResponseEntity.ok(xml);
    }

    @PostMapping("/json-file-to-xml")
    public  ResponseEntity<?> convertJsonToXml(@RequestParam("file") MultipartFile file) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        CustomerDetails customerDetails = objectMapper.readValue(file.getInputStream(), CustomerDetails.class);
        return ResponseEntity.ok(jsonToXmlService.convertJsonToXml(customerDetails));
    }

    @GetMapping("/getFdaPn-record")
    public List<CustomsFdaPnSubmitDTO> getFdaRecord(@RequestParam("createdOn") @DateTimeFormat(pattern = "dd-MM-yyyy") Date createdOn,
                                                    @RequestParam String referenceId) {
        return fdaPnRecordSaver.getFdaPn(createdOn, referenceId);
    }

    @GetMapping("/getFdaPn-records")
    public PageDTO<CustomsFdaPnSubmitDTO> filterByFdaPnRecords(@RequestParam(name = "createdOn", required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") Date createdOn,
                                                            @RequestParam(name = "status", required = false) String status,
                                                            @RequestParam(name = "referenceId", required = false) String referenceId,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return fdaPnRecordSaver.filterByCriteria(createdOn, status, referenceId, pageable);
    }

    @GetMapping("/get-all")
    public PageDTO<CustomsFdaPnSubmitDTO> getAllRecords(@RequestParam String userId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return fdaPnRecordSaver.getAllByUserId(userId,page, size);
    }


}