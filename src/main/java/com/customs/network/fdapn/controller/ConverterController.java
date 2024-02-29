package com.customs.network.fdapn.controller;

import com.customs.network.fdapn.model.CustomerDetails;
import com.customs.network.fdapn.service.ExcelReaderService;
import com.customs.network.fdapn.service.XmlConverterService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/convert")
@Slf4j
@AllArgsConstructor
public class ConverterController {
    private final ExcelReaderService excelReaderService;
    @PostMapping("/excel-to-xml")
    public String convertExcelToXml(@RequestParam("file") MultipartFile file) {
        try {
            Workbook workbook = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            CustomerDetails customerDetails = excelReaderService.mapExcelToCustomerDetails(sheet);
            return XmlConverterService.convertToXml(customerDetails);
        } catch (Exception e) {
            log.error("Error converting Excel to XML: -> {} ", e.getMessage());
            return "Error converting Excel to XML: " + e.getMessage();
        }
    }
}
