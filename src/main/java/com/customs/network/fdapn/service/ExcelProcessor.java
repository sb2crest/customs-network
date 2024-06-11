package com.customs.network.fdapn.service;

import org.springframework.web.multipart.MultipartFile;

public interface ExcelProcessor {
    String processExcel(MultipartFile file) throws Exception;
}
