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
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_product_info")
public class UserProductInfo {
    @Id
    private String productCode;
    private String userId;
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode productInfo;
}

