package com.customs.network.fdapn.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "monthly_audit")
@Data
public class MonthlyAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "month")
    private String month;

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
