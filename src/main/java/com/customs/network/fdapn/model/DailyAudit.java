package com.customs.network.fdapn.model;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name="daily_audit")
public class DailyAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="userId")
    private String userId;

    @Column(name="date")
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name="accepted")
    private Long acceptedCount;

    @Column(name="rejected")
    private Long rejectedCount;

    @Column(name="pending")
    private Long pendingCount;
    @Column(name = "validation_error")
    private Long validationErrorCount;

    @Column(name="cbpDown")
    private Long cbpDownCount;

    @Column(name="Total_Transactions")
    private Long totalTransactions;
}
