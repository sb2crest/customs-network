package com.customs.network.fdapn.validations.objects;

import lombok.Data;

import java.util.List;

@Data
public class TradeOrBrandNameInfo {
    private String tradeOrBrandName;
    private List<AdditionalInformation> additionalInformations;
}
