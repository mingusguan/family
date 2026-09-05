package com.youlai.boot.recipe.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.youlai.boot.common.exception.BusinessException;
import com.youlai.boot.recipe.config.JumdataRecipeProperties;
import com.youlai.boot.recipe.service.JumdataRecipeClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class JumdataRecipeClientImpl implements JumdataRecipeClient {

    private static final long TOTAL_CACHE_TTL_MILLIS = 5 * 60 * 1000L;
    private static final int TOTAL_COUNT_LIMIT = 10000;

    private final JumdataRecipeProperties properties;
    private final Map<String, TotalCacheEntry> totalCache = new ConcurrentHashMap<>();

    @Override
    public List<JumdataRecipeCategory> listCategories() {
        requireConfigured();
        JSONObject response = postForm(properties.getCategoryUrl(), buildBaseForm());
        if (isNoData(response)) {
            return List.of();
        }
        validateSuccess(response, "极速数据加载菜谱分类失败");
        Object categoryPayload = response.get("result");
        if (categoryPayload == null) {
            categoryPayload = response.get("data");
        }
        if (categoryPayload == null) {
            categoryPayload = response;
        }
        return parseCategoryNodes(categoryPayload, null, "root");
    }

    @Override
    public List<JumdataRecipeItem> searchBabyRecipes(List<String> keywords, int limitPerKeyword) {
        requireConfigured();
        List<String> actualKeywords = CollectionUtil.isEmpty(keywords) ? properties.getDefaultKeywords() : keywords;

        List<JumdataRecipeItem> result = new ArrayList<>();
        Set<String> seenSourceIds = new LinkedHashSet<>();
        for (String keyword : actualKeywords) {
            if (StrUtil.isBlank(keyword)) {
                continue;
            }
            List<JumdataRecipeItem> items = requestKeyword(StrUtil.trim(keyword), limitPerKeyword);
            for (JumdataRecipeItem item : items) {
                if (seenSourceIds.add(item.sourceRecipeId())) {
                    result.add(item);
                }
            }
        }
        return result;
    }

    @Override
    public JumdataRecipePage searchRecipePage(String categoryValue, String categoryName, String keyword,
                                              int pageNum, int pageSize) {
        requireConfigured();
        String actualCategoryValue = StrUtil.blankToDefault(categoryValue, categoryName);
        int actualPage = Math.max(pageNum, 1);
        int actualPageSize = Math.max(1, Math.min(pageSize, maxPageSize()));
        if (StrUtil.isBlank(actualCategoryValue) && StrUtil.isBlank(keyword)) {
            return new JumdataRecipePage(List.of(), 0, actualPage, actualPageSize, 0);
        }

        Map<String, Object> form = buildBaseForm();
        String url;
        if (StrUtil.isNotBlank(keyword)) {
            url = properties.getSearchUrl();
            form.put(properties.getKeywordParamName(), StrUtil.trim(keyword));
        } else {
            url = properties.getByClassUrl();
            form.put(properties.getCategoryParamName(), actualCategoryValue);
        }
        form.put(properties.getPageSizeParamName(), actualPageSize);
        form.put(properties.getPageParamName(), (actualPage - 1) * actualPageSize);

        JSONObject response = postForm(url, form);
        if (isNoData(response)) {
            long total = countRemoteTotal(url, form);
            int totalPages = total == 0 ? 0 : (int) ((total + actualPageSize - 1) / actualPageSize);
            return new JumdataRecipePage(List.of(), total, actualPage, actualPageSize, totalPages);
        }
        validateSuccess(response, "极速数据查询菜谱失败");
        JSONObject pageObject = findRecipePageObject(response);
        List<JumdataRecipeItem> records = extractRecipeObjects(pageObject == null ? response : pageObject).stream()
                .map(this::toRecipeItem)
                .filter(item -> StrUtil.isNotBlank(item.title()))
                .toList();
        long explicitTotal = pageObject == null ? -1
                : firstLong(pageObject, -1, "allNum", "totalCount", "total", "totalNum");
        long total = explicitTotal >= records.size() ? explicitTotal : countRemoteTotal(url, form);
        int totalPages = pageObject == null ? 0
                : (int) firstLong(pageObject, 0, "allPage", "totalPage", "totalPages");
        if (totalPages <= 0 && total > 0) {
            totalPages = (int) ((total + actualPageSize - 1) / actualPageSize);
        }
        return new JumdataRecipePage(records, total, actualPage, actualPageSize, totalPages);
    }

    @Override
    public JumdataRecipeItem getRecipeById(String categoryValue, String categoryName, String sourceRecipeId) {
        requireConfigured();
        Map<String, Object> form = buildBaseForm();
        form.put(properties.getIdParamName(), sourceRecipeId);
        JSONObject response = postForm(properties.getDetailUrl(), form);
        if (isNoData(response)) {
            throw new BusinessException("勾选的极速数据菜谱不存在：" + sourceRecipeId);
        }
        validateSuccess(response, "极速数据加载勾选菜谱失败");
        return extractRecipeObjects(response).stream().map(this::toRecipeItem)
                .filter(item -> sourceRecipeId.equals(item.sourceRecipeId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("勾选的极速数据菜谱不存在：" + sourceRecipeId));
    }

    private List<JumdataRecipeItem> requestKeyword(String keyword, int limit) {
        Map<String, Object> form = buildBaseForm();
        form.put(properties.getKeywordParamName(), keyword);
        form.put(properties.getPageSizeParamName(), Math.max(1, Math.min(limit, maxPageSize())));
        form.put(properties.getPageParamName(), 0);

        JSONObject response = postForm(properties.getSearchUrl(), form);
        if (isNoData(response)) {
            return List.of();
        }
        validateSuccess(response, "极速数据同步失败");
        return extractRecipeObjects(response).stream()
                .map(this::toRecipeItem)
                .filter(item -> StrUtil.isNotBlank(item.title()))
                .limit(limit)
                .toList();
    }

    private void requireConfigured() {
        if (StrUtil.isBlank(properties.getAppKey())) {
            throw new BusinessException("未配置极速数据 appkey，无法同步菜谱");
        }
    }

    private Map<String, Object> buildBaseForm() {
        Map<String, Object> form = new LinkedHashMap<>();
        form.put("appkey", properties.getAppKey());
        return form;
    }

    private JSONObject postForm(String url, Map<String, Object> form) {
        RuntimeException lastError = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                HttpResponse httpResponse = HttpRequest.post(url).form(form).charset(StandardCharsets.UTF_8)
                        .timeout(30000).execute();
                String body = httpResponse.body();
                if (httpResponse.getStatus() < 200 || httpResponse.getStatus() >= 300) {
                    throw new BusinessException("极速数据请求失败：HTTP " + httpResponse.getStatus()
                            + "，" + summarizeHttpBody(body));
                }
                JSONObject response = JSONUtil.parseObj(body);
                int code = response.getInt("status", response.getInt("code", response.getInt("ret_code", -1)));
                if (!Set.of(502, 606, 609, 610).contains(code) || attempt == 3) {
                    return response;
                }
            } catch (RuntimeException exception) {
                lastError = exception;
                if (attempt == 3) {
                    throw exception;
                }
            }
            try {
                Thread.sleep(250L * attempt);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new BusinessException("极速数据请求已中断");
            }
        }
        throw lastError == null ? new BusinessException("极速数据请求失败") : lastError;
    }

    private int maxPageSize() {
        return properties.getMaxPageSize() == null ? 20 : Math.max(1, properties.getMaxPageSize());
    }

    private long countRemoteTotal(String url, Map<String, Object> originalForm) {
        String cacheKey = buildTotalCacheKey(url, originalForm);
        TotalCacheEntry cached = totalCache.get(cacheKey);
        long now = System.currentTimeMillis();
        if (cached != null && cached.expiresAt > now) {
            return cached.total;
        }

        int pageSize = maxPageSize();
        long total = 0;
        for (int start = 0; start < TOTAL_COUNT_LIMIT; start += pageSize) {
            Map<String, Object> form = new LinkedHashMap<>(originalForm);
            form.put(properties.getPageSizeParamName(), pageSize);
            form.put(properties.getPageParamName(), start);
            JSONObject response = postForm(url, form);
            if (isNoData(response)) {
                break;
            }
            validateSuccess(response, "极速数据统计菜谱总数失败");
            JSONObject pageObject = findRecipePageObject(response);
            int count = extractRecipeObjects(pageObject == null ? response : pageObject).size();
            if (count <= 0) {
                break;
            }
            total += count;
            if (count < pageSize) {
                break;
            }
        }
        totalCache.put(cacheKey, new TotalCacheEntry(total, now + TOTAL_CACHE_TTL_MILLIS));
        return total;
    }

    private String buildTotalCacheKey(String url, Map<String, Object> form) {
        return url + "|" + form.entrySet().stream()
                .filter(entry -> !"appkey".equals(entry.getKey()))
                .filter(entry -> !properties.getPageParamName().equals(entry.getKey()))
                .filter(entry -> !properties.getPageSizeParamName().equals(entry.getKey()))
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .sorted()
                .reduce((left, right) -> left + "&" + right)
                .orElse("");
    }

    private String summarizeHttpBody(String body) {
        if (StrUtil.isBlank(body)) {
            return "接口未返回内容";
        }
        String trimmed = StrUtil.trim(body);
        if (StrUtil.startWithIgnoreCase(trimmed, "<!doctype") || StrUtil.startWithIgnoreCase(trimmed, "<html")) {
            return "第三方返回了 HTML 错误页，请检查 recipe.jisu.search-url/by-class-url/category-url/detail-url 配置";
        }
        return "body=" + StrUtil.maxLength(trimmed, 500);
    }

    private boolean isNoData(JSONObject response) {
        int code = response.getInt("status", response.getInt("code", response.getInt("ret_code", -1)));
        return code == 201 || code == 205 || code == 250;
    }

    private void validateSuccess(JSONObject response, String action) {
        int code = response.getInt("status", response.getInt("code", response.getInt("ret_code", -1)));
        if (code != 0 && code != 200) {
            throw new BusinessException(buildFailureMessage(response, action));
        }
    }

    private String buildFailureMessage(JSONObject response, String action) {
        String message = firstNonBlank(response, "msg", "message", "ret_msg", "errmsg", "error", "reason");
        Object code = response.get("status");
        if (code == null) code = response.get("code");
        if (code == null) code = response.get("ret_code");
        if (code == null) code = response.get("error_code");
        String suffix = StrUtil.isBlank(message) ? "" : "，message=" + message;
        return action + "：code=" + (code == null ? "unknown" : code)
                + suffix
                + "，response=" + StrUtil.maxLength(response.toString(), 600);
    }

    private List<JumdataRecipeCategory> parseCategoryNodes(Object value, String parentId, String path) {
        if (value instanceof JSONArray array) {
            List<JumdataRecipeCategory> categories = new ArrayList<>();
            for (Object item : array) {
                if (item instanceof JSONObject object) {
                    categories.addAll(parseCategoryObject(object, parentId, path));
                } else if (item != null && StrUtil.isNotBlank(String.valueOf(item))) {
                    String name = StrUtil.trim(String.valueOf(item));
                    categories.add(new JumdataRecipeCategory(path + "/" + name, name, parentId, name, List.of()));
                }
            }
            return categories;
        }
        if (value instanceof JSONObject object) {
            return parseCategoryObject(object, parentId, path);
        }
        return List.of();
    }

    private List<JumdataRecipeCategory> parseCategoryObject(JSONObject object, String parentId, String path) {
        String explicitName = firstNonBlank(object, "name", "title", "typeName", "categoryName");
        if (explicitName != null) {
            String queryValue = StrUtil.blankToDefault(
                    firstNonBlank(object, "classid", "id", "typeId", "categoryId", "code", "value"),
                    explicitName
            );
            String nodeId = path + "/" + queryValue;
            Object childPayload = firstPresent(object, "children", "childs", "list", "items", "types", "sons");
            List<JumdataRecipeCategory> children = parseCategoryNodes(childPayload, nodeId, nodeId);
            return List.of(new JumdataRecipeCategory(nodeId, explicitName, parentId, queryValue, children));
        }

        List<JumdataRecipeCategory> categories = new ArrayList<>();
        for (Map.Entry<String, Object> entry : object.entrySet()) {
            if (isResponseMetadata(entry.getKey()) || entry.getValue() == null) {
                continue;
            }
            String name = StrUtil.trim(entry.getKey());
            String nodeId = path + "/" + name;
            List<JumdataRecipeCategory> children = parseCategoryNodes(entry.getValue(), nodeId, nodeId);
            categories.add(new JumdataRecipeCategory(nodeId, name, parentId, name, children));
        }
        return categories;
    }

    private Object firstPresent(JSONObject object, String... names) {
        for (String name : names) {
            if (object.containsKey(name)) {
                return object.get(name);
            }
        }
        return null;
    }

    private boolean isResponseMetadata(String name) {
        return Set.of("status", "code", "ret_code", "msg", "message", "flag", "taskNo", "charge",
                "page", "pageSize", "total", "totalCount", "totalPage", "count", "num").contains(name);
    }

    private JSONObject findRecipePageObject(Object value) {
        if (value instanceof JSONObject object) {
            if (object.get("list") instanceof JSONArray
                    || object.get("datas") instanceof JSONArray
                    || object.get("items") instanceof JSONArray
                    || object.get("records") instanceof JSONArray) {
                return object;
            }
            for (Object nested : object.values()) {
                JSONObject found = findRecipePageObject(nested);
                if (found != null) return found;
            }
        } else if (value instanceof JSONArray array) {
            for (Object nested : array) {
                JSONObject found = findRecipePageObject(nested);
                if (found != null) return found;
            }
        }
        return null;
    }

    private long firstLong(JSONObject object, long defaultValue, String... names) {
        for (String name : names) {
            Object value = object.get(name);
            if (value == null) continue;
            try {
                return Long.parseLong(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                log.debug("Ignored invalid recipe number field {}={}", name, value);
            }
        }
        return defaultValue;
    }

    private List<JSONObject> extractRecipeObjects(JSONObject response) {
        List<JSONObject> recipes = new ArrayList<>();
        collectRecipeObjects(response.get("result"), recipes);
        collectRecipeObjects(response.get("data"), recipes);
        if (recipes.isEmpty()) {
            collectRecipeObjects(response, recipes);
        }
        return recipes;
    }

    private void collectRecipeObjects(Object value, List<JSONObject> recipes) {
        if (value == null) {
            return;
        }
        if (value instanceof JSONArray array) {
            for (Object item : array) {
                collectRecipeObjects(item, recipes);
            }
            return;
        }
        if (!(value instanceof JSONObject object)) {
            return;
        }
        if (looksLikeRecipe(object)) {
            recipes.add(object);
            return;
        }
        for (Object nested : object.values()) {
            collectRecipeObjects(nested, recipes);
        }
    }

    private boolean looksLikeRecipe(JSONObject object) {
        return firstNonBlank(object, "id", "recipeId", "menuId", "cpId") != null
                && firstNonBlank(object, "name", "title", "menuName", "cpName") != null;
    }

    private JumdataRecipeItem toRecipeItem(JSONObject object) {
        String sourceId = firstNonBlank(object, "id", "recipeId", "menuId", "cpId");
        String title = firstNonBlank(object, "name", "title", "menuName", "cpName");
        String summary = firstNonBlank(object, "desc", "des", "tip", "content", "summary", "description", "intro", "tag");
        String coverUrl = firstNonBlank(object, "pic", "smallImag", "smallImg", "largeImg", "img", "image", "cover", "coverUrl");
        String ingredients = stringifyField(object, "material", "yl", "materials", "ingredients", "ingredient");
        String steps = stringifyField(object, "process", "steps", "practice", "method", "procedures");
        String rawPayload = object.toString();
        if (StrUtil.isBlank(sourceId)) {
            sourceId = DigestUtil.sha256Hex(rawPayload);
        }
        return new JumdataRecipeItem(sourceId, title, summary, coverUrl, ingredients, steps, rawPayload);
    }

    private String stringifyField(JSONObject object, String... names) {
        for (String name : names) {
            Object value = object.get(name);
            if (value == null) {
                continue;
            }
            if (value instanceof JSONArray || value instanceof JSONObject) {
                return JSONUtil.toJsonStr(value);
            }
            String text = StrUtil.trimToNull(String.valueOf(value));
            if (text != null) {
                return text;
            }
        }
        return null;
    }

    private String firstNonBlank(JSONObject object, String... names) {
        for (String name : names) {
            String value = StrUtil.trimToNull(object.getStr(name));
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private record TotalCacheEntry(long total, long expiresAt) {
    }
}
