package com.youlai.boot.recipe.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.common.exception.BusinessException;
import com.youlai.boot.framework.security.util.SecurityUtils;
import com.youlai.boot.recipe.enums.BabyRecipeRuleSeverityEnum;
import com.youlai.boot.recipe.enums.BabyRecipeStatusEnum;
import cn.hutool.core.util.IdUtil;
import com.youlai.boot.recipe.enums.BabyRecipeTextureTypeEnum;
import com.youlai.boot.recipe.enums.RecipeSourceProviderEnum;
import com.youlai.boot.recipe.enums.RecipeAudienceTypeEnum;
import com.youlai.boot.recipe.mapper.BabyRecipeMapper;
import com.youlai.boot.recipe.mapper.BabyRecipeCategoryMapper;
import com.youlai.boot.recipe.mapper.BabyRecipeRuleMapper;
import com.youlai.boot.recipe.mapper.RecipeCookedRecordMapper;
import com.youlai.boot.recipe.mapper.RecipeFavoriteMapper;
import com.youlai.boot.recipe.mapper.RecipeRuleHitMapper;
import com.youlai.boot.recipe.mapper.RecipeSyncTaskMapper;
import com.youlai.boot.recipe.mapper.RecipeSourceMapper;
import com.youlai.boot.recipe.model.RecipeModels.BabyRecipeDetailVO;
import com.youlai.boot.recipe.model.RecipeModels.BabyRecipeQuery;
import com.youlai.boot.recipe.model.RecipeModels.BabyRecipeSaveRequest;
import com.youlai.boot.recipe.model.RecipeModels.BabyRecipeVO;
import com.youlai.boot.recipe.model.RecipeModels.JumdataCategorySyncItem;
import com.youlai.boot.recipe.model.RecipeModels.JumdataRecipeCategoryVO;
import com.youlai.boot.recipe.model.RecipeModels.JumdataRecipePreviewPageVO;
import com.youlai.boot.recipe.model.RecipeModels.JumdataRecipePreviewQuery;
import com.youlai.boot.recipe.model.RecipeModels.JumdataRecipePreviewVO;
import com.youlai.boot.recipe.model.RecipeModels.JumdataSelectedRecipeSyncRequest;
import com.youlai.boot.recipe.model.RecipeModels.JumdataRecipeSyncRequest;
import com.youlai.boot.recipe.model.RecipeModels.RecipeIngredientRequest;
import com.youlai.boot.recipe.model.RecipeModels.RecipeRuleHitVO;
import com.youlai.boot.recipe.model.RecipeModels.RecipeScreenResultVO;
import com.youlai.boot.recipe.model.RecipeModels.RecipeSyncResultVO;
import com.youlai.boot.recipe.model.RecipeModels.RecipeSyncFailureVO;
import com.youlai.boot.recipe.model.RecipeModels.RecipeRuleSaveRequest;
import com.youlai.boot.recipe.model.RecipeModels.RecipeRuleVO;
import com.youlai.boot.recipe.model.RecipeModels.RecipeStepRequest;
import com.youlai.boot.recipe.model.entity.BabyRecipe;
import com.youlai.boot.recipe.model.entity.BabyRecipeCategory;
import com.youlai.boot.recipe.model.RecipeModels.RecipeSyncTaskVO;
import com.youlai.boot.recipe.model.entity.BabyRecipeRule;
import com.youlai.boot.recipe.model.entity.RecipeCookedRecord;
import com.youlai.boot.recipe.model.entity.RecipeFavorite;
import com.youlai.boot.recipe.model.entity.RecipeRuleHit;
import com.youlai.boot.recipe.model.entity.RecipeSource;
import com.youlai.boot.recipe.service.BabyRecipeService;
import com.youlai.boot.recipe.service.JumdataRecipeClient;
import com.youlai.boot.recipe.service.JumdataRecipeClient.JumdataRecipeItem;
import com.youlai.boot.recipe.service.JumdataRecipeClient.JumdataRecipeCategory;
import com.youlai.boot.recipe.model.entity.RecipeSyncTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import com.youlai.boot.recipe.service.JumdataRecipeClient.JumdataRecipePage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.HashSet;
import jakarta.annotation.Resource;
import java.util.concurrent.Executor;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.concurrent.RejectedExecutionException;
import jakarta.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class BabyRecipeServiceImpl implements BabyRecipeService {

    private final BabyRecipeMapper babyRecipeMapper;
    private final BabyRecipeCategoryMapper babyRecipeCategoryMapper;
    private final BabyRecipeRuleMapper babyRecipeRuleMapper;
    private final RecipeRuleHitMapper recipeRuleHitMapper;
    private final RecipeFavoriteMapper recipeFavoriteMapper;
    private final RecipeCookedRecordMapper recipeCookedRecordMapper;
    private final RecipeSourceMapper recipeSourceMapper;
    private final RecipeSyncTaskMapper recipeSyncTaskMapper;
    @Resource(name = "recipeSyncExecutor")
    private Executor recipeSyncExecutor;
    private final JumdataRecipeClient jumdataRecipeClient;
    private final PlatformTransactionManager transactionManager;

    @PostConstruct
    public void recoverInterruptedTasks() {
        List<RecipeSyncTask> interrupted = recipeSyncTaskMapper.selectList(new LambdaQueryWrapper<RecipeSyncTask>()
                .in(RecipeSyncTask::getStatus, "PENDING", "RUNNING"));
        interrupted.forEach(RecipeSyncTask::recoverAsFailed);
        if (CollectionUtil.isNotEmpty(interrupted)) recipeSyncTaskMapper.updateById(interrupted, 100);
    }

    @Override
    public IPage<BabyRecipeVO> getAppRecipePage(BabyRecipeQuery query) {
        query.setStatus(BabyRecipeStatusEnum.PUBLISHED);
        IPage<BabyRecipeVO> page = getRecipePage(query, resolveAppScopedRecipeIds(query));
        markAppUserStatus(page.getRecords());
        return page;
    }

    @Override
    public BabyRecipeDetailVO getAppRecipeDetail(Long id) {
        BabyRecipe recipe = getPublishedRecipe(id);
        BabyRecipeDetailVO detail = toDetailVO(recipe);
        detail.setFavorite(isFavorite(id));
        detail.setCooked(isCooked(id));
        return detail;
    }

    @Override
    public List<String> listAppRecipeCategories(RecipeAudienceTypeEnum audienceType) {
        List<BabyRecipe> publishedRecipes = babyRecipeMapper.selectList(new LambdaQueryWrapper<BabyRecipe>()
                .select(BabyRecipe::getId, BabyRecipe::getCategoryName)
                .eq(BabyRecipe::getStatus, BabyRecipeStatusEnum.PUBLISHED)
                .eq(audienceType != null, BabyRecipe::getAudienceType, audienceType));
        if (CollectionUtil.isEmpty(publishedRecipes)) {
            return List.of();
        }
        Set<Long> publishedRecipeIds = publishedRecipes.stream()
                .map(BabyRecipe::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<String> categories = new ArrayList<>(publishedRecipes.stream()
                .map(BabyRecipe::getCategoryName)
                .filter(StrUtil::isNotBlank)
                .toList());
        if (CollectionUtil.isNotEmpty(publishedRecipeIds)) {
            categories.addAll(babyRecipeCategoryMapper.selectList(new LambdaQueryWrapper<BabyRecipeCategory>()
                            .select(BabyRecipeCategory::getCategoryName)
                            .in(BabyRecipeCategory::getRecipeId, publishedRecipeIds))
                    .stream()
                    .map(BabyRecipeCategory::getCategoryName)
                    .filter(StrUtil::isNotBlank)
                    .toList());
        }
        return categories.stream()
                .map(StrUtil::trim)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .sorted()
                .toList();
    }

    @Override
    public boolean favorite(Long id) {
        getPublishedRecipe(id);
        Long userId = requireCurrentUserId();
        boolean exists = recipeFavoriteMapper.selectCount(new LambdaQueryWrapper<RecipeFavorite>()
                .eq(RecipeFavorite::getUserId, userId)
                .eq(RecipeFavorite::getRecipeId, id)) > 0;
        if (exists) {
            return true;
        }
        RecipeFavorite favorite = new RecipeFavorite();
        favorite.setUserId(userId);
        favorite.setRecipeId(id);
        return recipeFavoriteMapper.insert(favorite) > 0;
    }

    @Override
    public boolean cancelFavorite(Long id) {
        Long userId = requireCurrentUserId();
        return recipeFavoriteMapper.delete(new LambdaQueryWrapper<RecipeFavorite>()
                .eq(RecipeFavorite::getUserId, userId)
                .eq(RecipeFavorite::getRecipeId, id)) >= 0;
    }

    @Override
    public boolean markCooked(Long id) {
        getPublishedRecipe(id);
        Long userId = requireCurrentUserId();
        boolean exists = recipeCookedRecordMapper.selectCount(new LambdaQueryWrapper<RecipeCookedRecord>()
                .eq(RecipeCookedRecord::getUserId, userId)
                .eq(RecipeCookedRecord::getRecipeId, id)) > 0;
        if (exists) {
            return true;
        }
        RecipeCookedRecord record = new RecipeCookedRecord();
        record.setUserId(userId);
        record.setRecipeId(id);
        record.setCookedAt(LocalDateTime.now());
        return recipeCookedRecordMapper.insert(record) > 0;
    }

    @Override
    public IPage<BabyRecipeVO> getManagementPage(BabyRecipeQuery query) {
        return getRecipePage(query, null);
    }

    private IPage<BabyRecipeVO> getRecipePage(BabyRecipeQuery query, List<Long> scopedRecipeIds) {
        if (scopedRecipeIds != null && CollectionUtil.isEmpty(scopedRecipeIds)) {
            Page<BabyRecipeVO> empty = new Page<>(query.getPageNum(), query.getPageSize(), 0);
            empty.setRecords(List.of());
            return empty;
        }
        LambdaQueryWrapper<BabyRecipe> wrapper = buildRecipeQuery(query);
        wrapper.in(scopedRecipeIds != null, BabyRecipe::getId, scopedRecipeIds == null ? List.of() : scopedRecipeIds);
        Page<BabyRecipe> entityPage = babyRecipeMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()),
                wrapper
        );
        Page<BabyRecipeVO> result = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        result.setRecords(entityPage.getRecords().stream().map(this::toVO).toList());
        return result;
    }

    @Override
    public List<String> listRecipeCategories() {
        List<String> categories = babyRecipeCategoryMapper.selectObjs(new LambdaQueryWrapper<BabyRecipeCategory>()
                        .select(BabyRecipeCategory::getCategoryName)
                        .isNotNull(BabyRecipeCategory::getCategoryName)
                        .ne(BabyRecipeCategory::getCategoryName, "")
                        .groupBy(BabyRecipeCategory::getCategoryName)
                        .orderByAsc(BabyRecipeCategory::getCategoryName))
                .stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .toList();
        if (CollectionUtil.isNotEmpty(categories)) {
            return categories;
        }
        return babyRecipeMapper.selectObjs(new LambdaQueryWrapper<BabyRecipe>()
                        .select(BabyRecipe::getCategoryName)
                        .isNotNull(BabyRecipe::getCategoryName)
                        .ne(BabyRecipe::getCategoryName, "")
                        .groupBy(BabyRecipe::getCategoryName)
                        .orderByAsc(BabyRecipe::getCategoryName))
                .stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .toList();
    }

    @Override
    public BabyRecipeDetailVO getManagementDetail(Long id) {
        return toDetailVO(getRecipe(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveRecipe(BabyRecipeSaveRequest request) {
        validateAgeRange(request);
        BabyRecipe recipe = BabyRecipe.create(
                request,
                JSONUtil.toJsonStr(request.getIngredients()),
                JSONUtil.toJsonStr(request.getSteps())
        );
        babyRecipeMapper.insert(recipe);
        ensureRecipeCategory(recipe, RecipeSourceProviderEnum.MANUAL, request.getCategoryId(), request.getCategoryName());
        return recipe.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRecipe(Long id, BabyRecipeSaveRequest request) {
        validateAgeRange(request);
        BabyRecipe recipe = getRecipe(id);
        recipe.update(request, JSONUtil.toJsonStr(request.getIngredients()), JSONUtil.toJsonStr(request.getSteps()));
        recipeRuleHitMapper.delete(new LambdaQueryWrapper<RecipeRuleHit>().eq(RecipeRuleHit::getRecipeId, id));
        boolean updated = babyRecipeMapper.updateById(recipe) > 0;
        ensureRecipeCategory(recipe, RecipeSourceProviderEnum.MANUAL, request.getCategoryId(), request.getCategoryName());
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RecipeScreenResultVO screenRecipe(Long id) {
        BabyRecipe recipe = getRecipe(id);
        recipeRuleHitMapper.delete(new LambdaQueryWrapper<RecipeRuleHit>().eq(RecipeRuleHit::getRecipeId, id));

        List<RecipeRuleHit> hits = findRuleHits(recipe);
        if (CollectionUtil.isNotEmpty(hits)) {
            recipeRuleHitMapper.insert(hits);
        }
        recipe.markAutoScreened(buildRuleSetVersion());
        babyRecipeMapper.updateById(recipe);

        RecipeScreenResultVO result = new RecipeScreenResultVO();
        result.setRecipeId(id);
        result.setStatus(recipe.getStatus());
        result.setHits(hits.stream().map(this::toHitVO).toList());
        return result;
    }

    @Override
    public boolean approveRecipe(Long id) {
        BabyRecipe recipe = getRecipe(id);
        requireCurrentRuleSet(recipe);
        recipe.approve(requireCurrentUserId());
        return babyRecipeMapper.updateById(recipe) > 0;
    }

    @Override
    public boolean rejectRecipe(Long id) {
        BabyRecipe recipe = getRecipe(id);
        recipe.reject(requireCurrentUserId());
        return babyRecipeMapper.updateById(recipe) > 0;
    }

    @Override
    public boolean publishRecipe(Long id) {
        BabyRecipe recipe = getRecipe(id);
        requireCurrentRuleSet(recipe);
        recipe.publish();
        return babyRecipeMapper.updateById(recipe) > 0;
    }

    @Override
    public boolean offlineRecipe(Long id) {
        BabyRecipe recipe = getRecipe(id);
        recipe.offline();
        return babyRecipeMapper.updateById(recipe) > 0;
    }

    @Override
    public List<JumdataRecipeCategoryVO> listJumdataCategories() {
        return jumdataRecipeClient.listCategories().stream().map(this::toCategoryVO).toList();
    }


    @Override
    public JumdataRecipePreviewPageVO previewJumdataRecipes(JumdataRecipePreviewQuery query) {
        JumdataRecipePage remotePage = jumdataRecipeClient.searchRecipePage(
                query.getCategoryId(), query.getCategoryName(), query.getKeyword(),
                query.getPageNum(), Math.min(query.getPageSize(), 50)
        );
        Map<String, RecipeSource> existing = listExistingJumdataSources(remotePage.records());
        List<JumdataRecipePreviewVO> available = remotePage.records().stream()
                .filter(item -> !existing.containsKey(item.sourceRecipeId()))
                .map(this::toPreviewVO)
                .toList();
        JumdataRecipePreviewPageVO result = new JumdataRecipePreviewPageVO();
        result.setList(available);
        result.setRemoteTotal(remotePage.total());
        result.setPageNum(remotePage.pageNum());
        result.setPageSize(remotePage.pageSize());
        result.setTotalPages(remotePage.totalPages());
        result.setExcludedLocalCount(remotePage.records().size() - available.size());
        LambdaQueryWrapper<RecipeSource> sourceQuery = new LambdaQueryWrapper<RecipeSource>()
                .eq(RecipeSource::getProvider, RecipeSourceProviderEnum.JISU)
                .eq(RecipeSource::getCategoryId, query.getCategoryId());
        if (StrUtil.isNotBlank(query.getCategoryName())) {
            sourceQuery.eq(RecipeSource::getCategoryName, query.getCategoryName());
        }
        long localCategoryCount = recipeSourceMapper.selectCount(sourceQuery);
        result.setAvailableTotalEstimate(Math.max(0, remotePage.total() - localCategoryCount));
        return result;
    }

    @Override
    public RecipeSyncTaskVO startCategorySyncTask(JumdataRecipeSyncRequest request) {
        if (request == null || CollectionUtil.isEmpty(request.getCategories())) {
            throw new BusinessException("请选择至少一个极速数据分类");
        }
        List<JumdataCategorySyncItem> categories = request.getCategories().stream()
                .filter(Objects::nonNull)
                .peek(category -> {
                    category.setCategoryId(StrUtil.trim(category.getCategoryId()));
                    category.setCategoryName(StrUtil.trim(category.getCategoryName()));
                    category.setKeyword(StrUtil.trim(category.getKeyword()));
                })
                .filter(category -> StrUtil.isNotBlank(category.getCategoryId()) && StrUtil.isNotBlank(category.getCategoryName()))
                .toList();
        if (CollectionUtil.isEmpty(categories)) {
            throw new BusinessException("请选择至少一个极速数据分类");
        }
        for (JumdataCategorySyncItem category : categories) {
            if (category.getLimit() == null || category.getLimit() < 1 || category.getLimit() > 10000) {
                throw new BusinessException("同步数量必须在1到10000之间");
            }
        }
        request.setCategories(categories);
        int total = categories.stream().mapToInt(JumdataCategorySyncItem::getLimit).sum();
        RecipeSyncTask task = createSyncTask("CATEGORY_COUNT", request, total);
        submitSyncTask(task, () -> runCategorySyncTask(task.getId(), request));
        return toTaskVO(task);
    }

    @Override
    public RecipeSyncTaskVO startSelectedSyncTask(JumdataSelectedRecipeSyncRequest request) {
        if (request == null || CollectionUtil.isEmpty(request.getSourceRecipeIds())) {
            throw new BusinessException("请选择至少一个菜谱");
        }
        request.setCategoryId(StrUtil.trim(request.getCategoryId()));
        request.setCategoryName(StrUtil.trim(request.getCategoryName()));
        if (StrUtil.isBlank(request.getCategoryId()) || StrUtil.isBlank(request.getCategoryName())) {
            throw new BusinessException("请选择同步分类");
        }
        List<String> distinctIds = request.getSourceRecipeIds().stream()
                .map(StrUtil::trim)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .toList();
        if (CollectionUtil.isEmpty(distinctIds)) {
            throw new BusinessException("请选择至少一个菜谱");
        }
        request.setSourceRecipeIds(distinctIds);
        RecipeSyncTask task = createSyncTask("SELECTED_RECIPES", request, distinctIds.size());
        submitSyncTask(task, () -> runSelectedSyncTask(task.getId(), request));
        return toTaskVO(task);
    }

    @Override
    public RecipeSyncTaskVO getSyncTask(String taskNo) {
        RecipeSyncTask task = recipeSyncTaskMapper.selectOne(new LambdaQueryWrapper<RecipeSyncTask>()
                .eq(RecipeSyncTask::getTaskNo, taskNo));
        if (task == null) throw new BusinessException("Recipe sync task does not exist");
        return toTaskVO(task);
    }

    @Override
    public List<RecipeSyncTaskVO> listRecentSyncTasks() {
        return recipeSyncTaskMapper.selectList(new LambdaQueryWrapper<RecipeSyncTask>()
                        .orderByDesc(RecipeSyncTask::getCreateTime).last("LIMIT 20"))
                .stream().map(this::toTaskVO).toList();
    }

    private void submitSyncTask(RecipeSyncTask task, Runnable runnable) {
        try {
            recipeSyncExecutor.execute(runnable);
        } catch (RejectedExecutionException exception) {
            task.fail(new BusinessException("同步任务队列已满，请稍后重试"));
            recipeSyncTaskMapper.updateById(task);
            throw new BusinessException("同步任务队列已满，请稍后重试");
        }
    }

    private RecipeSyncTask createSyncTask(String mode, Object request, int total) {
        RecipeSyncTask task = RecipeSyncTask.pending(IdUtil.fastSimpleUUID(), mode, JSONUtil.toJsonStr(request), total);
        recipeSyncTaskMapper.insert(task);
        return task;
    }

    private void runCategorySyncTask(Long taskId, JumdataRecipeSyncRequest request) {
        RecipeSyncTask task = recipeSyncTaskMapper.selectById(taskId);
        try {
            task.start();
            recipeSyncTaskMapper.updateById(task);
            for (JumdataCategorySyncItem category : request.getCategories()) {
                int remaining = category.getLimit() == null ? 0 : Math.max(0, category.getLimit());
                for (int page = 1; remaining > 0; page++) {
                    JumdataRecipePage remotePage = jumdataRecipeClient.searchRecipePage(
                            category.getCategoryId(), category.getCategoryName(), category.getKeyword(), page, 20);
                    if (remotePage.records().isEmpty()) break;
                    List<JumdataRecipeItem> items = remotePage.records().stream().limit(remaining).toList();
                    RecipeSyncResultVO result = importInTransaction(items, category.getCategoryId(), category.getCategoryName(), category.getAudienceType());
                    addTaskProgress(task, result, 0);
                    remaining -= items.size();
                    if (page >= remotePage.totalPages()) break;
                }
            }
            task.succeed();
            recipeSyncTaskMapper.updateById(task);
        } catch (Exception exception) {
            task.fail(exception);
            recipeSyncTaskMapper.updateById(task);
        }
    }

    private void runSelectedSyncTask(Long taskId, JumdataSelectedRecipeSyncRequest request) {
        RecipeSyncTask task = recipeSyncTaskMapper.selectById(taskId);
        List<RecipeSyncFailureVO> failures = new ArrayList<>();
        try {
            task.start();
            recipeSyncTaskMapper.updateById(task);
            for (String sourceRecipeId : request.getSourceRecipeIds()) {
                try {
                    JumdataRecipeItem item = jumdataRecipeClient.getRecipeById(
                            request.getCategoryId(), request.getCategoryName(), sourceRecipeId);
                    addTaskProgress(task, importInTransaction(List.of(item), request.getCategoryId(), request.getCategoryName(), request.getAudienceType()), 0);
                } catch (Exception itemException) {
                    RecipeSyncFailureVO failure = new RecipeSyncFailureVO();
                    failure.setSourceRecipeId(sourceRecipeId);
                    failure.setMessage(StrUtil.blankToDefault(itemException.getMessage(), itemException.getClass().getSimpleName()));
                    failures.add(failure);
                    task.addProgress(1, 0, 0, 0, 0, 1);
                    recipeSyncTaskMapper.updateById(task);
                }
            }
            task.recordFailures(JSONUtil.toJsonStr(failures));
            task.succeed();
            recipeSyncTaskMapper.updateById(task);
        } catch (Exception exception) {
            task.fail(exception);
            recipeSyncTaskMapper.updateById(task);
        }
    }

    private void addTaskProgress(RecipeSyncTask task, RecipeSyncResultVO result, int failed) {
        task.addProgress(result.getFetchedCount(), result.getImportedCount(), result.getDuplicatedCount(),
                result.getBlockedCount(), result.getWarnedCount(), failed);
        recipeSyncTaskMapper.updateById(task);
    }

    private JumdataRecipePreviewVO toPreviewVO(JumdataRecipeItem item) {
        JumdataRecipePreviewVO vo = new JumdataRecipePreviewVO();
        vo.setSourceRecipeId(item.sourceRecipeId());
        vo.setTitle(item.title());
        vo.setSummary(item.summary());
        vo.setCoverUrl(normalizeExternalImageUrl(item.coverUrl()));
        return vo;
    }

    private RecipeSyncTaskVO toTaskVO(RecipeSyncTask task) {
        RecipeSyncTaskVO vo = new RecipeSyncTaskVO();
        vo.setTaskNo(task.getTaskNo());
        vo.setMode(task.getMode());
        vo.setStatus(task.getStatus());
        vo.setTotalCount(task.getTotalCount());
        vo.setRequestedCount(task.getRequestedCount());
        vo.setProcessedCount(task.getProcessedCount());
        vo.setImportedCount(task.getImportedCount());
        vo.setDuplicatedCount(task.getDuplicatedCount());
        vo.setBlockedCount(task.getBlockedCount());
        vo.setWarnedCount(task.getWarnedCount());
        vo.setFailedCount(task.getFailedCount());
        vo.setErrorMessage(task.getErrorMessage());
        if (StrUtil.isNotBlank(task.getFailureDetailsJson())) {
            vo.setFailures(JSONUtil.toList(JSONUtil.parseArray(task.getFailureDetailsJson()), RecipeSyncFailureVO.class));
        }
        vo.setStartedAt(task.getStartedAt());
        vo.setFinishedAt(task.getFinishedAt());
        return vo;
    }
    private RecipeSyncResultVO importInTransaction(List<JumdataRecipeItem> fetchedItems, String categoryId, String categoryName, RecipeAudienceTypeEnum audienceType) {
        RecipeSyncResultVO result = new TransactionTemplate(transactionManager)
                .execute(status -> importJumdataItems(fetchedItems, categoryId, categoryName, audienceType));
        if (result == null) throw new BusinessException("食谱批次入库事务未返回结果");
        return result;
    }

    private RecipeSyncResultVO importJumdataItems(List<JumdataRecipeItem> fetchedItems, String categoryId, String categoryName, RecipeAudienceTypeEnum audienceType) {
        if (CollectionUtil.isEmpty(fetchedItems)) {
            return buildSyncResult(0, 0, 0, List.of());
        }

        Map<String, RecipeSource> existingSources = listExistingJumdataSources(fetchedItems);
        List<JumdataRecipeItem> newItems = fetchedItems.stream()
                .filter(item -> !existingSources.containsKey(item.sourceRecipeId()))
                .toList();

        List<RecipeSource> sources = new ArrayList<>();
        for (JumdataRecipeItem item : newItems) {
            sources.add(buildJumdataSource(item, categoryId, categoryName));
        }
        if (CollectionUtil.isNotEmpty(sources)) {
            recipeSourceMapper.insert(sources, 50);
        }
        Map<String, RecipeSource> insertedSources = sources.stream().collect(Collectors.toMap(RecipeSource::getProviderRecipeId, Function.identity()));
        List<BabyRecipe> importedRecipes = newItems.stream()
                .map(item -> buildPendingRecipeFromJumdata(item, insertedSources.get(item.sourceRecipeId()).getId(), categoryId, categoryName, audienceType))
                .toList();
        if (CollectionUtil.isNotEmpty(importedRecipes)) {
            babyRecipeMapper.insert(importedRecipes, 50);
        }
        Map<Long, BabyRecipe> recipesBySourceId = listRecipesBySourceId(existingSources.values().stream()
                .map(RecipeSource::getId)
                .filter(Objects::nonNull)
                .toList());
        importedRecipes.forEach(recipe -> recipesBySourceId.put(recipe.getSourceId(), recipe));
        for (JumdataRecipeItem item : fetchedItems) {
            RecipeSource source = insertedSources.getOrDefault(item.sourceRecipeId(), existingSources.get(item.sourceRecipeId()));
            if (source == null) {
                continue;
            }
            BabyRecipe recipe = recipesBySourceId.get(source.getId());
            ensureRecipeCategory(recipe, RecipeSourceProviderEnum.JISU, categoryId, categoryName);
        }
        List<RecipeRuleHit> allHits = new ArrayList<>();
        for (BabyRecipe recipe : importedRecipes) {
            List<RecipeRuleHit> hits = findRuleHits(recipe);
            allHits.addAll(hits);
            recipe.markAutoScreened(buildRuleSetVersion());
        }
        if (CollectionUtil.isNotEmpty(allHits)) recipeRuleHitMapper.insert(allHits, 100);
        if (CollectionUtil.isNotEmpty(importedRecipes)) {
            babyRecipeMapper.updateById(importedRecipes, 50);
        }
        return buildSyncResult(
                fetchedItems.size(),
                importedRecipes.size(),
                fetchedItems.size() - importedRecipes.size(),
                importedRecipes
        );
    }

    private JumdataRecipeCategoryVO toCategoryVO(JumdataRecipeCategory category) {
        JumdataRecipeCategoryVO vo = new JumdataRecipeCategoryVO();
        vo.setId(category.id());
        vo.setName(category.name());
        vo.setParentId(category.parentId());
        vo.setQueryValue(category.queryValue());
        vo.setChildren(category.children().stream().map(this::toCategoryVO).toList());
        return vo;
    }

    private LambdaQueryWrapper<BabyRecipe> buildRecipeQuery(BabyRecipeQuery query) {
        String keyword = StrUtil.trim(query.getKeyword());
        List<Long> categoryRecipeIds = listRecipeIdsByCategory(query.getCategoryName());
        return new LambdaQueryWrapper<BabyRecipe>()
                .and(StrUtil.isNotBlank(keyword), condition -> condition
                        .like(BabyRecipe::getTitle, keyword)
                        .or()
                        .like(BabyRecipe::getSummary, keyword)
                        .or()
                        .like(BabyRecipe::getIngredientsJson, keyword))
                .le(query.getMonthAge() != null, BabyRecipe::getMinMonthAge, query.getMonthAge())
                .ge(query.getMonthAge() != null, BabyRecipe::getMaxMonthAge, query.getMonthAge())
                .eq(query.getTextureType() != null, BabyRecipe::getTextureType, query.getTextureType())
                .eq(query.getMealType() != null, BabyRecipe::getMealType, query.getMealType())
                .eq(query.getAudienceType() != null, BabyRecipe::getAudienceType, query.getAudienceType())
                .eq(query.getStatus() != null, BabyRecipe::getStatus, query.getStatus())
                .in(categoryRecipeIds != null, BabyRecipe::getId, categoryRecipeIds == null ? List.of() : categoryRecipeIds)
                .orderByDesc(BabyRecipe::getPublishedAt)
                .orderByDesc(BabyRecipe::getCreateTime);
    }

    private List<Long> listRecipeIdsByCategory(String categoryName) {
        String actualCategoryName = StrUtil.trim(categoryName);
        if (StrUtil.isBlank(actualCategoryName)) {
            return null;
        }
        List<Long> recipeIds = babyRecipeCategoryMapper.selectList(new LambdaQueryWrapper<BabyRecipeCategory>()
                        .select(BabyRecipeCategory::getRecipeId)
                        .and(wrapper -> wrapper
                                .eq(BabyRecipeCategory::getCategoryName, actualCategoryName)
                                .or()
                                .likeRight(BabyRecipeCategory::getCategoryName, actualCategoryName + " /")))
                .stream()
                .map(BabyRecipeCategory::getRecipeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (CollectionUtil.isNotEmpty(recipeIds)) {
            return recipeIds;
        }
        recipeIds = babyRecipeMapper.selectList(new LambdaQueryWrapper<BabyRecipe>()
                        .select(BabyRecipe::getId)
                        .and(wrapper -> wrapper
                                .eq(BabyRecipe::getCategoryName, actualCategoryName)
                                .or()
                                .likeRight(BabyRecipe::getCategoryName, actualCategoryName + " /")))
                .stream()
                .map(BabyRecipe::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        return CollectionUtil.isEmpty(recipeIds) ? List.of(-1L) : recipeIds;
    }

    private List<RecipeRuleHit> findRuleHits(BabyRecipe recipe) {
        if (!RecipeAudienceTypeEnum.INFANT.equals(recipe.getAudienceType())) return Collections.emptyList();
        List<BabyRecipeRule> rules = babyRecipeRuleMapper.selectList(new LambdaQueryWrapper<BabyRecipeRule>()
                .eq(BabyRecipeRule::getEnabled, 1));
        if (CollectionUtil.isEmpty(rules)) {
            return Collections.emptyList();
        }
        String searchableText = String.join(" ",
                StrUtil.nullToEmpty(recipe.getTitle()),
                StrUtil.nullToEmpty(recipe.getSummary()),
                StrUtil.nullToEmpty(recipe.getIngredientsJson()),
                StrUtil.nullToEmpty(recipe.getStepsJson())
        );
        return rules.stream()
                .filter(rule -> rule.appliesTo(recipe.getMinMonthAge(), recipe.getMaxMonthAge()))
                .map(rule -> matchRule(recipe, rule, searchableText))
                .filter(hit -> hit != null)
                .toList();
    }

    private String buildRuleSetVersion() {
        String versions = babyRecipeRuleMapper.selectList(new LambdaQueryWrapper<BabyRecipeRule>()
                        .eq(BabyRecipeRule::getEnabled, 1).orderByAsc(BabyRecipeRule::getRuleCode))
                .stream().map(rule -> rule.getRuleCode() + ":" + rule.getRuleVersion()).collect(Collectors.joining("|"));
        return cn.hutool.crypto.digest.DigestUtil.sha256Hex(versions).substring(0, 16);
    }

    private void requireCurrentRuleSet(BabyRecipe recipe) {
        if (RecipeAudienceTypeEnum.INFANT.equals(recipe.getAudienceType())
                && !buildRuleSetVersion().equals(recipe.getScreeningVersion())) {
            throw new BusinessException("规则库已更新，请重新执行筛查后再审核或发布");
        }
    }

    private RecipeRuleHit matchRule(BabyRecipe recipe, BabyRecipeRule rule, String searchableText) {
        if ("AGE".equals(rule.getMatchType())) {
            return RecipeRuleHit.of(recipe.getSourceId(), recipe.getId(), rule, "月龄范围");
        }
        for (String keyword : StrUtil.split(rule.getKeywords(), ',')) {
            String actualKeyword = StrUtil.trim(keyword);
            if (StrUtil.isNotBlank(actualKeyword) && searchableText.contains(actualKeyword)) {
                return RecipeRuleHit.of(recipe.getSourceId(), recipe.getId(), rule, actualKeyword);
            }
        }
        return null;
    }

    private BabyRecipe getRecipe(Long id) {
        BabyRecipe recipe = babyRecipeMapper.selectById(id);
        if (recipe == null) {
            throw new BusinessException("食谱不存在");
        }
        return recipe;
    }

    private Map<String, RecipeSource> listExistingJumdataSources(List<JumdataRecipeItem> items) {
        List<String> sourceRecipeIds = items.stream()
                .map(JumdataRecipeItem::sourceRecipeId)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .toList();
        if (CollectionUtil.isEmpty(sourceRecipeIds)) {
            return Collections.emptyMap();
        }
        return recipeSourceMapper.selectList(new LambdaQueryWrapper<RecipeSource>()
                        .eq(RecipeSource::getProvider, RecipeSourceProviderEnum.JISU)
                        .in(RecipeSource::getProviderRecipeId, sourceRecipeIds))
                .stream()
                .collect(Collectors.toMap(RecipeSource::getProviderRecipeId, Function.identity(), (left, right) -> left));
    }

    private Map<Long, BabyRecipe> listRecipesBySourceId(List<Long> sourceIds) {
        if (CollectionUtil.isEmpty(sourceIds)) {
            return new LinkedHashMap<>();
        }
        return babyRecipeMapper.selectList(new LambdaQueryWrapper<BabyRecipe>()
                        .in(BabyRecipe::getSourceId, sourceIds))
                .stream()
                .filter(recipe -> recipe.getSourceId() != null)
                .collect(Collectors.toMap(BabyRecipe::getSourceId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
    }

    private void ensureRecipeCategory(BabyRecipe recipe, RecipeSourceProviderEnum provider, String categoryId, String categoryName) {
        if (recipe == null || recipe.getId() == null || StrUtil.isBlank(categoryName)) {
            return;
        }
        String actualCategoryId = StrUtil.blankToDefault(StrUtil.trim(categoryId), StrUtil.trim(categoryName));
        String actualCategoryName = StrUtil.trim(categoryName);
        long existing = babyRecipeCategoryMapper.selectCount(new LambdaQueryWrapper<BabyRecipeCategory>()
                .eq(BabyRecipeCategory::getRecipeId, recipe.getId())
                .eq(BabyRecipeCategory::getProvider, provider)
                .eq(BabyRecipeCategory::getCategoryId, actualCategoryId));
        if (existing == 0) {
            BabyRecipeCategory category = new BabyRecipeCategory();
            category.setRecipeId(recipe.getId());
            category.setProvider(provider);
            category.setCategoryId(actualCategoryId);
            category.setCategoryName(actualCategoryName);
            babyRecipeCategoryMapper.insert(category);
        }
        if (StrUtil.isBlank(recipe.getCategoryName())) {
            recipe.setCategoryId(actualCategoryId);
            recipe.setCategoryName(actualCategoryName);
            babyRecipeMapper.updateById(recipe);
        }
    }

    private RecipeSource buildJumdataSource(JumdataRecipeItem item, String categoryId, String categoryName) {
        RecipeSource source = new RecipeSource();
        source.setProvider(RecipeSourceProviderEnum.JISU);
        source.setProviderRecipeId(item.sourceRecipeId());
        source.setCategoryId(categoryId);
        source.setCategoryName(categoryName);
        source.setTitle(item.title());
        source.setCoverUrl(normalizeExternalImageUrl(item.coverUrl()));
        source.setRawPayload(item.rawPayload());
        source.setSyncStatus("IMPORTED");
        source.setSyncedAt(LocalDateTime.now());
        return source;
    }

    private BabyRecipe buildPendingRecipeFromJumdata(JumdataRecipeItem item, Long sourceId, String categoryId, String categoryName, RecipeAudienceTypeEnum audienceType) {
        BabyRecipeSaveRequest request = new BabyRecipeSaveRequest();
        request.setSourceId(sourceId);
        request.setCategoryId(categoryId);
        request.setCategoryName(categoryName);
        request.setTitle(item.title());
        request.setSummary(cleanDisplayText(item.summary()));
        request.setCoverUrl(normalizeExternalImageUrl(item.coverUrl()));
        RecipeAudienceTypeEnum resolvedAudienceType = audienceType == null
                ? (isInfantCategory(categoryName) ? RecipeAudienceTypeEnum.INFANT : RecipeAudienceTypeEnum.GENERAL)
                : audienceType;
        request.setAudienceType(resolvedAudienceType);
        if (RecipeAudienceTypeEnum.INFANT.equals(resolvedAudienceType)) {
            request.setMinMonthAge(6);
            request.setMaxMonthAge(36);
            request.setTextureType(BabyRecipeTextureTypeEnum.SOFT_CHUNK);
        }
        request.setDifficulty("待确认");
        request.setServingSize("1份");
        request.setIngredients(toIngredientRequests(item.ingredientsText()));
        request.setSteps(toStepRequests(item.stepsText()));
        BabyRecipe recipe = BabyRecipe.create(
                request,
                JSONUtil.toJsonStr(request.getIngredients()),
                JSONUtil.toJsonStr(request.getSteps())
        );
        return recipe;
    }

    private boolean isInfantCategory(String categoryName) {
        String name = StrUtil.nullToEmpty(categoryName);
        return List.of("婴幼儿", "婴儿", "幼儿", "宝宝", "辅食").stream().anyMatch(name::contains);
    }

    private List<RecipeIngredientRequest> toIngredientRequests(String ingredientsText) {
        if (StrUtil.isNotBlank(ingredientsText) && JSONUtil.isTypeJSONArray(ingredientsText)) {
            return JSONUtil.parseArray(ingredientsText).stream().map(value -> {
                RecipeIngredientRequest ingredient = new RecipeIngredientRequest();
                if (value instanceof cn.hutool.json.JSONObject object) {
                    ingredient.setName(firstJsonText(object, "mname", "ylName", "name", "ingredient", "material"));
                    ingredient.setAmount(firstJsonText(object, "amount", "ylUnit", "unit", "quantity"));
                } else ingredient.setName(String.valueOf(value));
                return ingredient;
            }).filter(item -> StrUtil.isNotBlank(item.getName())).toList();
        }
        List<String> names = splitRecipeText(ingredientsText);
        if (CollectionUtil.isEmpty(names)) {
            RecipeIngredientRequest placeholder = new RecipeIngredientRequest();
            placeholder.setName("待后台补充食材");
            return List.of(placeholder);
        }
        return names.stream().map(name -> {
            RecipeIngredientRequest ingredient = new RecipeIngredientRequest();
            ingredient.setName(name);
            return ingredient;
        }).toList();
    }

    private List<RecipeStepRequest> toStepRequests(String stepsText) {
        if (StrUtil.isNotBlank(stepsText) && JSONUtil.isTypeJSONArray(stepsText)) {
            return JSONUtil.parseArray(stepsText).stream().map(value -> {
                RecipeStepRequest step = new RecipeStepRequest();
                if (value instanceof cn.hutool.json.JSONObject object) {
                    step.setContent(cleanDisplayText(firstJsonText(object, "pcontent", "content", "step", "description", "text")));
                    step.setImageUrl(normalizeExternalImageUrl(firstJsonText(object, "imgUrl", "imageUrl", "img", "pic")));
                } else step.setContent(cleanDisplayText(String.valueOf(value)));
                return step;
            }).filter(item -> StrUtil.isNotBlank(item.getContent())).toList();
        }
        List<String> contents = splitRecipeText(stepsText);
        if (CollectionUtil.isEmpty(contents)) {
            RecipeStepRequest placeholder = new RecipeStepRequest();
            placeholder.setContent("待后台补充制作步骤");
            return List.of(placeholder);
        }
        return contents.stream().map(content -> {
            RecipeStepRequest step = new RecipeStepRequest();
            step.setContent(cleanDisplayText(content));
            return step;
        }).toList();
    }

    private String cleanDisplayText(String text) {
        String value = StrUtil.trimToNull(text);
        if (value == null) {
            return null;
        }
        value = value.replaceAll("(?i)<br\\s*/?>", " ");
        value = value.replaceAll("<[^>]+>", " ");
        value = HtmlUtils.htmlUnescape(value);
        return value.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
    }

    private String normalizeExternalImageUrl(String imageUrl) {
        String value = StrUtil.trimToNull(imageUrl);
        if (value == null) {
            return null;
        }
        if (value.startsWith("http://pic1.jisuapi.cn/") || value.startsWith("http://api.jisuapi.com/")) {
            return "https://" + value.substring("http://".length());
        }
        return value;
    }

    private String firstJsonText(cn.hutool.json.JSONObject object, String... names) {
        for (String name : names) {
            String value = StrUtil.trimToNull(object.getStr(name));
            if (value != null) return value;
        }
        return null;
    }

    private List<String> splitRecipeText(String text) {
        if (StrUtil.isBlank(text)) {
            return List.of();
        }
        if (JSONUtil.isTypeJSON(text)) {
            Object parsed = JSONUtil.parse(text);
            if (parsed instanceof JSONArray list) {
                return list.stream().map(Objects::toString).filter(StrUtil::isNotBlank).toList();
            }
        }
        return Arrays.stream(text.split("[\\n,，;；、]+"))
                .map(StrUtil::trim)
                .filter(StrUtil::isNotBlank)
                .limit(30)
                .toList();
    }

    private RecipeSyncResultVO buildSyncResult(int fetchedCount, int importedCount, int duplicatedCount, List<BabyRecipe> recipes) {
        RecipeSyncResultVO result = new RecipeSyncResultVO();
        result.setFetchedCount(fetchedCount);
        result.setImportedCount(importedCount);
        result.setDuplicatedCount(duplicatedCount);
        result.setImportedRecipes(recipes.stream().map(this::toVO).toList());
        result.setBlockedCount((int) recipes.stream().filter(recipe -> hasBlockHit(recipe.getId())).count());
        result.setWarnedCount((int) recipes.stream().filter(recipe -> !hasBlockHit(recipe.getId())
                && CollectionUtil.isNotEmpty(listRuleHits(recipe.getId()))).count());
        return result;
    }


    private BabyRecipe getPublishedRecipe(Long id) {
        BabyRecipe recipe = getRecipe(id);
        if (!BabyRecipeStatusEnum.PUBLISHED.equals(recipe.getStatus())) {
            throw new BusinessException("食谱不存在或未发布");
        }
        return recipe;
    }

    private void validateAgeRange(BabyRecipeSaveRequest request) {
        if (RecipeAudienceTypeEnum.GENERAL.equals(request.getAudienceType())) {
            request.setMinMonthAge(null);
            request.setMaxMonthAge(null);
            request.setTextureType(null);
            return;
        }
        if (request.getMinMonthAge() == null || request.getMaxMonthAge() == null || request.getTextureType() == null) {
            throw new BusinessException("婴幼儿食谱必须填写月龄范围和食物性状");
        }
        if (request.getMinMonthAge() > request.getMaxMonthAge()) {
            throw new BusinessException("最小月龄不能大于最大月龄");
        }
        if (request.getMinMonthAge() < 6 && CollectionUtil.isNotEmpty(request.getIngredients())) {
            // 0-5月龄规则仍由自动筛查处理，这里只提醒后台录入者不要绕过月龄边界。
            throw new BusinessException("0-5月龄不应录入辅食食材");
        }
    }

    private boolean hasBlockHit(Long recipeId) {
        return recipeRuleHitMapper.selectCount(new LambdaQueryWrapper<RecipeRuleHit>()
                .eq(RecipeRuleHit::getRecipeId, recipeId)
                .eq(RecipeRuleHit::getSeverity, BabyRecipeRuleSeverityEnum.BLOCK)) > 0;
    }

    private List<Long> resolveAppScopedRecipeIds(BabyRecipeQuery query) {
        boolean favoriteOnly = Boolean.TRUE.equals(query.getFavoriteOnly());
        boolean cookedOnly = Boolean.TRUE.equals(query.getCookedOnly());
        if (!favoriteOnly && !cookedOnly) {
            return null;
        }
        Long userId = requireCurrentUserId();
        Set<Long> scopedRecipeIds = null;
        if (favoriteOnly) {
            scopedRecipeIds = new HashSet<>(listFavoriteRecipeIds(userId));
        }
        if (cookedOnly) {
            Set<Long> cookedRecipeIds = new HashSet<>(listCookedRecipeIds(userId));
            if (scopedRecipeIds == null) {
                scopedRecipeIds = cookedRecipeIds;
            } else {
                scopedRecipeIds.retainAll(cookedRecipeIds);
            }
        }
        return scopedRecipeIds == null ? null : scopedRecipeIds.stream().toList();
    }

    private void markAppUserStatus(List<BabyRecipeVO> recipes) {
        if (CollectionUtil.isEmpty(recipes)) {
            return;
        }
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return;
        }
        List<Long> recipeIds = recipes.stream().map(BabyRecipeVO::getId).toList();
        Set<Long> favoriteRecipeIds = new HashSet<>(recipeFavoriteMapper.selectList(new LambdaQueryWrapper<RecipeFavorite>()
                        .eq(RecipeFavorite::getUserId, userId)
                        .in(RecipeFavorite::getRecipeId, recipeIds))
                .stream()
                .map(RecipeFavorite::getRecipeId)
                .toList());
        Set<Long> cookedRecipeIds = new HashSet<>(recipeCookedRecordMapper.selectList(new LambdaQueryWrapper<RecipeCookedRecord>()
                        .eq(RecipeCookedRecord::getUserId, userId)
                        .in(RecipeCookedRecord::getRecipeId, recipeIds))
                .stream()
                .map(RecipeCookedRecord::getRecipeId)
                .toList());
        recipes.forEach(recipe -> {
            recipe.setFavorite(favoriteRecipeIds.contains(recipe.getId()));
            recipe.setCooked(cookedRecipeIds.contains(recipe.getId()));
        });
    }

    private List<Long> listFavoriteRecipeIds(Long userId) {
        return recipeFavoriteMapper.selectList(new LambdaQueryWrapper<RecipeFavorite>()
                        .select(RecipeFavorite::getRecipeId)
                        .eq(RecipeFavorite::getUserId, userId))
                .stream()
                .map(RecipeFavorite::getRecipeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private List<Long> listCookedRecipeIds(Long userId) {
        return recipeCookedRecordMapper.selectList(new LambdaQueryWrapper<RecipeCookedRecord>()
                        .select(RecipeCookedRecord::getRecipeId)
                        .eq(RecipeCookedRecord::getUserId, userId))
                .stream()
                .map(RecipeCookedRecord::getRecipeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private boolean isFavorite(Long recipeId) {
        Long userId = SecurityUtils.getUserId();
        return userId != null && recipeFavoriteMapper.selectCount(new LambdaQueryWrapper<RecipeFavorite>()
                .eq(RecipeFavorite::getUserId, userId)
                .eq(RecipeFavorite::getRecipeId, recipeId)) > 0;
    }

    private boolean isCooked(Long recipeId) {
        Long userId = SecurityUtils.getUserId();
        return userId != null && recipeCookedRecordMapper.selectCount(new LambdaQueryWrapper<RecipeCookedRecord>()
                .eq(RecipeCookedRecord::getUserId, userId)
                .eq(RecipeCookedRecord::getRecipeId, recipeId)) > 0;
    }

    private Long requireCurrentUserId() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        return userId;
    }

    private BabyRecipeVO toVO(BabyRecipe recipe) {
        BabyRecipeVO vo = new BabyRecipeVO();
        vo.setId(recipe.getId());
        vo.setTitle(recipe.getTitle());
        vo.setSummary(cleanDisplayText(recipe.getSummary()));
        vo.setCoverUrl(recipe.getCoverUrl());
        vo.setAudienceType(recipe.getAudienceType());
        vo.setAudienceTypeLabel(recipe.getAudienceType() == null ? null : recipe.getAudienceType().getLabel());
        vo.setMinMonthAge(recipe.getMinMonthAge());
        vo.setMaxMonthAge(recipe.getMaxMonthAge());
        vo.setTextureType(recipe.getTextureType());
        vo.setTextureTypeLabel(recipe.getTextureType() == null ? null : recipe.getTextureType().getLabel());
        vo.setMealType(recipe.getMealType());
        vo.setMealTypeLabel(recipe.getMealType() == null ? null : recipe.getMealType().getLabel());
        vo.setDifficulty(recipe.getDifficulty());
        vo.setCookTimeMinutes(recipe.getCookTimeMinutes());
        vo.setServingSize(recipe.getServingSize());
        vo.setAllergenTags(recipe.getAllergenTags());
        vo.setRiskTags(recipe.getRiskTags());
        vo.setSourceId(recipe.getSourceId());
        vo.setCategoryId(recipe.getCategoryId());
        vo.setCategoryName(recipe.getCategoryName());
        populateCategoryInfo(vo, recipe.getId());
        populateSourceInfo(vo, recipe.getSourceId());
        vo.setStatus(recipe.getStatus());
        vo.setStatusLabel(recipe.getStatus() == null ? null : recipe.getStatus().getLabel());
        populateRuleRisk(vo, recipe.getId());
        vo.setFavorite(false);
        vo.setCooked(false);
        vo.setCreateTime(recipe.getCreateTime());
        vo.setPublishedAt(recipe.getPublishedAt());
        return vo;
    }

    private void populateSourceInfo(BabyRecipeVO vo, Long sourceId) {
        if (sourceId == null) {
            return;
        }
        RecipeSource source = recipeSourceMapper.selectById(sourceId);
        if (source == null) {
            return;
        }
        vo.setSourceProvider(source.getProvider() == null ? null : source.getProvider().getValue());
        vo.setSourceRecipeId(source.getProviderRecipeId());
        if (StrUtil.isBlank(vo.getCategoryId())) {
            vo.setCategoryId(source.getCategoryId());
        }
        if (StrUtil.isBlank(vo.getCategoryName())) {
            vo.setCategoryName(source.getCategoryName());
        }
    }

    private void populateCategoryInfo(BabyRecipeVO vo, Long recipeId) {
        if (recipeId == null) {
            return;
        }
        List<BabyRecipeCategory> categories = babyRecipeCategoryMapper.selectList(new LambdaQueryWrapper<BabyRecipeCategory>()
                .eq(BabyRecipeCategory::getRecipeId, recipeId)
                .orderByAsc(BabyRecipeCategory::getId));
        if (CollectionUtil.isEmpty(categories)) {
            return;
        }
        vo.setCategoryId(categories.stream()
                .map(BabyRecipeCategory::getCategoryId)
                .filter(StrUtil::isNotBlank)
                .findFirst()
                .orElse(vo.getCategoryId()));
        vo.setCategoryName(categories.stream()
                .map(BabyRecipeCategory::getCategoryName)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.joining("、")));
    }

    private BabyRecipeDetailVO toDetailVO(BabyRecipe recipe) {
        BabyRecipeDetailVO detail = new BabyRecipeDetailVO();
        BabyRecipeVO vo = toVO(recipe);
        detail.setId(vo.getId());
        detail.setTitle(vo.getTitle());
        detail.setSummary(vo.getSummary());
        detail.setCoverUrl(vo.getCoverUrl());
        detail.setAudienceType(vo.getAudienceType());
        detail.setAudienceTypeLabel(vo.getAudienceTypeLabel());
        detail.setMinMonthAge(vo.getMinMonthAge());
        detail.setMaxMonthAge(vo.getMaxMonthAge());
        detail.setTextureType(vo.getTextureType());
        detail.setTextureTypeLabel(vo.getTextureTypeLabel());
        detail.setMealType(vo.getMealType());
        detail.setMealTypeLabel(vo.getMealTypeLabel());
        detail.setDifficulty(vo.getDifficulty());
        detail.setCookTimeMinutes(vo.getCookTimeMinutes());
        detail.setServingSize(vo.getServingSize());
        detail.setAllergenTags(vo.getAllergenTags());
        detail.setRiskTags(vo.getRiskTags());
        detail.setSourceId(vo.getSourceId());
        detail.setCategoryId(vo.getCategoryId());
        detail.setCategoryName(vo.getCategoryName());
        detail.setSourceProvider(vo.getSourceProvider());
        detail.setSourceRecipeId(vo.getSourceRecipeId());
        detail.setStatus(vo.getStatus());
        detail.setStatusLabel(vo.getStatusLabel());
        detail.setRiskLevel(vo.getRiskLevel());
        detail.setRiskLevelLabel(vo.getRiskLevelLabel());
        detail.setRuleHitCount(vo.getRuleHitCount());
        detail.setFavorite(vo.getFavorite());
        detail.setCooked(vo.getCooked());
        detail.setCreateTime(vo.getCreateTime());
        detail.setPublishedAt(vo.getPublishedAt());
        detail.setIngredients(readList(recipe.getIngredientsJson(), RecipeIngredientRequest.class));
        detail.setSteps(cleanStepContent(readList(recipe.getStepsJson(), RecipeStepRequest.class)));
        detail.setRuleHits(listRuleHits(recipe.getId()));
        return detail;
    }

    private List<RecipeStepRequest> cleanStepContent(List<RecipeStepRequest> steps) {
        if (CollectionUtil.isEmpty(steps)) {
            return steps;
        }
        steps.forEach(step -> step.setContent(cleanDisplayText(step.getContent())));
        return steps;
    }

    private List<RecipeRuleHitVO> listRuleHits(Long recipeId) {
        return listRuleHitEntities(recipeId)
                .stream()
                .map(this::toHitVO)
                .toList();
    }

    private List<RecipeRuleHit> listRuleHitEntities(Long recipeId) {
        return recipeRuleHitMapper.selectList(new LambdaQueryWrapper<RecipeRuleHit>()
                .eq(RecipeRuleHit::getRecipeId, recipeId)
                .orderByDesc(RecipeRuleHit::getSeverity)
                .orderByDesc(RecipeRuleHit::getCreateTime));
    }

    private void populateRuleRisk(BabyRecipeVO vo, Long recipeId) {
        List<RecipeRuleHit> hits = listRuleHitEntities(recipeId);
        vo.setRuleHitCount(hits.size());
        BabyRecipeRuleSeverityEnum riskLevel = resolveHighestRisk(hits);
        vo.setRiskLevel(riskLevel);
        vo.setRiskLevelLabel(riskLevel == null ? null : riskLevel.getLabel());
    }

    private BabyRecipeRuleSeverityEnum resolveHighestRisk(List<RecipeRuleHit> hits) {
        if (CollectionUtil.isEmpty(hits)) {
            return null;
        }
        if (hits.stream().anyMatch(hit -> BabyRecipeRuleSeverityEnum.BLOCK.equals(hit.getSeverity()))) {
            return BabyRecipeRuleSeverityEnum.BLOCK;
        }
        if (hits.stream().anyMatch(hit -> BabyRecipeRuleSeverityEnum.REVIEW.equals(hit.getSeverity()))) {
            return BabyRecipeRuleSeverityEnum.REVIEW;
        }
        return BabyRecipeRuleSeverityEnum.WARN;
    }

    private RecipeRuleHitVO toHitVO(RecipeRuleHit hit) {
        RecipeRuleHitVO vo = new RecipeRuleHitVO();
        vo.setRuleCode(hit.getRuleCode());
        vo.setSeverity(hit.getSeverity());
        vo.setSeverityLabel(hit.getSeverity() == null ? null : hit.getSeverity().getLabel());
        vo.setMatchedText(hit.getMatchedText());
        vo.setSuggestion(hit.getSuggestion());
        vo.setRuleVersion(hit.getRuleVersion());
        vo.setEvidenceSource(hit.getEvidenceSource());
        vo.setEvidenceUrl(hit.getEvidenceUrl());
        return vo;
    }

    @Override
    public List<RecipeRuleVO> listRules() {
        return babyRecipeRuleMapper.selectList(new LambdaQueryWrapper<BabyRecipeRule>().orderByAsc(BabyRecipeRule::getRuleCode))
                .stream().map(this::toRuleVO).toList();
    }

    @Override
    public Long createRule(RecipeRuleSaveRequest request) {
        validateRuleRequest(request);
        if (babyRecipeRuleMapper.selectCount(new LambdaQueryWrapper<BabyRecipeRule>().eq(BabyRecipeRule::getRuleCode, request.getRuleCode())) > 0)
            throw new BusinessException("规则编码已存在");
        BabyRecipeRule rule = BabyRecipeRule.create(request.getRuleCode(), request.getRuleName(), request.getMinMonthAge(),
                request.getMaxMonthAge(), request.getMatchType(), request.getKeywords(), request.getSeverity(), request.getSuggestion(),
                request.getEvidenceSource(), request.getEvidenceUrl(), request.getEffectiveDate(), request.getEnabled());
        babyRecipeRuleMapper.insert(rule);
        return rule.getId();
    }

    @Override
    public boolean updateRule(Long id, RecipeRuleSaveRequest request) {
        validateRuleRequest(request);
        BabyRecipeRule rule = babyRecipeRuleMapper.selectById(id);
        if (rule == null) throw new BusinessException("规则不存在");
        if (!rule.getRuleCode().equals(request.getRuleCode())) throw new BusinessException("规则编码创建后不可修改");
        rule.revise(request.getRuleName(), request.getMinMonthAge(), request.getMaxMonthAge(), request.getMatchType(),
                request.getKeywords(), request.getSeverity(), request.getSuggestion(), request.getEvidenceSource(),
                request.getEvidenceUrl(), request.getEffectiveDate(), request.getEnabled());
        return babyRecipeRuleMapper.updateById(rule) > 0;
    }

    @Override
    public boolean deleteRule(Long id) {
        BabyRecipeRule rule = babyRecipeRuleMapper.selectById(id);
        if (rule == null) throw new BusinessException("规则不存在");
        rule.setEnabled(0);
        rule.setRuleVersion(rule.getRuleVersion() + 1);
        return babyRecipeRuleMapper.updateById(rule) > 0;
    }

    private void validateRuleRequest(RecipeRuleSaveRequest request) {
        if (request.getMinMonthAge() > request.getMaxMonthAge()) throw new BusinessException("规则最小月龄不能大于最大月龄");
        if (!Set.of("AGE", "INGREDIENT", "TEXT").contains(request.getMatchType())) throw new BusinessException("规则匹配类型仅支持 AGE/INGREDIENT/TEXT");
    }

    private RecipeRuleVO toRuleVO(BabyRecipeRule rule) {
        RecipeRuleVO vo = new RecipeRuleVO();
        vo.setId(rule.getId()); vo.setRuleCode(rule.getRuleCode()); vo.setRuleVersion(rule.getRuleVersion());
        vo.setRuleName(rule.getRuleName()); vo.setMinMonthAge(rule.getMinMonthAge()); vo.setMaxMonthAge(rule.getMaxMonthAge());
        vo.setMatchType(rule.getMatchType()); vo.setKeywords(rule.getKeywords()); vo.setSeverity(rule.getSeverity());
        vo.setSuggestion(rule.getSuggestion()); vo.setEvidenceSource(rule.getEvidenceSource()); vo.setEvidenceUrl(rule.getEvidenceUrl());
        vo.setEffectiveDate(rule.getEffectiveDate()); vo.setEnabled(rule.getEnabled()); vo.setCreateTime(rule.getCreateTime()); vo.setUpdateTime(rule.getUpdateTime());
        return vo;
    }

    private <T> List<T> readList(String json, Class<T> type) {
        if (StrUtil.isBlank(json)) {
            return Collections.emptyList();
        }
        return JSONUtil.toList(JSONUtil.parseArray(json), type);
    }
}
