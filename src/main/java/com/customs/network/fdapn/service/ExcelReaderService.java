package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.CustomerFdaPnFailure;
import com.customs.network.fdapn.dto.ExcelResponse;
import com.customs.network.fdapn.exception.ErrorResCodes;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.model.TrackingDetails;
import com.customs.network.fdapn.model.ExcelColumn;
import com.customs.network.fdapn.model.PartyDetails;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


@Component
@AllArgsConstructor
@Slf4j
public class ExcelReaderService {
    private final ValidationService validationService;
    private final ExcelJsonProcessor excelJsonProcessor;
    private final ExcelWriter excelWriter;

    public String processExcelFile(MultipartFile file) {
        try {
            return readExcelFile(file);
        } catch (Exception e) {
            log.error("Error converting Excel to XML: -> {} ", e.getMessage());
            throw new FdapnCustomExceptions(ErrorResCodes.CONVERSION_FAILURE, "Error converting Excel to XML , " + e.getMessage());
        }
    }

    public String readExcelFile(MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new FdapnCustomExceptions(ErrorResCodes.EMPTY_DETAILS, "The uploaded file is empty.");
        }
        List<CustomerFdaPnFailure> excelResponseList = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        long endTime = System.currentTimeMillis();
        double executionTimeSeconds = (endTime - startTime) / 1000.0;
        log.info("Loading the excel took : {} seconds", executionTimeSeconds);
        List<Future<List<CustomerFdaPnFailure>>> futures = new ArrayList<>();
        Sheet sheet = workbook.getSheetAt(0);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        int endRow = sheet.getLastRowNum();
        int totalTransactions = 0;
        int currentRowNumber = -1;
        int chunkSize = 500;
        int chunkStart = 0;
        int chunkEnd = 0;
        startTime = System.currentTimeMillis();
        for (int i = 0; i <= endRow; i++) {
            Row row = sheet.getRow(i);
            if (i == endRow && chunkStart < endRow) {
                int finalChunkStart = chunkStart;
                futures.add(executorService.submit(() -> processRows(sheet, finalChunkStart, endRow)));
            }
            currentRowNumber++;
            if (currentRowNumber == 0) {
                continue;
            }
            if (row.getRowNum() == 1) {
                chunkStart = row.getRowNum();
                continue;
            }
            Cell firstCell = row.getCell(1);
            if (firstCell != null && firstCell.getCellType() != CellType.BLANK) {
                totalTransactions++;
                chunkEnd = row.getRowNum() - 1;

                if (totalTransactions % chunkSize == 0 && totalTransactions != 0) {
                    int finalChunkStart = chunkStart;
                    int finalChunkEnd = chunkEnd;
                    futures.add(executorService.submit(() -> processRows(sheet, finalChunkStart, finalChunkEnd)));
                    chunkStart = row.getRowNum() - 1;
                }
            }
        }
        endTime = System.currentTimeMillis();
        executionTimeSeconds = (endTime - startTime) / 1000.0;
        log.info("Chunking the excel completed in : {} seconds", executionTimeSeconds);
        new Thread(() -> {
            try {
                for (Future<List<CustomerFdaPnFailure>> future : futures) {
                    List<CustomerFdaPnFailure> futureResult = future.get();
                    if (!futureResult.isEmpty()) {
                        excelResponseList.addAll(futureResult);
                    }
                }

                if (!excelResponseList.isEmpty()) {
                    log.info("Validation errors are found");
                    excelWriter.writeExcel(excelResponseList);
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error(e.toString());
            }
        }).start();
        executorService.shutdown();
        workbook.close();
        return "Excel uploaded successfully";
    }

    private List<CustomerFdaPnFailure> processRows(Sheet sheet, int startRow, int endRow) throws Exception {
        List<ExcelResponse> excelResponseList = new ArrayList<>();
        TrackingDetails currentTrackingDetails = null;
        LinkedList<PartyDetails> partyDetailsList = new LinkedList<>();
        ExcelResponse excelResponse = null;
        for (int i = startRow; i <= endRow; i++) {
            Row currentRow = sheet.getRow(i);
            if (currentRow == null) {
                break;
            }
            Cell firstCell = currentRow.getCell(0);
            if (firstCell == null || firstCell.getCellType() == CellType.BLANK) {
                if (currentTrackingDetails != null) {
                    PartyDetails partyDetails = readPartyDetails(currentRow);
                    if (isRowEmpty(currentRow) && partyDetails == null) {
                        break;
                    }
                    if (partyDetails != null) {
                        partyDetailsList.add(partyDetails);
                    }
                }
            } else {
                currentTrackingDetails = new TrackingDetails();
                mapFields(TrackingDetails.class.getDeclaredFields(), currentTrackingDetails, currentRow);
                PartyDetails partyDetails = readPartyDetails(currentRow);
                partyDetailsList = new LinkedList<>();
                partyDetailsList.add(partyDetails);
                currentTrackingDetails.setPartyDetails(partyDetailsList);
                excelResponse = new ExcelResponse();
                excelResponse.setTrackingDetails(currentTrackingDetails);
                excelResponseList.add(excelResponse);
            }
        }
        List<ExcelResponse> excelResponses = validationService.validateField(excelResponseList);
        return excelJsonProcessor.processResponses(excelResponses);
    }

    private boolean isRowEmpty(Row row) {
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private PartyDetails readPartyDetails(Row row) throws Exception {
        if (isRowEmpty(row)) {
            return null;
        }
        PartyDetails partyDetails = new PartyDetails();
        mapFields(PartyDetails.class.getDeclaredFields(), partyDetails, row);
        return partyDetails;
    }

    private static void mapFields(Field[] fields, Object object, Row row) throws Exception {
        for (Field field : fields) {
            if (field.isAnnotationPresent(ExcelColumn.class)) {
                ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
                int columnIndex = excelColumn.index();
                Cell cell = row.getCell(columnIndex);
                setField(object, field, cell);
            }
        }
    }

    private static void setField(Object object, Field field, Cell cell) throws Exception {
        if (cell != null) {
            Class<?> fieldType = field.getType();
            field.setAccessible(true);
            if (fieldType == int.class || fieldType == Integer.class) {
                if (cell.getCellType() == CellType.NUMERIC) {
                    if (DateUtil.isCellDateFormatted(cell)) {
                        field.setInt(object, (int) cell.getNumericCellValue());
                    } else {
                        field.setInt(object, (int) cell.getNumericCellValue());
                    }
                } else if (cell.getCellType() == CellType.STRING) {
                    field.setInt(object, Integer.parseInt(cell.getStringCellValue()));
                }
            } else if (fieldType == long.class || fieldType == Long.class) {
                if (cell.getCellType() == CellType.NUMERIC) {
                    if (DateUtil.isCellDateFormatted(cell)) {
                        field.setLong(object, (long) cell.getDateCellValue().getTime());
                    } else {
                        field.setLong(object, (long) cell.getNumericCellValue());
                    }
                } else if (cell.getCellType() == CellType.STRING) {
                    field.setLong(object, Long.parseLong(cell.getStringCellValue()));
                }
            } else if (fieldType == String.class) {
                field.set(object, getStringCellValue(cell));
            }
        }
    }

    private static String getStringCellValue(Cell cell) {
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                double numericValue = cell.getNumericCellValue();
                if (numericValue < 1) {
                    LocalTime time = cell.getLocalDateTimeCellValue().toLocalTime();
                    return time.format(DateTimeFormatter.ofPattern("HHmm"));
                } else {
                    LocalDate date = cell.getLocalDateTimeCellValue().toLocalDate();
                    return date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                }
            } else {
                return String.valueOf((long) cell.getNumericCellValue());
            }
        } else {
            return "";
        }
    }
}
