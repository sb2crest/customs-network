package com.customs.network.fdapn.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
@Data
public class FilterCriteriaDTO {
    @JsonFormat(pattern = "dd-MM-yyyy")
    private Date createdOn;
    private String status;
    private String userId;
    private int page;
    private int size;

}
