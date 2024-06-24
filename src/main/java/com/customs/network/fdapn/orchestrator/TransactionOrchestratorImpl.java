package com.customs.network.fdapn.orchestrator;

import com.customs.network.fdapn.dto.FilterCriteriaDTO;
import com.customs.network.fdapn.dto.PageDTO;
import com.customs.network.fdapn.dto.ScanSchema;
import com.customs.network.fdapn.exception.ErrorResCodes;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.model.TransactionInfo;
import com.customs.network.fdapn.repository.TransactionManagerRepo;
import com.customs.network.fdapn.service.AWSS3Services;
import com.customs.network.fdapn.service.ExcelProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class TransactionOrchestratorImpl implements TransactionOrchestrator {
    @Value("${aws.bucketName}")
    private String cbpDownBucketName;
    private final ExcelProcessor excelProcessor;
    private final AWSS3Services awss3Services;
    private final TransactionManagerRepo transactionManager;

    @Autowired
    public TransactionOrchestratorImpl(ExcelProcessor excelProcessor,
                                       AWSS3Services awss3Services,
                                       TransactionManagerRepo transactionManager) {
        this.excelProcessor = excelProcessor;
        this.awss3Services = awss3Services;
        this.transactionManager = transactionManager;
    }


    @Override
    public String processExcel(MultipartFile file) {
        try {
            return excelProcessor.processExcel(file);
        } catch (Exception e) {
            throw new FdapnCustomExceptions(ErrorResCodes.EXECUTION_FAILURE,e.getMessage());
        }
    }

    @Override
    public TransactionInfo getFdapnTransaction(String refId) {
        return transactionManager.fetchTransaction(refId);
    }

    @Override
    public PageDTO<TransactionInfo> fetchByFilter(FilterCriteriaDTO criteriaDTO) {
        return transactionManager.fetchByFilter(criteriaDTO);
    }

    @Override
    public PageDTO<TransactionInfo> scanSchemaByColValue(ScanSchema scanner) {
        return transactionManager.scanSchemaByColValue(scanner.getFieldName(),
                scanner.getValue(),
                scanner.getStartDate(),
                scanner.getEndDate(),
                scanner.getUserId(),
                scanner.getPage(),
                scanner.getSize()
        );
    }

    @Override
    public List<String> getTextFilesInFolder(String folderKey) {
        return awss3Services.getTextFilesInFolder(folderKey);
    }

    @Override
    public List<String> getFoldersInBucket() {
        return awss3Services.getFoldersInBucket(cbpDownBucketName);
    }
}
