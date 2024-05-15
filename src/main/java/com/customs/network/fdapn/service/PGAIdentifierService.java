package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.PGAIdentifierDto;
import com.customs.network.fdapn.model.PGAIdentifierDetails;

public interface PGAIdentifierService {
    PGAIdentifierDto getByAgencyProgramCode(String governmentAgencyProgramCode);
//    String postByAgencyProgramCode(PGAIdentifierDto pgaIdentifierDto);

}
