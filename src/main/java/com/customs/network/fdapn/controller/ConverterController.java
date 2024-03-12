package com.customs.network.fdapn.controller;

import com.customs.network.fdapn.dto.CustomsFdaPnSubmitDTO;
import com.customs.network.fdapn.dto.PageDTO;
import com.customs.network.fdapn.model.CustomerDetails;
import com.customs.network.fdapn.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
       return excelReaderService.processExcelFile(file);
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
                                                            @RequestParam(name = "userId") String userId,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdOn");
        Pageable pageable = PageRequest.of(page, size, sort);
        return fdaPnRecordSaver.filterByCriteria(createdOn, status, referenceId,userId,pageable);
    }

    @GetMapping("/get-all")
    public PageDTO<CustomsFdaPnSubmitDTO> getAllRecords(@RequestParam String userId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return fdaPnRecordSaver.getAllByUserId(userId,page, size);
    }


}