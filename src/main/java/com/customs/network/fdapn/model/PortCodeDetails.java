package com.customs.network.fdapn.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "port_code_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortCodeDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sno;

    private String country;

    private String state;

    private String portName;

    private Integer portCode;
}
