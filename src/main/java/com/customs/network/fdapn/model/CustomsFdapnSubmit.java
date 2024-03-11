package com.customs.network.fdapn.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Date;

@Data
@Entity
@Table(name = "customs_fdapn_submit")
public class CustomsFdapnSubmit {
    @Id
    private String batchId;
    @Column
    private String traceId;
    @Column
    private String userId;
    @Column(name = "account_id")
    private String accountId;
    @Column(name = "reference_id")
    private String referenceId;
    @Column
    private String envelopNumber;
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedOn;
    @Column
    private String status;
    @Column(name = "request_json")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode requestJson;

    @Column(name = "response_json")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode responseJson;
}
