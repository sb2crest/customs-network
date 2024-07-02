package com.customs.network.fdapn.validations.objects;

import lombok.Data;

import java.util.List;

@Data
public class EntityDetails {
    private EntityData entityData;
    private EntityAddress entityAddress;
    private List<PointOfContact> pointOfContacts;
}
@Data
 class EntityData {
    private String address1;
    private String partyName;
    private String partyType;
    private String partyIdentifierType;
    private String partyIdentifierNumber;
    private List<AdditionalInformation> additionalInformations;
}
@Data
 class EntityAddress {
    private String address2;
    private String postalCode;
    private String stateOrProvince;
    private String apartmentOrSuiteNo;
    private String city;
    private String country;
    private List<AdditionalInformation> additionalInformations;
}
@Data
 class PointOfContact {
    private String contactPerson;
    private String email;
    private String telephoneNumber;
    private String individualQualifier;
    private List<AdditionalInformation> additionalInformations;
}

