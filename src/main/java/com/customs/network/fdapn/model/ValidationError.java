package com.customs.network.fdapn.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationError {

    private String fieldName;
    private String message;
    private Object actual;
}
