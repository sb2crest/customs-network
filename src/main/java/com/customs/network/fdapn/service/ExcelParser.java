package com.customs.network.fdapn.service;

import com.customs.network.fdapn.model.PortCodeDetails;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExcelParser {
    public static List<PortCodeDetails> parse(MultipartFile file) throws IOException {
        List<PortCodeDetails> portCodeDetailsList = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }
                PortCodeDetails portCodeDetails = new PortCodeDetails();
                portCodeDetails.setSno((long) row.getCell(0).getNumericCellValue());
                portCodeDetails.setCountry(row.getCell(1).getStringCellValue());
                portCodeDetails.setState(row.getCell(2).getStringCellValue());
                portCodeDetails.setPortName(row.getCell(3).getStringCellValue());
                Cell portCodeCell = row.getCell(4);
                if (portCodeCell != null && portCodeCell.getCellType() == CellType.NUMERIC) {
                    portCodeDetails.setPortCode((int) portCodeCell.getNumericCellValue());
                } else {
                    portCodeDetails.setPortCode(null);
                }
                portCodeDetailsList.add(portCodeDetails);
            }
        }
        return portCodeDetailsList;
    }
}
