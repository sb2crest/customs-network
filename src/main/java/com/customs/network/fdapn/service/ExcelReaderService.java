package com.customs.network.fdapn.service;

import com.customs.network.fdapn.model.CustomerDetails;
import com.customs.network.fdapn.model.ExcelColumn;
import com.customs.network.fdapn.model.PartyDetails;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class ExcelReaderService {
    private static final int DATA_ROW_INDEX = 1;

    public CustomerDetails mapExcelToCustomerDetails(Sheet sheet) throws Exception {
        CustomerDetails customerDetails = new CustomerDetails();
        Row dataRow = sheet.getRow(DATA_ROW_INDEX);

        mapFields(CustomerDetails.class.getDeclaredFields(), customerDetails, dataRow);

        LinkedList<PartyDetails> partyDetailsList = IntStream.range(1, sheet.getLastRowNum())
                .mapToObj(i -> {
                    Row partyRow = sheet.getRow(i);
                    PartyDetails partyDetails = new PartyDetails();
                    try {
                        mapFields(PartyDetails.class.getDeclaredFields(), partyDetails, partyRow);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return partyDetails;
                })
                .collect(Collectors.toCollection(LinkedList::new));

        customerDetails.setPartyDetails(partyDetailsList);
        return customerDetails;
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
                field.setInt(object, (int) cell.getNumericCellValue());
            } else if (fieldType == long.class || fieldType == Long.class) {
                field.setLong(object, (long) cell.getNumericCellValue());
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
}
