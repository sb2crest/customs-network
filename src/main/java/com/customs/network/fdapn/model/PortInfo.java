package com.customs.network.fdapn.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@Table(name = "port_info", schema = "public")
public class PortInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sno;

    @Column(name = "port_number")
    private Integer portNumber;

    @Column(name = "user_id")
    private String userId;

    @Column(name="date")
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "accepted_count")
    private Long acceptedCount;

    @Column(name = "pending_count")
    private Long pendingCount;

    @Column(name = "rejected_count")
    private Long rejectedCount;

    @Column(name = "total_count")
    private Long totalCount;

}