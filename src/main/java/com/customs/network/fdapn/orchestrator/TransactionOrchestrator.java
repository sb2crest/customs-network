package com.customs.network.fdapn.orchestrator;

import com.customs.network.fdapn.dto.FilterCriteriaDTO;
import com.customs.network.fdapn.dto.PageDTO;
import com.customs.network.fdapn.dto.ScanSchema;
import com.customs.network.fdapn.model.TrackingDetails;
import com.customs.network.fdapn.model.TransactionInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TransactionOrchestrator {
    String processExcel(MultipartFile file);

    TransactionInfo getFdapnTransaction(String refId);

    PageDTO<TransactionInfo> fetchByFilter(FilterCriteriaDTO criteriaDTO);

    PageDTO<TransactionInfo> scanSchemaByColValue(ScanSchema scanner);

    List<String> getTextFilesInFolder(String folderKey);

    List<String> getFoldersInBucket();
}
