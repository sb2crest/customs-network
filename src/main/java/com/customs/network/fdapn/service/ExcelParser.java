package com.customs.network.fdapn.service;

import com.customs.network.fdapn.model.PortCodeDetails;
import com.customs.network.fdapn.repository.PortCodeDetailsRepository;
import io.micrometer.common.util.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExcelParser {
    private final PortCodeDetailsRepository repository;

    public ExcelParser(PortCodeDetailsRepository repository) {
        this.repository = repository;
    }

    //LATER WILL REMOVE THIS IS FOR PORT-DETAILS ENTRIES
    public List<PortCodeDetails> parse(MultipartFile file) throws IOException {
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
                if (row.getCell(3) != null) {
                    portCodeDetails.setPortName(row.getCell(3).getStringCellValue());
                } else {
                    portCodeDetails.setPortName(null);
                    System.out.println("Row with Serial Number " + portCodeDetails.getSno() + " has a null PortName.");
                }

                Cell portCodeCell = row.getCell(4);
                if (portCodeCell != null) {
                    portCodeDetails.setPortCode(portCodeCell.getStringCellValue());
                } else {
                    portCodeDetails.setPortCode(null);
                    System.out.println("Row with Serial Number " + portCodeDetails.getSno() + " has a null PortCode.");
                }
                portCodeDetailsList.add(portCodeDetails);
            }
        }
        return portCodeDetailsList;
    }
    public List<?> getPortDetailsByPortNumberOrPortName(String portName, String portCode) {
        List<?> portDetails;
        if(!StringUtils.isBlank(portName)){
            portDetails = repository.findDistinctPortNamesByPattern(portName);
        } else {
            portDetails = repository.findDistinctPortCodesByPattern(portCode);
        }
        return portDetails;
    }
}
