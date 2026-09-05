-- Ensure the baby recipe menu points to the existing Vue page.

UPDATE sys_menu
SET route_name = 'BabyRecipe',
    route_path = 'recipes',
    component = 'recipe/index',
    visible = 1,
    update_time = NOW()
WHERE id = 430;
