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
    private long id;

    @Column(name="userId")
    private String userId;

    @Column(name="date")
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name="accepted")
    private long acceptedCount;

    @Column(name="rejected")
    private long rejectedCount;

    @Column(name="pending")
    private long pendingCount;

    @Column(name="cbpDown")
    private long cbpDownCount;

    @Column(name="Total_Transactions")
    private long totalTransactions;
}
