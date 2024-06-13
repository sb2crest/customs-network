package com.customs.network.fdapn.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExcelBatchResponse {
    List<ExcelValidationResponse> successList;
    List<TransactionFailureResponse> failedList;
}
