package com.youlai.boot.recipe.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.youlai.boot.common.base.BaseEntity;
import com.youlai.boot.recipe.enums.BabyRecipeRuleSeverityEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("recipe_rule_hit")
public class RecipeRuleHit extends BaseEntity {

    private Long sourceId;
    private Long recipeId;
    private String ruleCode;
    private Integer ruleVersion;
    private BabyRecipeRuleSeverityEnum severity;
    private String matchedText;
    private String suggestion;
    private String evidenceSource;
    private String evidenceUrl;

    public static RecipeRuleHit of(Long sourceId, Long recipeId, BabyRecipeRule rule, String matchedText) {
        RecipeRuleHit hit = new RecipeRuleHit();
        hit.sourceId = sourceId;
        hit.recipeId = recipeId;
        hit.ruleCode = rule.getRuleCode();
        hit.ruleVersion = rule.getRuleVersion();
        hit.severity = rule.getSeverity();
        hit.matchedText = matchedText;
        hit.suggestion = rule.getSuggestion();
        hit.evidenceSource = rule.getEvidenceSource();
        hit.evidenceUrl = rule.getEvidenceUrl();
        return hit;
    }
}
