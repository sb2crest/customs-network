package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.PortCodeDetailsDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PortInfoServices {
    List<PortCodeDetailsDto> getPortDetailsByPortNumberOrPortName(String portDetails);

    String readAndUpdatePortDetailsFromTheExcel(MultipartFile file) throws IOException;
}
