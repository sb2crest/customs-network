package com.customs.network.fdapn.model;

import lombok.Data;

@Data
public class PartyDetails {
    @ExcelColumn(index = 26)
    private String partyType;

    @ExcelColumn(index = 27)
    private String partyIdentifierType;

    @ExcelColumn(index = 28)
    private String partyIdentifierNumber;

    @ExcelColumn(index = 29)
    private String partyName;

    @ExcelColumn(index = 30)
    private String address1;

    @ExcelColumn(index = 31)
    private String address2;

    @ExcelColumn(index = 32)
    private String apartmentOrSuiteNo;

    @ExcelColumn(index = 33)
    private String city;

    @ExcelColumn(index = 34)
    private String stateOrProvince;

    @ExcelColumn(index = 35)
    private String country;

    @ExcelColumn(index = 36)
    private String postalCode;

    @ExcelColumn(index = 37)
    private String contactPerson;

    @ExcelColumn(index = 38)
    private String telephoneNumber;

    @ExcelColumn(index = 39)
    private String email;
}
