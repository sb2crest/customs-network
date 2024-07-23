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

@Entity
@Data
@Table(name = "user_party_info")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPartyInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @JsonIgnore
    private Long id;

    private String partyIdentifierId;

    private String uniqueUserIdentifier;

    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode partyInfo;
}
