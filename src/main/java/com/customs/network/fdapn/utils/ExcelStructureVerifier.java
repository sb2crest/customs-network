package com.customs.network.fdapn.utils;

import com.customs.network.fdapn.exception.ErrorResCodes;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Objects;

@Component
public class ExcelStructureVerifier {
    private ExcelStructureVerifier(){}

    public static boolean isExcelFile(MultipartFile file) {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        return fileName.endsWith(".xls") || fileName.endsWith(".xlsx");
    }
    public static void validateExcelStructure(Workbook workbook) {
        String[][] expectedHeaders = {
                {"Sl no", "Unique User Identifier", "Action Code", "Declaration", "Product Information ( Transaction Level )"},
                {"Sl no", "Unique User Identifier", "Product Identifier", "Action Code", "Product Information (Json)"},
                {"Sl no", "Unique User Identifier", "party identifier Id", "Action Code", "Party Information"}
        };

        Map<Integer, String> sheetNames = Map.of(
                1, "Transactions",
                2, "Basic Product Info",
                3, "Party Info"
        );

        for (int i = 0; i < expectedHeaders.length; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            if (!validateSheetHeader(sheet, expectedHeaders[i])) {
                throw new FdapnCustomExceptions(ErrorResCodes.INVALID_DETAILS,
                        String.format("Can not process the Excel, Structure not valid at sheet %d (%s)", i + 1, sheetNames.get(i + 1)));
            }
        }
    }

    private static boolean validateSheetHeader(Sheet sheet, String[] expectedHeaders) {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            return false;
        }

        int lastCellNum = headerRow.getLastCellNum();
        if (lastCellNum != expectedHeaders.length) {
            return false;
        }

        for (int i = 0; i < expectedHeaders.length; i++) {
            Cell cell = headerRow.getCell(i);
            if (cell == null || !cell.getStringCellValue().trim().equals(expectedHeaders[i])) {
                return false;
            }
        }

        return true; // Header validation successful
    }
}
