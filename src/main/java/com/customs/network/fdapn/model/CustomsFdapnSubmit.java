package com.customs.network.fdapn.model;

import jakarta.persistence.*;
import lombok.Data;
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
    @Temporal(TemporalType.DATE)
    private Date createdOn;
    @Column
    @Temporal(TemporalType.DATE)
    private Date updatedOn;
    @Column
    private String status;
    @Column(name = "json_data_column", columnDefinition = "jsonb")
    private String jsonData; // Keep jsonData as String

}
