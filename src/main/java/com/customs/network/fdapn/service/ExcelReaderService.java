package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.ExcelResponse;
import com.customs.network.fdapn.model.CustomerDetails;
import com.customs.network.fdapn.model.ExcelColumn;
import com.customs.network.fdapn.model.PartyDetails;
import com.customs.network.fdapn.model.ValidationError;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.customs.network.fdapn.service.ValidationService.convertToAMPMFormat;

@Component
@AllArgsConstructor
@Slf4j
public class ExcelReaderService {
    private final ValidationService validationService;
    private final FdaPnRecordSaver fdaPnRecordSaver;
    private static final int DATA_ROW_INDEX = 1;

    public Object processExcelFile(MultipartFile file) {
        try {
            Workbook workbook = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            ExcelResponse excelResponse = mapExcelToCustomerDetails(sheet);
            if (!excelResponse.getValidationErrors().isEmpty()) {
                return fdaPnRecordSaver.failureRecords(excelResponse);
            }
            fdaPnRecordSaver.save(excelResponse);
            return XmlConverterService.convertToXml(excelResponse.getCustomerDetails());
        } catch (Exception e) {
            log.error("Error converting Excel to XML: -> {} ", e.getMessage());
            return "Error converting Excel to XML: " + e.getMessage();
        }
    }
    private ExcelResponse mapExcelToCustomerDetails(Sheet sheet) throws Exception {
        ExcelResponse excelResponse = new ExcelResponse();
        CustomerDetails customerDetails = new CustomerDetails();
        Row dataRow = sheet.getRow(DATA_ROW_INDEX);

        mapFields(CustomerDetails.class.getDeclaredFields(), customerDetails, dataRow);

        LinkedList<PartyDetails> partyDetailsList = IntStream.range(1, sheet.getLastRowNum())
                .mapToObj(i -> {
                    Row partyRow = sheet.getRow(i);
                    if (partyRow == null || isRowEmpty(partyRow)) {
                        return null;
                    }
                    PartyDetails partyDetails = new PartyDetails();
                    try {
                        mapFields(PartyDetails.class.getDeclaredFields(), partyDetails, partyRow);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return partyDetails;
                })
                .takeWhile(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedList::new));

        customerDetails.setPartyDetails(partyDetailsList);
        excelResponse.setCustomerDetails(customerDetails);
        List<ValidationError> validationErrors = validationService.validateField(List.of(customerDetails));
        excelResponse.setValidationErrors(validationErrors);
        boolean arrivalTimeError = validationErrors.stream()
                .anyMatch(error -> "arrivalTime".equals(error.getFieldName()));
        if (!arrivalTimeError) {
            String arrivalTime = excelResponse.getCustomerDetails().getArrivalTime();
            String toAMPMFormat = convertToAMPMFormat(arrivalTime);
            excelResponse.getCustomerDetails().setArrivalTime(toAMPMFormat);
        }
        return excelResponse;
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
            return ""; // For other types, return an empty string
        }
    }

    // Method to check if a string represents a valid date
    private static boolean isDate(String value) {
        try {
            LocalDate.parse(value, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    // Method to check if a row is empty
    private boolean isRowEmpty(Row row) {
        for (int i = row.getFirstCellNum(); i <= row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false; // Row has at least one non-empty cell
            }
        }
        return true; // All cells are empty
    }
}
