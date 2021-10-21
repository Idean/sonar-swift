package com.backelite.sonarqube.swift.issues.swiflint;

import com.backelite.sonarqube.swift.issues.swiftlint.ParseRule;
import com.backelite.sonarqube.swift.lang.core.Swift;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.sonar.api.server.debt.DebtRemediationFunction;
import org.sonar.api.server.rule.RulesDefinition;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class ParseRuleTest {
    private RulesDefinition.Context context;
    private RulesDefinition.NewRepository newRepository;
    private RulesDefinition.NewRule newRule;

    private JSONObject initRule() {
        String jsonRules = "" +
                "[\n" +
                "    {\n" +
                "        \"key\": \"anyobject_protocol\",\n" +
                "        \"category\": \"SwiftLint\",\n" +
                "        \"name\": \"AnyObject Protocol\",\n" +
                "        \"description\": \"Prefer using `AnyObject` over `class` for class-only protocols.\",\n" +
                "        \"severity\": \"MINOR\",\n" +
                "        \"type\" : \"CONSTANT_ISSUE\",\n" +
                "        \"gap\" : \"0d 0h 5min\",\n" +
                "        \"effort\" : \"5min\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"key\": \"array_init\",\n" +
                "        \"category\": \"SwiftLint\",\n" +
                "        \"name\": \"Array Init\",\n" +
                "        \"description\": \"Prefer using `Array(seq)` over `seq.map { $0 }` to convert a sequence into an Array.\",\n" +
                "        \"severity\": \"MINOR\",\n" +
                "        \"type\" : \"CONSTANT_ISSUE\",\n" +
                "        \"gap\" : \"0d 0h 5min\",\n" +
                "        \"effort\" : \"5min\"\n" +
                "    }\n" +
                "]";
        JSONArray slRules = (JSONArray) JSONValue.parse(jsonRules);
        JSONObject jsonRule = (JSONObject) slRules.get(0);
        context = new RulesDefinition.Context();
        newRepository = context.createRepository("testRules", Swift.KEY);
        newRule = newRepository.createRule((String) jsonRule.get("key"))
                .setName((String) jsonRule.get("name"))
                .setSeverity((String) jsonRule.get("severity"))
                .setHtmlDescription((String) jsonRule.get("description"));
        return jsonRule;
    }

    @Test
    public void addDebtRemediation_addNewRule_ruleTypeCorrect() {
        // Arrange
        JSONObject partialRule = initRule();

        // Act
        new ParseRule().addDebtRemediation(partialRule, newRule);
        newRepository.done();
        RulesDefinition.Rule rule = context.repository("testRules").rule("anyobject_protocol");

        // Assert
        assert rule != null;
        assert rule.debtRemediationFunction() != null;
        assertThat(rule.debtRemediationFunction().type()).isEqualTo(DebtRemediationFunction.Type.CONSTANT_ISSUE);
    }

    @Test
    public void addDebtRemediation_addNewRule_ruleGapNull() {
        // Arrange
        JSONObject partialRule = initRule();

        // Act
        new ParseRule().addDebtRemediation(partialRule, newRule);
        newRepository.done();
        RulesDefinition.Rule rule = context.repository("testRules").rule("anyobject_protocol");

        // Assert
        assert rule != null;
        assert rule.debtRemediationFunction() != null;
        assertThat(rule.debtRemediationFunction().gapMultiplier()).isNull();
    }

    @Test
    public void addDebtRemediation_addNewRule_ruleBaseEffortHasTime() {
        // Arrange
        JSONObject partialRule = initRule();

        // Act
        new ParseRule().addDebtRemediation(partialRule, newRule);
        newRepository.done();
        RulesDefinition.Rule rule = context.repository("testRules").rule("anyobject_protocol");

        // Assert
        assert rule != null;
        assert rule.debtRemediationFunction() != null;
        LoggerFactory.getLogger(ParseRuleTest.class).info(rule.debtRemediationFunction().baseEffort());
        assertThat(rule.debtRemediationFunction().baseEffort()).isEqualTo("5min");
    }

    @Test
    public void addDebtRemediation_addNewRule_ruleDescriptionExist() {
        // Arrange
        JSONObject partialRule = initRule();

        // Act
        new ParseRule().addDebtRemediation(partialRule, newRule);
        newRepository.done();
        RulesDefinition.Rule rule = context.repository("testRules").rule("anyobject_protocol");

        // Assert
        assert rule != null;
        assert rule.debtRemediationFunction() != null;
        assertThat(rule.htmlDescription()).isEqualTo("Prefer using `AnyObject` over `class` for class-only protocols.");
    }
}
