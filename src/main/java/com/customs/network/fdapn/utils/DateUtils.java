package com.customs.network.fdapn.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class DateUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd-MM-yyyy");
    private static final SimpleDateFormat DATE_FORMATTER_YYYY_MM_DD = new SimpleDateFormat("yyyy-MM-dd");

    public static String formatDate(Date date) {
        return DATE_FORMAT.format(date);
    }
    public static String formatterDate(Date date) {
        return DATE_FORMATTER_YYYY_MM_DD.format(date);
    }

    public static boolean isValidDate(String dateStr) {
        if (dateStr == null) {
            return false;
        }
        try {
            LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    public static boolean isValidDateFormat(String dateStr) {
        String regex = "\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\\d|3[01])";
        return dateStr.matches(regex);
    }

}
