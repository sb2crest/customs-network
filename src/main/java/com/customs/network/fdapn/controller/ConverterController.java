package com.customs.network.fdapn.controller;

import com.customs.network.fdapn.dto.CustomsFdaPnSubmitDTO;
import com.customs.network.fdapn.model.CustomerDetails;
import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.service.ExcelReaderService;
import com.customs.network.fdapn.service.FdaPnRecordSaver;
import com.customs.network.fdapn.service.ValidationService;
import com.customs.network.fdapn.service.XmlConverterService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/convert")
@Slf4j
@AllArgsConstructor
public class ConverterController {
    private final ExcelReaderService excelReaderService;
    private final FdaPnRecordSaver fdaPnRecordSaver;
    private final ValidationService validationService;

    @PostMapping("/excel-to-xml")
    public Object convertExcelToXml(@RequestParam("file") MultipartFile file) {
        try {
            Workbook workbook = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            CustomerDetails customerDetails = excelReaderService.mapExcelToCustomerDetails(sheet);
            List<ValidationError> validationErrors = validationService.validateField(Collections.singletonList(customerDetails));
            if (!validationErrors.isEmpty()) {
                return fdaPnRecordSaver.failureRecords(customerDetails, validationErrors);
            }
            fdaPnRecordSaver.save(customerDetails);
            return XmlConverterService.convertToXml(customerDetails);
        } catch (Exception e) {
            log.error("Error converting Excel to XML: -> {} ", e.getMessage());
            return "Error converting Excel to XML: " + e.getMessage();
        }
    }

    @GetMapping("/getFdapn-record")
    public List<CustomsFdaPnSubmitDTO> getFdaRecord(@RequestParam("createdOn") @DateTimeFormat(pattern = "dd-MM-yyyy") Date createdOn,
                                                    @RequestParam String referenceId) {
        return fdaPnRecordSaver.getFdaPn(createdOn, referenceId);
    }

    @GetMapping("/getFdaPn-records")
    public List<CustomsFdaPnSubmitDTO> filterByFdaPnRecords(@RequestParam(name = "createdOn", required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") Date createdOn,
                                                            @RequestParam(name = "status", required = false) String status,
                                                            @RequestParam(name = "referenceId", required = false) String referenceId) {
        return fdaPnRecordSaver.filterByCriteria(createdOn, status, referenceId);
    }

}