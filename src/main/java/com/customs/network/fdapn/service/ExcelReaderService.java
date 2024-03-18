package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.ExcelResponse;
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
import java.time.format.DateTimeFormatter;
import java.util.*;


@Component
@AllArgsConstructor
@Slf4j
public class ExcelReaderService {
    private final ValidationService validationService;
    private final ExcelJsonProcessor excelJsonProcessor;

    public Map<String, List<Object>> processExcelFile(MultipartFile file) {
        try {
            List<ExcelResponse> excelResponses = readExcelFile(file);
            return excelJsonProcessor.processResponses(excelResponses);
        } catch (Exception e) {
            log.error("Error converting Excel to XML: -> {} ", e.getMessage());
            throw new RuntimeException();
        }
    }
    public List<ExcelResponse> readExcelFile(MultipartFile file) throws Exception {
        List<ExcelResponse> excelResponseList = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        TrackingDetails currentTrackingDetails = null;
        LinkedList<PartyDetails> partyDetailsList = new LinkedList<>();
        ExcelResponse excelResponse = null;
        for (Row currentRow : sheet) {
            if (currentRow.getRowNum() == 0) {
                continue;
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
        workbook.close();
        return validationService.validateField(excelResponseList);
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
                LocalDate date = cell.getLocalDateTimeCellValue().toLocalDate();
                return date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            } else {
                return String.valueOf((long) cell.getNumericCellValue());
            }
        } else {
            return "";
        }
    }

}
