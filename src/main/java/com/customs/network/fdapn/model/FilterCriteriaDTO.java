package com.customs.network.fdapn.model;

import lombok.Data;

import java.util.Date;
@Data
public class FilterCriteriaDTO {
    private Date createdOn;
    private String status;
    private String referenceId;
    private String userId;
    private int page;
    private int size;

}
