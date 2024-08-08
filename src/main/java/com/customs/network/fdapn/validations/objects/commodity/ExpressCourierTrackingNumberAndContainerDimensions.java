package com.customs.network.fdapn.validations.objects.commodity;

import lombok.Data;

@Data
public class ExpressCourierTrackingNumberAndContainerDimensions {
    private String packageTrackingNumberCode;
    private String packageTrackingNumber;
    private String containerDimensionsOne;
    private String containerDimensionsTwo;
    private String containerDimensionsThree;
}
