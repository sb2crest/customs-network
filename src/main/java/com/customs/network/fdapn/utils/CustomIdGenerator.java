package com.customs.network.fdapn.utils;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
@Component
public class CustomIdGenerator {
    public String generate(String userId, Long lastIndex){
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String currentDate=today.format(formatter);
        String recordNumber = String.format("%08d", lastIndex + 1);
        return userId+currentDate+recordNumber;
    }
    public Long extractIdFromRefId(String refId){
        String idSubString = refId.substring(Math.max(refId.length() - 8, 0));
        int value = Integer.parseInt(idSubString);
        return (long) value;
    }
    public String extractUserIdFromRefId(String refId){
        return refId.substring(0, 15);
    }
}
