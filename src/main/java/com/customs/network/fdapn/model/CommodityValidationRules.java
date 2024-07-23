package com.customs.network.fdapn.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
@Entity
@Table(name = "commodity_validation_rules")
public class CommodityValidationRules {
    @Id
    private String code;
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode rules;
}
