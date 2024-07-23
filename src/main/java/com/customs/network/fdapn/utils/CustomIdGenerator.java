package com.customs.network.fdapn.utils;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class CustomIdGenerator {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int MAX_LENGTH  = 7;


    public String generateRefId(String userId, Long lastIndex){
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

    public static String generatePartyIdentifierId(){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < MAX_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(SECURE_RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
