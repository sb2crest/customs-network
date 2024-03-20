package com.customs.network.fdapn.dto;

import lombok.Data;

@Data
public class ScanSchema {
    private String fieldName;
    private String value;
    private String startDate;
    private String endDate;
    private String userId;
    private int page;
    private int size;
}
