package com.customs.network.fdapn.dto.productdto;

import lombok.Data;

@Data
public class PartyDetails {
    private String city;
    private String email;
    private String country;
    private String address1;
    private String address2;
    private String partyName;
    private String individualQualifier;
    private String partyType;
    private String postalCode;
    private String contactPerson;
    private String stateOrProvince;
    private String telephoneNumber;
    private String apartmentOrSuiteNo;
    private String partyIdentifierType;
    private String partyIdentifierNumber;
}
