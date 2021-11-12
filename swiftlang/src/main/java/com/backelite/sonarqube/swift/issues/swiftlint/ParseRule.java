package com.backelite.sonarqube.swift.issues.swiftlint;

import org.json.simple.JSONObject;
import org.sonar.api.server.debt.DebtRemediationFunction;
import org.sonar.api.server.rule.RulesDefinition;

public class ParseRule {
    public void addDebtRemediation(JSONObject partialRule, RulesDefinition.NewRule rule) {
        RulesDefinition.DebtRemediationFunctions debtRemediationFunctions = rule.debtRemediationFunctions();
        String effort = (String) partialRule.get("effort");
        DebtRemediationFunction debtRemediationFunction = debtRemediationFunctions.constantPerIssue(effort);
        rule.setDebtRemediationFunction(debtRemediationFunction);
    }
}
