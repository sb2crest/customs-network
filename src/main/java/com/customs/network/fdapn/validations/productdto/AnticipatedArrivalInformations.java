package com.customs.network.fdapn.validations.productdto;

import lombok.Data;

@Data
public class AnticipatedArrivalInformations {
    private String anticipatedArrivalDate;
    private String anticipatedArrivalTime;
    private String inspectionOrArrivalLocationCode;
    private String inspectionOrArrivalLocation;
    private String anticipatedArrivalInformation;
}
