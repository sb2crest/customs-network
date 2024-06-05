package com.customs.network.fdapn.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

import java.util.Date;
@Data
public class TransactionInfo {
    private Long slNo;
    private String batchId;
    private String traceId;
    private String uniqueUserIdentifier;
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
