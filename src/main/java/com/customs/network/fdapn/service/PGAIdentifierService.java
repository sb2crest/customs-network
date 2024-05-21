package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.PGAIdentifierDto;
import com.customs.network.fdapn.dto.StateCodeInfoDto;

import java.util.List;

public interface PGAIdentifierService {
    PGAIdentifierDto getByAgencyProgramCode(String governmentAgencyProgramCode);

    String saveStateCodes(StateCodeInfoDto stateCodeInfoDto);

    StateCodeInfoDto getStateCodes(String countryCode);

    List<String> getAllCountryCodes();
}
