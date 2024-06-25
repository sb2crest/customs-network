package com.customs.network.fdapn.validations;

public class RuleValuePair {
    private String key;
    private DrugValidationRules rules;

    public RuleValuePair(String key, DrugValidationRules rules) {
        this.key = key;
        this.rules = rules;
    }

    public String getKey() {
        return key;
    }

    public DrugValidationRules getRules() {
        return rules;
    }
}
