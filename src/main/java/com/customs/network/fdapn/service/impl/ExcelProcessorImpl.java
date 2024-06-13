package com.customs.network.fdapn.service.impl;

import com.customs.network.fdapn.dto.*;
import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.service.AnalyzeFuture;
import com.customs.network.fdapn.service.ExcelProcessor;
import com.customs.network.fdapn.service.TransactionSegregator;
import com.customs.network.fdapn.service.UserProductInfoServices;
import com.customs.network.fdapn.validations.ValidateProduct;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.stream.Collectors;

import static com.customs.network.fdapn.utils.RowMapper.mapFields;

@Service
@Slf4j
@Primary
public class ExcelProcessorImpl implements ExcelProcessor {
    private final ObjectMapper objectMapper;
    private final UserProductInfoServices userInfoServices;
    private final TransactionSegregator transactionSegregator;
    private final ValidateProduct validateProduct;
    private final AnalyzeFuture analyzeFuture;

    public ExcelProcessorImpl(ObjectMapper objectMapper,
                              UserProductInfoServices userInfoServices,
                              TransactionSegregator transactionSegregator,
                              ValidateProduct validateProduct, AnalyzeFuture analyzeFuture) {
        this.objectMapper = objectMapper;
        this.userInfoServices = userInfoServices;
        this.transactionSegregator = transactionSegregator;
        this.validateProduct = validateProduct;
        this.analyzeFuture = analyzeFuture;
    }

    @Override
    public String processExcel(MultipartFile file) throws Exception {
        long start = System.currentTimeMillis();
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        long end = System.currentTimeMillis();
        log.info("Time taken to load the excel  :->{} seconds", (end - start) / 1000.0);
        readSheetTwo(workbook.getSheetAt(1));
        String res = readSheetOne(workbook.getSheetAt(0));
        end = System.currentTimeMillis();
        log.info("Time taken by processExcel() :->{} seconds", (end - start) / 1000.0);
        return res;
    }

    public String readSheetOne(Sheet sheet) {
        int chunkSize = 900;
        int numRows = sheet.getLastRowNum();
        log.info("Total Rows :->{}", numRows);
        int numChunks = (int) Math.ceil((double) (numRows) / chunkSize);

        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<ExcelBatchResponse>> futures = new ArrayList<>();

        for (int i = 0; i < numChunks; i++) {
            final int startRow = i * chunkSize;
            final int endRow = Math.min(startRow + chunkSize - 1, numRows);
            futures.add(executorService.submit(() -> readChunk(sheet, startRow, endRow)));
        }
        executorService.shutdown();
        new Thread(() -> analyzeFuture.ofExcelBatchResponse(futures)).start();
        return "Excel Uploaded Successfully";
    }

    private ExcelBatchResponse readChunk(Sheet sheet, int startRow, int endRow) throws Exception {
        List<ExcelTransactionInfo> transactionInfos = new ArrayList<>();
        for (int i = startRow; i <= endRow; i++) {
            ExcelTransactionInfo transactionInfo = new ExcelTransactionInfo();
            Row row = sheet.getRow(i);
            if (row == null || i == 0) {
                continue;
            }
            mapFields(ExcelTransactionInfo.class.getDeclaredFields(), transactionInfo, row);
            String priorNoticeString = row.getCell(3).getRichStringCellValue().getString();
            String productCodeString = row.getCell(4).getRichStringCellValue().getString();
            if (StringUtils.isBlank(priorNoticeString) || StringUtils.isBlank(productCodeString)) {
                log.warn("Skipping row {} due to empty prior notice information or product code information", i);
                continue;
            }
            List<String> productList = objectMapper.readValue(productCodeString, new TypeReference<>() {
            });
            PriorNoticeData priorNoticeData = objectMapper.treeToValue(objectMapper.readTree(priorNoticeString), PriorNoticeData.class);
            priorNoticeData.setActionCode(transactionInfo.getActionCode());
            priorNoticeData.setUniqueUserIdentifier(transactionInfo.getUniqueUserIdentifier());
            transactionInfo.setPriorNoticeData(priorNoticeData);
            transactionInfo.setProductCode(productList);
            transactionInfos.add(transactionInfo);
        }
        List<ExcelValidationResponse> excelValidationRespons = validateProduct.validateExcelTransactions(transactionInfos);
        return transactionSegregator.segregateExcelResponse(excelValidationRespons);

    }

    private void readSheetTwo(Sheet sheet) throws Exception {
        List<UserProductInfoDto> userProductInfoDtos = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            UserProductInfoDto product = new UserProductInfoDto();
            Row row = sheet.getRow(i);
            mapFields(UserProductInfoDto.class.getDeclaredFields(), product, row);
            if (!StringUtils.isBlank(product.getActionCode()) && product.getActionCode().equalsIgnoreCase("A") ||
                    product.getActionCode().equalsIgnoreCase("R") ||
                    product.getActionCode().equalsIgnoreCase("E") ||
                    product.getActionCode().equalsIgnoreCase("TU")) {
                String jsonString = row.getCell(4).getRichStringCellValue().getString();
                if (StringUtils.isBlank(jsonString)) {
                    throw new RuntimeException("For action code A or E ,the field Product Information is mandatory");
                }
                JsonNode jsonNode = objectMapper.readTree(jsonString);
                product.setProductInfo(jsonNode);
            }
            userProductInfoDtos.add(product);
        }
        process(userProductInfoDtos);
    }

    private void process(List<UserProductInfoDto> data) {
        data.parallelStream()
                .filter(Objects::nonNull)
                .forEach(this::accept);
    }

    private UserProductInfoDto processUpdateAction(UserProductInfoDto object) {
        long startTime = System.currentTimeMillis();
        JsonNode update = object.getProductInfo();
        UserProductInfoDto originalProductInfo = userInfoServices.getProductByProductCode(object.getUniqueUserIdentifier(),
                object.getProductCode());
        ObjectNode original = (ObjectNode) originalProductInfo.getProductInfo();
        log.info("Original Product Info: {}", original);
        log.info("Update Product Info: {}", update);
        Iterator<Map.Entry<String, JsonNode>> fields = update.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String fieldName = entry.getKey();
            JsonNode newValue = entry.getValue();
            if (original.has(fieldName) && !original.get(fieldName).equals(newValue)) {
                original.set(fieldName, newValue);
            } else if (!original.has(fieldName)) {
                log.error("Field '{}' not found in original product info for user '{}', product code '{}'",
                        fieldName, object.getUniqueUserIdentifier(), object.getProductCode());
            }
        }
        log.info("Modified Product Info: {}", original);
        originalProductInfo.setProductInfo(original);
        long endTime = System.currentTimeMillis();
        log.info("Time taken by processUpdateAction() :->{} seconds", (endTime - startTime) / 1000.0);
        return originalProductInfo;
    }

    private void accept(UserProductInfoDto object) {
        switch (object.getActionCode().toUpperCase()) {
            case "A":
                object.setValidationErrors(validate(object.getProductInfo()));
                userInfoServices.saveProduct(object);
                break;
            case "R":
                object.setValidationErrors(validate(object.getProductInfo()));
                userInfoServices.updateProductInfo(object);
                break;
            case "D":
                userInfoServices.deleteProduct(object.getUniqueUserIdentifier(), object.getProductCode());
                break;
            case "E":
                UserProductInfoDto userProductInfoDto = processUpdateAction(object);
                userProductInfoDto.setValidationErrors(validate(userProductInfoDto.getProductInfo()));
                userInfoServices.updateProductInfo(userProductInfoDto);
                break;
            case "TU":
                UserProductInfoDto tempDto = processUpdateAction(object);
                tempDto.setValidationErrors(validate(object.getProductInfo()));
                break;
            default:
                log.error("Invalid Action code");
        }
    }

    private JsonNode validate(JsonNode productInfo) {
        try {
            List<ValidationError> validationErrors = validateProduct.validateProduct(productInfo);
            if (validationErrors.isEmpty()) {
                return null;
            } else {
                return convertValidationErrorsToJson(validationErrors);
            }
        } catch (JsonProcessingException e) {
            log.error("Exception while converting validation errors to json: - " + e.getMessage());
            return null;
        }
    }


    public JsonNode convertValidationErrorsToJson(List<ValidationError> validationErrors) {
        return validationErrors.stream()
                .map(error -> objectMapper.createObjectNode()
                        .put("fieldName", error.getFieldName())
                        .put("message", error.getMessage())
                        .set("actual", objectMapper.valueToTree(error.getActual())))
                .collect(Collectors.collectingAndThen(Collectors.toList(), objectMapper::valueToTree));
    }
}
