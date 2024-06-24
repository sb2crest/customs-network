package com.customs.network.fdapn.service.impl;

import com.converter.exceptions.InvalidDataException;
import com.converter.objects.EdiRequest;
import com.converter.objects.EdiResponse;
import com.converter.service.ConverterService;
import com.customs.network.fdapn.dto.ExcelValidationResponse;
import com.customs.network.fdapn.dto.UserProductInfoDto;
import com.customs.network.fdapn.service.AWSS3Services;
import com.customs.network.fdapn.service.UserProductInfoServices;
import com.customs.network.fdapn.utils.CustomIdGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.*;

@Service
@Slf4j
public class CBPServiceImpl {
    private final CustomIdGenerator idGenerator;
    private final AWSS3Services s3Services;
    private final ObjectMapper objectMapper;
    private final UserProductInfoServices userProductInfoServices;
    private final ConverterService converterService;
    Random random = new Random();

    public CBPServiceImpl(CustomIdGenerator idGenerator,
                          AWSS3Services s3Services,
                          ObjectMapper objectMapper,
                          UserProductInfoServices userProductInfoServices,
                          ConverterService converterService) {
        this.idGenerator = idGenerator;
        this.s3Services = s3Services;
        this.objectMapper = objectMapper;
        this.userProductInfoServices = userProductInfoServices;
        this.converterService = converterService;
    }

    private void hitCbp(EdiResponse response) {
        String ediData = response.getFile();
        String refID = response.getRefId();
        Long sNo = idGenerator.extractIdFromRefId(refID);
        if (sNo % 5000 == 0) {
            log.info("CBP Server Down");
            s3Services.saveCbpDownFiles(ediData, refID);
        }
        if (sNo % 25000 == 0) {
            Path path = getFilePath(refID);
            downloadRandomSampleEdiFile(ediData, path);
        }
    }

    private Path getFilePath(String refId) {
        String fileName = "output_" + refId + ".txt";
        Path downloadsPath = Paths.get(System.getProperty("user.home"), "Downloads" + "/Test_folder/");
        return downloadsPath.resolve(fileName);
    }

    private void downloadRandomSampleEdiFile(String ediFile, Path filename) {
        log.info("Downloading sample EDI, Saved file:- > {} location", filename);
        try (BufferedWriter writer = Files.newBufferedWriter(filename)) {
            writer.write(ediFile);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void executeFinalProcessingAndSendToCBP(List<ExcelValidationResponse> excelValidationResponse) {
        log.info("CBP Processing Started");
        List<CompletableFuture<Void>> tasks = excelValidationResponse.stream()
                .map(obj -> CompletableFuture.runAsync(() -> processAndSendToCBP(obj)))
                .toList();
        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]))
                .join();
        log.info("CBP Processing Completed");
    }

    private void processAndSendToCBP(ExcelValidationResponse obj) {
        EdiRequest ediRequest = new EdiRequest();
        List<String> productCodes = obj.getExcelTransactionInfo().getProductCode();
        String uniqueUserIdentifier = obj.getExcelTransactionInfo().getUniqueUserIdentifier();
        List<UserProductInfoDto> productInfoList = userProductInfoServices.fetchAllProducts(productCodes, uniqueUserIdentifier);

        if (!productInfoList.isEmpty()) {
            List<JsonNode> productInfo = productInfoList.stream()
                    .map(UserProductInfoDto::getProductInfo)
                    .toList();

            obj.getExcelTransactionInfo().getPriorNoticeData().setProducts(productInfo);
            JsonNode edi = objectMapper.valueToTree(obj.getExcelTransactionInfo().getPriorNoticeData());
            String refId = obj.getExcelTransactionInfo().getReferenceId();
            ediRequest.setRefId(refId);
            ediRequest.setSubject(edi);
            try {
                EdiResponse response = converterService.convertToEdi(ediRequest);
                hitCbp(response);
            } catch (InvalidDataException e) {
                log.error("Error converting to EDI: {}", e.getMessage());
            }
        }
    }


    private void generateEdiResponse(List<EdiRequest> ediRequests) {
        log.info("Generating EDI response for the batch request with size " + ediRequests.size());
        long start = System.currentTimeMillis();
        List<EdiResponse> responses = converterService.convertToEdiSequential(ediRequests, true);
        long end = System.currentTimeMillis();
        log.info("EDI response generated for the batch request with size {} in {} seconds ", responses.size(), (end - start) / 1000);
        responses.stream()
                .filter(Objects::nonNull)
                .forEach(this::hitCbp);
    }
}
