package com.customs.network.fdapn.validations.objects;

import com.customs.network.fdapn.validations.objects.commodity.*;
import lombok.Data;

import java.util.List;

@Data
public class TransactionProductData {
    private String productIdentifier;
    private List<String> partyIdentifiers;
    private List<EntityDetails> partyDetails;
    private List<AnticipatedArrivalInformations> anticipatedArrivalInformations;
    private List<List<ProductPackaging>> productPackaging;
    private List<ProductCondition> productCondition;
    private List<ContainerInformation> containerInformation;
    private List<AffirmationOfCompliance> affirmationOfCompliance;
    private LicensePlateIssuer licensePlateIssuer;
    private LicensePlateNumber licensePlateNumber;
    private CourierTrackingAndDimensions courierTrackingAndDimensions;
}
