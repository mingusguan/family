package com.youlai.boot.recipe.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JumdataRecipeProperties.class)
public class RecipeIntegrationConfig {
}
