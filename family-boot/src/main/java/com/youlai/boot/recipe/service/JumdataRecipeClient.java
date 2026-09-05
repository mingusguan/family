package com.youlai.boot.recipe.service;

import java.util.List;

public interface JumdataRecipeClient {

    /** Lists the remote recipe category tree. */
    List<JumdataRecipeCategory> listCategories();

    List<JumdataRecipeItem> searchBabyRecipes(List<String> keywords, int limitPerKeyword);

    /** Searches one remote page. */
    JumdataRecipePage searchRecipePage(String categoryValue, String categoryName, String keyword, int pageNum, int pageSize);

    /** Loads the latest remote payload for one selected recipe id. */
    JumdataRecipeItem getRecipeById(String categoryValue, String categoryName, String sourceRecipeId);



    /** Remote recipe category. */
    record JumdataRecipeCategory(
            String id,
            String name,
            String parentId,
            String queryValue,
            List<JumdataRecipeCategory> children
    ) {
    }


    record JumdataRecipeItem(
            String sourceRecipeId,
            String title,
            String summary,
            String coverUrl,
            String ingredientsText,
            String stepsText,
            String rawPayload
    ) {
    }

    record JumdataRecipePage(
            List<JumdataRecipeItem> records,
            long total,
            int pageNum,
            int pageSize,
            int totalPages
    ) {
    }
}
