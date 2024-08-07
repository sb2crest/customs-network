package com.customs.network.fdapn.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.multipart.MultipartFile;

public interface ExcelProcessor {

    String processExcel(MultipartFile file) throws Exception;

    String processRequestJson(JsonNode requestJson);

}
