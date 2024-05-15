package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.PGAIdentifierDto;

public interface PGAIdentifierService {
    PGAIdentifierDto getByAgencyProgramCode(String governmentAgencyProgramCode);
}
