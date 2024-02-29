package com.customs.network.fdapn.service;

import com.customs.network.fdapn.model.CustomerDetails;
import com.customs.network.fdapn.model.ExcelColumn;
import com.customs.network.fdapn.model.PartyDetails;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExcelReaderService {
    private static final int DATA_ROW_INDEX = 1;

    public CustomerDetails mapExcelToCustomerDetails(Sheet sheet) throws Exception {
        CustomerDetails customerDetails = new CustomerDetails();
        Row dataRow = sheet.getRow(DATA_ROW_INDEX);

        for (Field field : CustomerDetails.class.getDeclaredFields()) {
            if (field.isAnnotationPresent(ExcelColumn.class)) {
                ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
                int columnIndex = excelColumn.index();
                Cell cell = dataRow.getCell(columnIndex);
                setField(customerDetails, field, cell);
            }
        }
        List<PartyDetails> partyDetailsList = new ArrayList<>();
        for (int i = 1; i < sheet.getLastRowNum(); i++) {
            Row partyRow = sheet.getRow(i);
            if (partyRow != null) {
                PartyDetails partyDetails = new PartyDetails();
                for (Field field : PartyDetails.class.getDeclaredFields()) {
                    if (field.isAnnotationPresent(ExcelColumn.class)) {
                        ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
                        int columnIndex = excelColumn.index();
                        Cell cell = partyRow.getCell(columnIndex);
                        setField(partyDetails, field, cell);
                    }
                }
                partyDetailsList.add(partyDetails);
            }
        }
        customerDetails.setPartyDetails(partyDetailsList);
        return customerDetails;
    }

    private static void setField(Object object, Field field, Cell cell) throws Exception {
        if (cell != null) {
            Class<?> fieldType = field.getType();
            field.setAccessible(true);
            if (fieldType == int.class || fieldType == Integer.class) {
                field.setInt(object, (int) getNumericCellValue(cell));
            } else if (fieldType == long.class || fieldType == Long.class) {
                field.setLong(object, (long) getNumericCellValue(cell));
            } else if (fieldType == String.class) {
                field.set(object, getStringCellValue(cell));
            }
        }
    }

    private static String getStringCellValue(Cell cell) {
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        } else {
            return "";
        }
    }

    private static double getNumericCellValue(Cell cell) {
        return cell == null ? 0 : cell.getNumericCellValue();
    }
}
