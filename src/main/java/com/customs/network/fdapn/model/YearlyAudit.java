package com.customs.network.fdapn.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="yearly_audit")
public class YearlyAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "year")
    private String year;

    @Column(name = "accepted_count")
    private Long acceptedCount;

    @Column(name = "rejected_count")
    private Long rejectedCount;

    @Column(name = "pending_count")
    private Long pendingCount;

    @Column(name = "validation_error_count")
    private Long validationErrorCount;

    @Column(name = "cbp_down_count")
    private Long cbpDownCount;

    @Column(name = "total_transactions")
    private Long totalTransactions;
}
