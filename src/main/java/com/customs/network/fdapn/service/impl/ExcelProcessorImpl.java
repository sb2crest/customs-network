package com.customs.network.fdapn.service.impl;

import com.customs.network.fdapn.dto.*;
import com.customs.network.fdapn.exception.ErrorResCodes;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.service.*;
import com.customs.network.fdapn.validations.ValidationEntryPoint;
import com.customs.network.fdapn.validations.objects.priornotice.Declaration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.customs.network.fdapn.utils.ExcelStructureVerifier.validateExcelStructure;
import static com.customs.network.fdapn.utils.ObjectValidations.validateUserPartyInfoDto;
import static com.customs.network.fdapn.utils.RowMapper.mapFields;
import static com.customs.network.fdapn.utils.UtilMethods.truncateString;
import static com.customs.network.fdapn.validations.utils.ErrorUtils.createValidationError;

@Service
@Slf4j
@Primary
public class ExcelProcessorImpl implements ExcelProcessor {
    private final ObjectMapper objectMapper;
    private final TransactionSegregator transactionSegregator;
    private final ValidationEntryPoint validationEntryPoint;
    private final ProductServicePreProcessor productServicePreProcessor;
    private final AnalyzeFuture analyzeFuture;
    private final PartyDetailsService partyDetailsService;
    public long processStartTime;

    public ExcelProcessorImpl(ObjectMapper objectMapper,
                              TransactionSegregator transactionSegregator,
                              ValidationEntryPoint validationEntryPoint, ProductServicePreProcessor productServicePreProcessor,
                              AnalyzeFuture analyzeFuture, PartyDetailsService partyDetailsService) {
        this.objectMapper = objectMapper;
        this.transactionSegregator = transactionSegregator;
        this.validationEntryPoint = validationEntryPoint;
        this.productServicePreProcessor = productServicePreProcessor;
        this.analyzeFuture = analyzeFuture;
        this.partyDetailsService = partyDetailsService;
    }

    @Override
    public String processExcel(MultipartFile file) throws Exception {
        processStartTime = System.currentTimeMillis();
        long start = System.currentTimeMillis();
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        long end = System.currentTimeMillis();
        log.info("Time taken to load the excel  :->{} seconds", (end - start) / 1000.0);
        validateExcelStructure(workbook);
        log.info("Excel Structure validation is completed successfully");
        processPartyInfoSheet(workbook.getSheetAt(2));
        processBasicProductInfoSheet(workbook.getSheetAt(1));
        String res = processTransactionSheet(workbook.getSheetAt(0));
        end = System.currentTimeMillis();
        log.info("Time taken by processExcel() :->{} seconds", (end - start) / 1000.0);
        return res;
    }

    public String processTransactionSheet(Sheet sheet) {
        int chunkSize = 900;
        int numRows = sheet.getLastRowNum();
        log.info("Total Rows: {}", numRows);
        int numChunks = (int) Math.ceil((double) numRows / chunkSize);

        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<ExcelBatchResponse>> futures = new ArrayList<>();
        // Submit tasks for each chunk
        for (int i = 0; i < numChunks; i++) {
            final int startRow = i * chunkSize;
            final int endRow = Math.min(startRow + chunkSize - 1, numRows);
            futures.add(executorService.submit(() -> readChunk(sheet, startRow, endRow)));
        }
        new Thread(() -> analyzeFuture.ofExcelBatchResponse(futures)).start();

        return "Excel Uploaded Successfully";
    }

    private ExcelBatchResponse readChunk(Sheet sheet, int startRow, int endRow) throws Exception {
        List<ExcelTransactionInfo> transactionInfos = new ArrayList<>();
        for (int i = startRow; i <= endRow; i++) {
            ExcelTransactionInfo transactionInfo = new ExcelTransactionInfo();
            List<ValidationError> errors = new ArrayList<>();

            Row row = sheet.getRow(i);
            if (row == null || i == 0) {
                continue;
            }
            mapFields(ExcelTransactionInfo.class.getDeclaredFields(), transactionInfo, row);
            String declarationString = row.getCell(3).getRichStringCellValue().getString();
            String productCodeString = row.getCell(4).getRichStringCellValue().getString();
            if (StringUtils.isBlank(declarationString) || StringUtils.isBlank(productCodeString)) {
                log.warn("Skipping row {} due to empty prior notice information or product code information", i);
                continue;
            }
            List<String> productList = new ArrayList<>();
            Declaration declaration = getDeclaration(declarationString, String.valueOf(transactionInfo.getSlNo()), errors);
            if (declaration != null) {
                declaration.setActionCode(transactionInfo.getActionCode());
                declaration.setUniqueUserIdentifier(transactionInfo.getUniqueUserIdentifier());
            }
            transactionInfo.setDeclaration(declaration);
            transactionInfo.setValidationErrors(errors);
            transactionInfo.setProductCode(productList);
            transactionInfos.add(transactionInfo);
            transactionInfo.setTransactionProductDataString(productCodeString);
        }
        List<ExcelValidationResponse> excelValidationResponses = validationEntryPoint.validateExcelTransactions(transactionInfos);
        return transactionSegregator.segregateExcelResponse(excelValidationResponses);
    }

    private Declaration getDeclaration(String declarationString, String slNo, List<ValidationError> errors) {
        try {
            ObjectMapper strictMapper = objectMapper.copy();
            strictMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
            return strictMapper.readValue(declarationString, Declaration.class);
        } catch (JsonProcessingException e) {
            if (e instanceof UnrecognizedPropertyException unrecognizedPropertyException) {
                errors.add(createValidationError("Declaration", "Invalid JSON provided for Declaration for the transaction slNo " + slNo + " : Unknown field '" +
                        unrecognizedPropertyException.getPropertyName() + "' ", null));
            } else {
                errors.add(createValidationError("Declaration","Provided Declaration Json is Incorrect at Transaction slNo "+slNo, truncateString(declarationString,50)));
            }
        }
        return null;
    }

    private void processBasicProductInfoSheet(Sheet sheet) throws Exception {
        List<UserProductInfoDto> userProductInfoDtos = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            UserProductInfoDto product = new UserProductInfoDto();
            Row row = sheet.getRow(i);
            mapFields(UserProductInfoDto.class.getDeclaredFields(), product, row);
            if (!StringUtils.isBlank(product.getActionCode()) &&
                    product.getActionCode().equalsIgnoreCase("A") ||
                    product.getActionCode().equalsIgnoreCase("R") ||
                    product.getActionCode().equalsIgnoreCase("E")) {
                String jsonString = row.getCell(4).getRichStringCellValue().getString();
                if (StringUtils.isBlank(jsonString)) {
                    throw new FdapnCustomExceptions(ErrorResCodes.INVALID_DETAILS, "For action code A or E or R ,the field Product Information is mandatory");
                }
                try {
                    JsonNode jsonNode = objectMapper.readTree(jsonString);
                    product.setProductInfo(jsonNode);
                } catch (JsonProcessingException e) {
                    throw new FdapnCustomExceptions(ErrorResCodes.CONVERSION_FAILURE, "Please re-check the json structure provided for the product " + product.getProductCode() + ", reason : " + e.getMessage());
                }
            }
            userProductInfoDtos.add(product);
        }
        productServicePreProcessor.processProductInfo(userProductInfoDtos);
    }

    private void processPartyInfoSheet(Sheet sheet) throws Exception {
        List<UserPartyInfoDto> userPartyInfoDtoList = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            UserPartyInfoDto party = new UserPartyInfoDto();
            Row row = sheet.getRow(i);
            mapFields(UserPartyInfoDto.class.getDeclaredFields(), party, row);
            if (StringUtils.isBlank(party.getActionCode())) {
                throw new FdapnCustomExceptions(ErrorResCodes.INVALID_DETAILS, "Action code is mandatory for every row");
            } else if ("R".equalsIgnoreCase(party.getActionCode())) {
                String jsonString = row.getCell(4).getRichStringCellValue().getString();
                if (StringUtils.isBlank(jsonString)) {
                    throw new FdapnCustomExceptions(ErrorResCodes.INVALID_DETAILS, "For action code R, the field Party Information is mandatory");
                }
                JsonNode jsonNode = objectMapper.readTree(jsonString);
                party.setPartyInfo(jsonNode);
            }
            userPartyInfoDtoList.add(party);
        }
        performActionForPartyDetails(userPartyInfoDtoList);
    }

    private void performActionForPartyDetails(List<UserPartyInfoDto> userPartyInfos) {
        userPartyInfos.stream()
                .filter(Objects::nonNull)
                .forEach(obj -> {
                    String actionCode = obj.getActionCode().toUpperCase();
                    if (actionCode.equals("R")) {
                        validateUserPartyInfoDto(obj);
                        partyDetailsService.updatedParty(obj);
                    } else if (actionCode.equals("D")) {
                        partyDetailsService.deletedParty(obj.getUniqueUserIdentifier(), obj.getPartyIdentifierId());
                    } else {
                        throw new FdapnCustomExceptions(ErrorResCodes.UNKNOWN_ACTION, "Invalid action code provided, valid codes are R for replace, D for delete");
                    }
                });
    }
}
