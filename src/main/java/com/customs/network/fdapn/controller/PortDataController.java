package com.customs.network.fdapn.controller;

import com.customs.network.fdapn.dto.PortCodeDetailsDto;
import com.customs.network.fdapn.service.PortInfoServices;
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
public class PortDataController {
    private final PortInfoServices portInfoServices;

    public PortDataController(PortInfoServices portInfoServices) {
        this.portInfoServices = portInfoServices;
    }

    @PostMapping("/upload-port-details-excel")
    public ResponseEntity<String> uploadExcel(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please upload a file.");
        }
        return ResponseEntity.ok(portInfoServices.readAndUpdatePortDetailsFromTheExcel(file));
    }

    @GetMapping("/getPortData")
    public List<PortCodeDetailsDto> getByPortDetails(@RequestParam(required = false) String portDetails) {
        return portInfoServices.getPortDetailsByPortNumberOrPortName(portDetails);
    }
}
