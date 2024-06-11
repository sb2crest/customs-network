package com.customs.network.fdapn.orchestrator;

import com.customs.network.fdapn.dto.FilterCriteriaDTO;
import com.customs.network.fdapn.dto.PageDTO;
import com.customs.network.fdapn.dto.ScanSchema;
import com.customs.network.fdapn.model.TrackingDetails;
import com.customs.network.fdapn.model.TransactionInfo;
import com.customs.network.fdapn.repository.TransactionManagerRepo;
import com.customs.network.fdapn.service.AWSS3Services;
import com.customs.network.fdapn.service.ExcelProcessor;
import com.customs.network.fdapn.service.impl.ExcelReaderServiceImpl;
import com.customs.network.fdapn.service.JsonToXmlService;
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
    private final ExcelReaderServiceImpl excelReaderServiceImpl;
    private final JsonToXmlService jsonToXmlService;

    public TransactionOrchestratorImpl(ExcelProcessor excelProcessor, AWSS3Services awss3Services, TransactionManagerRepo transactionManager, ExcelReaderServiceImpl excelReaderServiceImpl, JsonToXmlService jsonToXmlService) {
        this.excelProcessor = excelProcessor;
        this.awss3Services = awss3Services;
        this.transactionManager = transactionManager;
        this.excelReaderServiceImpl = excelReaderServiceImpl;
        this.jsonToXmlService = jsonToXmlService;
    }

    @Override
    public String processExcelFile(MultipartFile file) {
        return excelReaderServiceImpl.processExcel(file);
    }

    @Override
    public String convertJsonToXml(List<TrackingDetails> trackingDetails) {
        return jsonToXmlService.convertJsonToXml(trackingDetails);
    }

    @Override
    public String processExcel(MultipartFile file) {
        try {
            return excelProcessor.processExcel(file);
        } catch (Exception e) {
            throw new RuntimeException(e);
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
