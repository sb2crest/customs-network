package com.customs.network.fdapn.dto;

import lombok.Data;

@Data
public class PartialCodeRequest {
    private String industry;
    private String industryClass;
    private String subclass;
    private String pic;
    private String group;
}
