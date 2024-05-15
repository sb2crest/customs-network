package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.PGAIdentifierDto;
import com.customs.network.fdapn.exception.ErrorResCodes;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.model.PGAIdentifierDetails;
import com.customs.network.fdapn.repository.PGAIdentifierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PGAIdentifierServiceImp implements PGAIdentifierService {

    @Autowired
    PGAIdentifierRepository identifierRepository;


    @Override
    public PGAIdentifierDto getByAgencyProgramCode(String governmentAgencyProgramCode) {
        try {
            PGAIdentifierDto pgaIdentifierDto = new PGAIdentifierDto();
            Optional<PGAIdentifierDetails> productDetails = identifierRepository.findById(governmentAgencyProgramCode);
            if (productDetails.isPresent()) {
                PGAIdentifierDetails details = productDetails.get();
                pgaIdentifierDto.setProgramCodeData(details.getProgramCodeData());
                return pgaIdentifierDto;
            } else {
                throw new FdapnCustomExceptions(ErrorResCodes.EMPTY_DETAILS, "No data available for " + governmentAgencyProgramCode);
            }
        } catch (DataRetrievalFailureException exception) {
            throw new FdapnCustomExceptions(ErrorResCodes.SERVER_ERROR, "getting error in while retrieving data");
        }
    }

}
