package com.customs.network.fdapn.controller;

import com.customs.network.fdapn.dto.PGAIdentifierDto;
import com.customs.network.fdapn.dto.StateCodeInfoDto;
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

    @GetMapping("/get-agency-program-code")
    public ResponseEntity<PGAIdentifierDto> getByGovernmentAgencyProgramCode(@RequestParam String governmentAgencyProgramCode) {
        return new ResponseEntity<>(pgaIdentifierService.getByAgencyProgramCode(governmentAgencyProgramCode), HttpStatus.OK);
    }

    @PostMapping("/save-state-codes")
    public ResponseEntity<String> saveStateCodes(@RequestBody StateCodeInfoDto stateCodeInfoDto) {
        return new ResponseEntity<>(pgaIdentifierService.saveStateCodes(stateCodeInfoDto), HttpStatus.OK);
    }

    @GetMapping("/get-state-codes")
    public ResponseEntity<StateCodeInfoDto> getStateCodes(@RequestParam("countryCode") String countryCode) {
        return new ResponseEntity<>(pgaIdentifierService.getStateCodes(countryCode), HttpStatus.OK);
    }


}
