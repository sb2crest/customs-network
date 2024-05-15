package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.PGAIdentifierDto;
import com.customs.network.fdapn.exception.ErrorResCodes;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.model.PGAIdentifierDetails;
import com.customs.network.fdapn.repository.PGAIdentifierRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataRetrievalFailureException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PGAIdentifierServiceImpTest {
    @InjectMocks
    private PGAIdentifierServiceImp serviceImp;

    @Mock
    private PGAIdentifierRepository identifierRepository;

    @Test
    void whenValidCodesThere_saveInDatabase_thenReturnSuccessfully() {
        PGAIdentifierDetails productDetails = new PGAIdentifierDetails();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode dummyJson = objectMapper.createObjectNode()
                .put("key1", "value1")
                .put("key2", "value2")
                .put("key3", 123)
                .putArray("key4")
                .add("element1")
                .add("element2");
        productDetails.setProgramCodeData(dummyJson);

        when(identifierRepository.findById("BIO")).thenReturn(Optional.of(productDetails));

        PGAIdentifierDto byAgencyProgramCode = serviceImp.getByAgencyProgramCode("BIO");

        Assertions.assertNotNull(byAgencyProgramCode);

        verify(identifierRepository).findById("BIO");
    }
    @Test
    void whenGovernmentAgencyProgramCodeIsNotPresent_ThrowFdapnCustomeException(){

        when(identifierRepository.findById("INVALID_CODE")).thenReturn(Optional.empty());

        FdapnCustomExceptions exception = assertThrows(FdapnCustomExceptions.class, () -> {
            serviceImp.getByAgencyProgramCode("INVALID_CODE");});

        assertEquals(ErrorResCodes.EMPTY_DETAILS, exception.getResCodes());
        assertEquals("No data available for " +"INVALID_CODE", exception.getMessage());
    }

    @Test
    void whenValidCodesNotThere_throwDataNotAccessException() {

        when(identifierRepository.findById("FOO")).thenThrow(new DataRetrievalFailureException("getting error"));

        Exception exception = assertThrows(FdapnCustomExceptions.class, () ->
                serviceImp.getByAgencyProgramCode("FOO"));
        assertEquals("getting error in while retrieving data", exception.getMessage());
    }
}