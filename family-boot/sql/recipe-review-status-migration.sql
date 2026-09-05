-- Adjust Jumdata-imported recipe review states after removing automatic rejection.
-- PENDING_REVIEW means rule hits need manual attention; clean recipes are APPROVED directly.

UPDATE baby_recipe br
JOIN recipe_source rs ON rs.id = br.source_id
SET br.status = 'APPROVED',
    br.reviewed_at = COALESCE(br.reviewed_at, NOW())
WHERE rs.provider = 'JUMDATA'
  AND br.status = 'PENDING_REVIEW';

UPDATE baby_recipe br
JOIN recipe_source rs ON rs.id = br.source_id
SET br.status = 'PENDING_REVIEW',
    br.reviewed_at = NULL
WHERE rs.provider = 'JUMDATA'
  AND br.status = 'AUTO_REJECTED';
