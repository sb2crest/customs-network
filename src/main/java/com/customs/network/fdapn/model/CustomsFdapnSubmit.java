package com.customs.network.fdapn.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class CustomsFdapnSubmit {
    private Long slNo;
    private String batchId;
    private String traceId;
    private String userId;
    private String accountId;
    private String referenceId;
    private String envelopNumber;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "UTC")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "UTC")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedOn;

    private String status;
    private JsonNode requestJson;
    private JsonNode responseJson;
}
