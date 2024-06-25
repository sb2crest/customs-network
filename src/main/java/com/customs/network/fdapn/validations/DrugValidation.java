package com.customs.network.fdapn.validations;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@RequiredArgsConstructor
public class DrugValidation {

    private Map<String, List<DrugValidationRules>> ruleMap = new HashMap<>();

    public void setupPG01ValidationRules() {
        List<DrugValidationRules> pg01Rules = List.of(
                new DrugValidationRules("Control Identifier", 2, 2, true, "PG"),
                new DrugValidationRules("Record Type", 2, 2, true, "01"),
                new DrugValidationRules("PGA Line Number", 3, 3, true, null),
                new DrugValidationRules("Government Agency Code", 3, 3, true, "FDA"),
                new DrugValidationRules("Government Agency Program Code", 3, 3, false, "DRU"),
                new DrugValidationRules("Government Agency Processing Code", 3, 3, false, "PRE,OTC,INV,PHN,RND,804"),
                new DrugValidationRules("Intended Use Code", 16, 16, true, "ValidValuesHere"),
                new DrugValidationRules("Intended Use Description", 21, 21, false, null),
                new DrugValidationRules("Disclaimer", 1, 1, false, "A,F")
        );

        ruleMap.put("PG01", pg01Rules);
    }

    public boolean validatePG01(Map<String, String> pg01Record) {
        List<DrugValidationRules> rules = ruleMap.get("PG01");
        if (rules == null) {
            throw new IllegalArgumentException("Validation rules for PG01 not found");
        }

        for (DrugValidationRules rule : rules) {
            String value = pg01Record.get(rule.getFieldName());
            if (!rule.validate(value)) {
                return false;
            }
        }

        return true;
    }

}
