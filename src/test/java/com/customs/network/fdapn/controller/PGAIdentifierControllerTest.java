package com.customs.network.fdapn.controller;

import com.customs.network.fdapn.config.AuthService;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import com.customs.network.fdapn.dto.PGAIdentifierDto;
import com.customs.network.fdapn.service.PGAIdentifierServiceImp;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(PGAIdentifierController.class)
class PGAIdentifierControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private PGAIdentifierServiceImp identifierServiceImp;
    @MockBean
    private AuthService authService;
    @Autowired
    WebApplicationContext context;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void testGetByGovernmentAgencyProgramCode() throws Exception {
        PGAIdentifierDto dto=new PGAIdentifierDto();
        JsonNode dummyJson = objectMapper.createObjectNode()
                .put("key1", "value1")
                .put("key2", "value2")
                .put("key3", 123)
                .putArray("key4")
                .add("element1")
                .add("element2");
       dto.setProgramCodeData(dummyJson);
        mockMvc.perform(get("/pgaIdentifier/product")
                        .param("governmentAgencyProgramCode","FOO")
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

}