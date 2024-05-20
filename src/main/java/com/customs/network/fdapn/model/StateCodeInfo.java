package com.customs.network.fdapn.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "state_code_info")
public class StateCodeInfo {
    @Id
    private String countryCode;
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode stateCodes;
}
