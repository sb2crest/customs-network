package com.customs.network.fdapn.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_product_info")
public class UserProductInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;
    private String uniqueUserIdentifier;
    private String productCode;
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode productInfo;
    private boolean isValid;
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode validationErrors;
}

