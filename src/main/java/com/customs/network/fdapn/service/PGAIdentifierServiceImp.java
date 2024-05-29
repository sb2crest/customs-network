package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.PGAIdentifierDto;
import com.customs.network.fdapn.dto.StateCodeInfoDto;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.model.PGAIdentifierDetails;
import com.customs.network.fdapn.model.StateCodeInfo;
import com.customs.network.fdapn.repository.PGAIdentifierRepository;
import com.customs.network.fdapn.repository.StateCodeInfoRepository;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.customs.network.fdapn.exception.ErrorResCodes.*;

@Service
@Slf4j
public class PGAIdentifierServiceImp implements PGAIdentifierService {

    @Autowired
    PGAIdentifierRepository identifierRepository;
    @Autowired
    StateCodeInfoRepository stateCodeInfoRep;


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
                throw new FdapnCustomExceptions(EMPTY_DETAILS, "No data available for " + governmentAgencyProgramCode);
            }
        } catch (DataRetrievalFailureException exception) {
            throw new FdapnCustomExceptions(SERVER_ERROR, "getting error in while retrieving data");
        }
    }

    @Override
    public String saveStateCodes(StateCodeInfoDto stateCodeInfoDto) {
        try {
            stateCodeInfoRep.save(getStateCodeInfo(stateCodeInfoDto));
            return "Saved Successfully";
        } catch (DataAccessException e) {
            log.error("fail to save state codes with country code {} ,{} ",
                    stateCodeInfoDto.getCountryCode(), e.getMessage());
            throw new FdapnCustomExceptions(SERVER_ERROR,
                    "Failed to save the countryCode " + stateCodeInfoDto.getCountryCode());
        } catch (Exception e) {
            log.error("Unexpected error while saving stateCode with country code {} ,{} ",
                    stateCodeInfoDto.getCountryCode(), e.getMessage());
            throw new FdapnCustomExceptions(SERVER_ERROR,
                    "Error while saving the stateCodes " + stateCodeInfoDto.getCountryCode());
        }
    }

    @Override
    public StateCodeInfoDto getStateCodes(String countryCode) {
        if (StringUtils.isBlank(countryCode))
            throw new FdapnCustomExceptions(INVALID_DETAILS, "country code cannot be empty or null");
        StateCodeInfo stateCodeInfo = stateCodeInfoRep.findById(countryCode.toUpperCase())
                .orElseThrow(() -> new FdapnCustomExceptions(RECORD_NOT_FOUND, "No data available for " + countryCode));
        return getStateCodeInfoDto(stateCodeInfo);
    }

    @Override
    public List<String> getAllCountryCodes(){
        try{
            return stateCodeInfoRep.findAllCountryCodes();
        }catch (Exception e){
            log.error("Error getting country codes -{}",e.getMessage());
            return Collections.emptyList();
        }
    }

    private StateCodeInfo getStateCodeInfo(StateCodeInfoDto stateCodeInfoDto) {
        return StateCodeInfo.builder()
                .countryCode(stateCodeInfoDto.getCountryCode().toUpperCase())
                .stateCodes(stateCodeInfoDto.getStateCodes())
                .build();
    }

    private StateCodeInfoDto getStateCodeInfoDto(StateCodeInfo stateCodeInfo) {
        return StateCodeInfoDto.builder()
                .countryCode(stateCodeInfo.getCountryCode())
                .stateCodes(stateCodeInfo.getStateCodes())
                .build();
    }
}
