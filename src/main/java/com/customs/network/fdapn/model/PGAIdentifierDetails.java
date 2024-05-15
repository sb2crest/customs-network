package com.customs.network.fdapn.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "government_agency_program_code_info")
public class PGAIdentifierDetails {
    @Id
    public String governmentAgencyProgramCode;
    @JdbcTypeCode(SqlTypes.JSON)
    public JsonNode programCodeData;
}
