package com.customs.network.fdapn.controller;

import com.customs.network.fdapn.model.PortCodeDetails;
import com.customs.network.fdapn.repository.PortCodeDetailsRepository;
import com.customs.network.fdapn.service.ExcelParser;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@RestController
public class PortCodeExcelController {
    private final PortCodeDetailsRepository repository;
    private final ExcelParser excelParser;

    public PortCodeExcelController(PortCodeDetailsRepository repository, com.customs.network.fdapn.service.ExcelParser excelParser) {
        this.repository = repository;
        this.excelParser = excelParser;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadExcel(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please upload a file.");
        }
        List<PortCodeDetails> portCodeDetailsList = excelParser.parse(file);
        repository.saveAll(portCodeDetailsList);
        return ResponseEntity.ok("Data uploaded successfully.");
    }

    @GetMapping("/getPortData")
    public List<?> getByPortDetails(@RequestParam(required = false) String portName,
                                    @RequestParam(required = false) String portCode){
       return excelParser.getPortDetailsByPortNumberOrPortName(portName,portCode);
    }
}
