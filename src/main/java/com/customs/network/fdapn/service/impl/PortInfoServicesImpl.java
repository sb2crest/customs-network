package com.customs.network.fdapn.service.impl;

import com.customs.network.fdapn.dto.PortCodeDetailsDto;
import com.customs.network.fdapn.exception.ErrorResCodes;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.model.PortCodeDetails;
import com.customs.network.fdapn.repository.PortCodeDetailsRepository;
import com.customs.network.fdapn.service.PortInfoServices;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PortInfoServicesImpl implements PortInfoServices {
    private final PortCodeDetailsRepository repository;

    public PortInfoServicesImpl(PortCodeDetailsRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<PortCodeDetailsDto> getPortDetailsByPortNumberOrPortName(String portDetails) {
        List<PortCodeDetails> portCodeDetailsList;
        if(!StringUtils.isBlank(portDetails)){
            portCodeDetailsList = repository.findByPortCodeOrPortName(portDetails);
        }else{
            throw new FdapnCustomExceptions(ErrorResCodes.INVALID_DETAILS,"port details cannot be empty or null");
        }
        return portCodeDetailsList.stream().map(this::convertToPortCodeDetails).toList();
    }
    private PortCodeDetailsDto convertToPortCodeDetails(PortCodeDetails portCodeDetails){
        PortCodeDetailsDto portCodeDetailsDto=new PortCodeDetailsDto();
        portCodeDetailsDto.setPortName(portCodeDetails.getPortName());
        portCodeDetailsDto.setCountry(portCodeDetails.getCountry());
        portCodeDetailsDto.setState(portCodeDetails.getState());
        portCodeDetailsDto.setPortCode(portCodeDetails.getPortCode());
        portCodeDetailsDto.setSno(portCodeDetails.getSno());
        return portCodeDetailsDto;
    }
    @Override
    public String readAndUpdatePortDetailsFromTheExcel(MultipartFile file) throws IOException {
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
                    log.info("Row with Serial Number " + portCodeDetails.getSno() + " has a null PortName.");
                }

                Cell portCodeCell = row.getCell(4);
                if (portCodeCell != null) {
                    portCodeDetails.setPortCode(portCodeCell.getStringCellValue());
                } else {
                    portCodeDetails.setPortCode(null);
                    log.info("Row with Serial Number " + portCodeDetails.getSno() + " has a null PortCode.");
                }
                portCodeDetailsList.add(portCodeDetails);
            }
            repository.saveAll(portCodeDetailsList);
        }
        return "Uploaded";
    }
}
