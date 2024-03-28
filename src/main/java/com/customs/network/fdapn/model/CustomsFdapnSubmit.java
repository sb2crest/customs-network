package com.customs.network.fdapn.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
public class  CustomsFdapnSubmit {
    private Long slNo;
    private String batchId;
    private String traceId;
    private String userId;
    private String accountId;
    private String referenceId;
    private String envelopNumber;
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedOn;
    private String status;
    private JsonNode requestJson;

    private JsonNode responseJson;
}
