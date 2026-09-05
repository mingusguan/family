package com.youlai.boot.recipe.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.youlai.boot.common.base.BaseEntity;
import com.youlai.boot.recipe.enums.BabyRecipeRuleSeverityEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@TableName("baby_recipe_rule")
public class BabyRecipeRule extends BaseEntity {

    private String ruleCode;
    private Integer ruleVersion;
    private String ruleName;
    private Integer minMonthAge;
    private Integer maxMonthAge;
    private String matchType;
    private String keywords;
    private BabyRecipeRuleSeverityEnum severity;
    private String suggestion;
    private String evidenceSource;
    private String evidenceUrl;
    private LocalDate effectiveDate;
    private Integer enabled;

    public boolean appliesTo(Integer minAge, Integer maxAge) {
        int min = minAge == null ? 0 : minAge;
        int max = maxAge == null ? min : maxAge;
        return this.minMonthAge <= max && this.maxMonthAge >= min;
    }

    public static BabyRecipeRule create(String code, String name, Integer minAge, Integer maxAge, String matchType,
                                        String keywords, BabyRecipeRuleSeverityEnum severity, String suggestion,
                                        String evidenceSource, String evidenceUrl, LocalDate effectiveDate, Integer enabled) {
        BabyRecipeRule rule = new BabyRecipeRule();
        rule.ruleCode = code;
        rule.ruleVersion = 1;
        rule.apply(name, minAge, maxAge, matchType, keywords, severity, suggestion, evidenceSource, evidenceUrl, effectiveDate, enabled);
        return rule;
    }

    public void revise(String name, Integer minAge, Integer maxAge, String matchType, String keywords,
                       BabyRecipeRuleSeverityEnum severity, String suggestion, String evidenceSource,
                       String evidenceUrl, LocalDate effectiveDate, Integer enabled) {
        ruleVersion = ruleVersion == null ? 1 : ruleVersion + 1;
        apply(name, minAge, maxAge, matchType, keywords, severity, suggestion, evidenceSource, evidenceUrl, effectiveDate, enabled);
    }

    private void apply(String name, Integer minAge, Integer maxAge, String type, String words,
                       BabyRecipeRuleSeverityEnum level, String advice, String source, String url,
                       LocalDate date, Integer active) {
        ruleName = name;
        minMonthAge = minAge;
        maxMonthAge = maxAge;
        matchType = type;
        keywords = words;
        severity = level;
        suggestion = advice;
        evidenceSource = source;
        evidenceUrl = url;
        effectiveDate = date;
        enabled = active;
    }
}
