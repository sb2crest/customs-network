package com.customs.network.fdapn.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
}
