package com.customs.network.fdapn.utils;

import com.customs.network.fdapn.model.ExcelColumn;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class RowMapper {
    private RowMapper(){
    }
    public static void mapFields(Field[] fields, Object object, Row row) throws Exception {
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
