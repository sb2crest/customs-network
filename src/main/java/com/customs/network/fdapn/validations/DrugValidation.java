package com.customs.network.fdapn.validations;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@RequiredArgsConstructor
public class DrugValidation {

    private Map<String, List<RuleValuePair>> ruleMap = new HashMap<>();

    public void setupPG01ValidationRules() {
        List<RuleValuePair> pg01Rules = new ArrayList<>();
        pg01Rules.add(new RuleValuePair("Control Identifier", new DrugValidationRules(2, 2, true, "PG")));
        pg01Rules.add(new RuleValuePair("Record Type", new DrugValidationRules(2, 2, true, "01")));
        pg01Rules.add(new RuleValuePair("PGA Line Number", new DrugValidationRules(3, 3, true, null)));
        pg01Rules.add(new RuleValuePair("Government Agency Code", new DrugValidationRules(3, 3, true, "FDA")));
        pg01Rules.add(new RuleValuePair("Government Agency Program Code", new DrugValidationRules(3, 3, false, "DRU")));
        pg01Rules.add(new RuleValuePair("Government Agency Processing Code", new DrugValidationRules(3, 3, false, "PRE,OTC,INV,PHN,RND,804")));
        pg01Rules.add(new RuleValuePair("Intended Use Code", new DrugValidationRules(16, 16, true, "ValidValuesHere")));
        pg01Rules.add(new RuleValuePair("Intended Use Description", new DrugValidationRules(21, 21, false, null)));
        pg01Rules.add(new RuleValuePair("Disclaimer", new DrugValidationRules(1, 1, false, "A,F")));

        ruleMap.put("DrugValidations", pg01Rules);
    }

    public boolean validatePG01(Map<String, String> pg01Record) {
        List<RuleValuePair> rules = ruleMap.get("DrugValidations");
        if (rules == null) {
            throw new IllegalArgumentException("Validation rules for PG01 not found");
        }

        for (RuleValuePair pair : rules) {
            DrugValidationRules rule = pair.getRules();
            String value = pg01Record.get(pair.getKey());
            if (value == null) {
                return false;
            }
            if (!rule.validate(value)) {
                return false;
            }
        }

        return true;
    }
}
