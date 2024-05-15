package com.customs.network.fdapn.controller;

import com.customs.network.fdapn.dto.PGAIdentifierDto;
import com.customs.network.fdapn.service.PGAIdentifierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pgaIdentifier")
public class PGAIdentifierController {

    @Autowired
    PGAIdentifierService pgaIdentifierService;

    @GetMapping("/product")
    public ResponseEntity<PGAIdentifierDto> getByGovernmentAgencyProgramCode(@RequestParam String governmentAgencyProgramCode) {
        return new ResponseEntity<>(pgaIdentifierService.getByAgencyProgramCode(governmentAgencyProgramCode), HttpStatus.OK);
    }

}
