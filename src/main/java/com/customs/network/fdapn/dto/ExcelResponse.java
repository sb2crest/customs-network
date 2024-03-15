package com.customs.network.fdapn.dto;

import com.customs.network.fdapn.model.TrackingDetails;
import com.customs.network.fdapn.model.ValidationError;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ExcelResponse {
    private TrackingDetails trackingDetails;
    private List<ValidationError> validationErrors;
}
