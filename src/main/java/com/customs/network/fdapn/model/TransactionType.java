package com.customs.network.fdapn.model;

import lombok.Getter;

@Getter
public enum TransactionType {
    DAILY("today"),
    WEEKLY("week"),
    MONTHLY("monthly");

    private final String value;

    TransactionType(String value) {
        this.value = value;
    }

}
