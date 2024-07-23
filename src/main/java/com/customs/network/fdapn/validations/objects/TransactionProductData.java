package com.customs.network.fdapn.validations.objects;

import lombok.Data;

import java.util.List;

@Data
public class TransactionProductData {
    private String productIdentifier;
    private List<String> partyIdentifiers;
    private List<EntityDetails> partyDetails;
    private List<AnticipatedArrivalInformation> anticipatedArrivalInformations;
    private List<List<ProductPackaging>> productPackaging;
    private List<ProductCondition> productCondition;
    private List<ContainerInformation> containerInformation;
}
