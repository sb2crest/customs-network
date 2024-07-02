package com.customs.network.fdapn.validations.objects;

import lombok.Data;

@Data
public class AnticipatedArrivalInformation {
    private String anticipatedArrivalDate;
    private String anticipatedArrivalTime;
    private String inspectionOrArrivalLocation;
    private String anticipatedArrivalInformation;
    private String inspectionOrArrivalLocationCode;
}
