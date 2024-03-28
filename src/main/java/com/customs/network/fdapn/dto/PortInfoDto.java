package com.customs.network.fdapn.dto;

import lombok.Data;

@Data
public class PortInfoDto {
    private Long sno;
    private Integer portCode;
    private String userId;
    private String date;
    private long acceptedCount;
    private long rejectedCount;
    private long pendingCount;
    private long totalCount;

}