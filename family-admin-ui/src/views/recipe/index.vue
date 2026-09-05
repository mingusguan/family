<template>
  <div class="app-container">
    <el-tabs v-model="mainTab">
      <el-tab-pane label="菜谱管理" name="recipes">
        <el-form :model="query" inline>
          <el-form-item label="关键词"><el-input v-model="query.keyword" clearable placeholder="标题、摘要或食材" /></el-form-item>
          <el-form-item label="适用人群"><el-select v-model="query.audienceType" clearable style="width: 130px"><el-option label="普通家庭" value="GENERAL" /><el-option label="婴幼儿" value="INFANT" /></el-select></el-form-item>
          <el-form-item label="分类"><el-select v-model="query.categoryName" clearable filterable placeholder="第三方分类" style="width: 220px"><el-option-group v-for="group in categoryGroups" :key="group.label" :label="group.label"><el-option v-for="item in group.options" :key="item.value" :label="item.label" :value="item.value" /></el-option-group></el-select></el-form-item>
          <el-form-item label="状态"><el-select v-model="query.status" clearable style="width: 130px"><el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
          <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="resetSearch">重置</el-button></el-form-item>
        </el-form>
        <div class="toolbar"><el-button v-hasPerm="['recipe:create']" type="success" @click="openForm()">新增菜谱</el-button><el-button v-hasPerm="['recipe:sync']" type="primary" @click="openSync">极速同步</el-button></div>
        <el-table v-loading="loading" :data="table.list" border>
          <el-table-column label="封面" width="76"><template #default="{ row }"><el-image v-if="row.coverUrl" :src="row.coverUrl" class="cover" fit="cover" /><span v-else>无图</span></template></el-table-column>
          <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
          <el-table-column prop="categoryName" label="分类" min-width="140" show-overflow-tooltip />
          <el-table-column label="人群" width="100"><template #default="{ row }">{{ row.audienceTypeLabel }}</template></el-table-column>
          <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="statusType(row.status)">{{ row.statusLabel }}</el-tag></template></el-table-column>
          <el-table-column label="规则标记" width="120"><template #default="{ row }"><el-tag v-if="row.riskLevel" :type="riskType(row.riskLevel)">{{ row.riskLevel }}</el-tag><span v-else>-</span></template></el-table-column>
          <el-table-column prop="sourceProvider" label="来源" width="100" />
          <el-table-column prop="createTime" label="创建时间" width="170" />
          <el-table-column fixed="right" label="操作" width="350">
            <template #default="{ row }">
              <el-button link type="primary" @click="openDetail(row.id)">详情</el-button>
              <el-button v-hasPerm="['recipe:update']" link type="primary" @click="openForm(row.id)">编辑</el-button>
              <el-button v-if="['DRAFT','AUTO_REJECTED','REJECTED','OFFLINE'].includes(row.status)" v-hasPerm="['recipe:review']" link type="warning" @click="screen(row.id)">筛查</el-button>
              <el-button v-if="row.status === 'PENDING_REVIEW'" v-hasPerm="['recipe:review']" link type="success" @click="approve(row.id)">通过</el-button>
              <el-button v-if="row.status === 'PENDING_REVIEW'" v-hasPerm="['recipe:review']" link type="danger" @click="reject(row.id)">驳回</el-button>
              <el-button v-if="row.status === 'APPROVED'" v-hasPerm="['recipe:publish']" link type="success" @click="publishRecipe(row.id)">发布</el-button>
              <el-button v-if="row.status === 'PUBLISHED'" v-hasPerm="['recipe:publish']" link type="danger" @click="offline(row.id)">下线</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination v-if="table.total" v-model:total="table.total" v-model:page="query.pageNum" v-model:limit="query.pageSize" @pagination="fetchRecipes" />
      </el-tab-pane>

      <el-tab-pane label="婴幼儿规则库" name="rules">
        <div class="toolbar"><el-button v-hasPerm="['recipe:rule']" type="primary" @click="openRule()">新增规则</el-button><el-button @click="loadRules">刷新</el-button></div>
        <el-table :data="ruleRows" border>
          <el-table-column prop="ruleCode" label="规则编码" min-width="180" />
          <el-table-column prop="ruleName" label="名称" min-width="180" />
          <el-table-column label="版本" width="70"><template #default="{ row }">v{{ row.ruleVersion }}</template></el-table-column>
          <el-table-column label="月龄" width="90"><template #default="{ row }">{{ row.minMonthAge }}-{{ row.maxMonthAge }}</template></el-table-column>
          <el-table-column prop="severity" label="级别" width="90" />
          <el-table-column prop="keywords" label="关键词" min-width="220" show-overflow-tooltip />
          <el-table-column prop="evidenceSource" label="依据" min-width="200" show-overflow-tooltip />
          <el-table-column label="启用" width="70"><template #default="{ row }"><el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '是' : '否' }}</el-tag></template></el-table-column>
          <el-table-column label="操作" width="140"><template #default="{ row }"><el-button link type="primary" @click="openRule(row)">编辑</el-button><el-button link type="danger" @click="disableRule(row)">停用</el-button></template></el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="formVisible" :title="editingId ? '编辑菜谱' : '新增菜谱'" width="850px">
      <el-form ref="formRef" :model="form" label-width="100px">
        <el-row :gutter="16"><el-col :span="12"><el-form-item label="标题" required><el-input v-model="form.title" /></el-form-item></el-col><el-col :span="12"><el-form-item label="适用人群" required><el-select v-model="form.audienceType"><el-option label="普通家庭" value="GENERAL" /><el-option label="婴幼儿" value="INFANT" /></el-select></el-form-item></el-col></el-row>
        <el-form-item label="分类"><el-select v-model="form.categoryName" clearable filterable allow-create default-first-option placeholder="选择或输入分类" style="width: 100%"><el-option-group v-for="group in categoryGroups" :key="group.label" :label="group.label"><el-option v-for="item in group.options" :key="item.value" :label="item.label" :value="item.value" /></el-option-group></el-select></el-form-item>
        <el-form-item label="摘要"><el-input v-model="form.summary" type="textarea" /></el-form-item>
        <el-form-item label="封面URL"><el-input v-model="form.coverUrl" /></el-form-item>
        <el-row v-if="form.audienceType === 'INFANT'" :gutter="16"><el-col :span="8"><el-form-item label="最小月龄" required><el-input-number v-model="form.minMonthAge" :min="0" :max="36" /></el-form-item></el-col><el-col :span="8"><el-form-item label="最大月龄" required><el-input-number v-model="form.maxMonthAge" :min="0" :max="36" /></el-form-item></el-col><el-col :span="8"><el-form-item label="食物性状" required><el-select v-model="form.textureType"><el-option v-for="item in textureOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col></el-row>
        <el-divider>食材</el-divider>
        <div v-for="(item,index) in form.ingredients" :key="index" class="ingredient-row"><el-input v-model="item.name" placeholder="食材名称" /><el-input v-model="item.amount" placeholder="用量，如 50克" /><el-input v-model="item.note" placeholder="备注" /><el-button link type="danger" @click="removeIngredient(index)">删除</el-button></div>
        <el-button @click="form.ingredients.push({name:''})">添加食材</el-button>
        <el-divider>制作步骤</el-divider>
        <div v-for="(item,index) in form.steps" :key="index" class="step-row">
          <el-input v-model="item.content" type="textarea" :placeholder="`步骤 ${index + 1}`" />
          <div class="step-image-field">
            <el-image v-if="item.imageUrl" :src="item.imageUrl" class="step-image-preview" fit="cover" :preview-src-list="[item.imageUrl]" preview-teleported />
            <div v-else class="step-image-empty">无图</div>
            <el-input v-model="item.imageUrl" clearable placeholder="步骤图片URL（可选）" />
          </div>
          <el-button link type="danger" @click="removeStep(index)">删除</el-button>
        </div>
        <el-button @click="form.steps.push({content:''})">添加步骤</el-button>
      </el-form>
      <template #footer><el-button @click="formVisible=false">取消</el-button><el-button type="primary" :loading="saving" @click="saveRecipe">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="菜谱详情与规则筛查" width="920px">
      <template v-if="detail">
        <el-descriptions :column="3" border><el-descriptions-item label="标题">{{ detail.title }}</el-descriptions-item><el-descriptions-item label="分类">{{ detail.categoryName || "-" }}</el-descriptions-item><el-descriptions-item label="人群">{{ detail.audienceTypeLabel }}</el-descriptions-item><el-descriptions-item label="状态">{{ detail.statusLabel }}</el-descriptions-item><el-descriptions-item label="规则标记"><el-tag v-if="detail.riskLevel" :type="riskType(detail.riskLevel)">{{ detail.riskLevel }}</el-tag><span v-else>-</span></el-descriptions-item></el-descriptions>
        <h4>规则筛查</h4><el-table :data="detail.ruleHits" border empty-text="无规则风险"><el-table-column label="级别" width="90"><template #default="{ row }"><el-tag :type="riskType(row.severity)">{{ row.severity }}</el-tag></template></el-table-column><el-table-column prop="ruleCode" label="规则" width="190" /><el-table-column prop="matchedText" label="命中" width="100" /><el-table-column prop="suggestion" label="建议" /><el-table-column label="依据" min-width="180"><template #default="{ row }"><el-link v-if="row.evidenceUrl" :href="row.evidenceUrl" target="_blank">{{ row.evidenceSource }}</el-link><span v-else>{{ row.evidenceSource }}</span></template></el-table-column></el-table>
      </template>
    </el-dialog>

    <el-dialog v-model="syncVisible" title="极速数据菜谱同步" width="1050px" @opened="loadSyncData">
      <el-alert title="仅导入本地不存在的 sourceRecipeId；同步后按规则自动筛查，普通菜谱直接通过，命中拦截规则的进入待审核。" type="info" :closable="false" />
      <el-select v-model="activeTaskNo" clearable placeholder="最近同步任务" class="task-select" @change="resumeTask"><el-option v-for="task in recentTasks" :key="task.taskNo" :label="`${task.status} ${task.processedCount}/${task.requestedCount}`" :value="task.taskNo" /></el-select>
      <div v-if="syncTask" class="task-progress"><el-progress :percentage="progress" /><span>状态 {{ syncTask.status }}，已处理 {{ syncTask.processedCount }}/{{ syncTask.requestedCount }}，导入 {{ syncTask.importedCount }}，重复 {{ syncTask.duplicatedCount }}，失败 {{ syncTask.failedCount }}</span><el-alert v-if="syncTask.errorMessage" :title="syncTask.errorMessage" type="error" show-icon :closable="false" /></div>
      <div class="sync-options"><span>同步到</span><el-select v-model="syncAudience" placeholder="智能判断" style="width: 180px"><el-option label="智能判断" value="" /><el-option label="普通家庭" value="GENERAL" /><el-option label="婴幼儿" value="INFANT" /></el-select><el-text type="info">选择“普通家庭”后，新导入菜谱不会写入月龄和辅食性状。</el-text></div>
      <el-tabs v-model="syncTab">
        <el-tab-pane label="按分类和数量" name="count">
          <el-select v-model="batchIds" multiple filterable placeholder="选择分类" style="width: 600px" @change="changeBatch"><el-option v-for="item in flatCategories" :key="item.id" :label="item.pathName" :value="item.id" /></el-select>
          <el-table :data="batchRows" border class="mt">
            <el-table-column prop="pathName" label="分类" min-width="280" />
            <el-table-column label="第三方总数" width="170">
              <template #default="{ row }"><el-tag v-if="row.countLoading" type="info">查询中</el-tag><span v-else>{{ row.remoteTotal ?? "-" }} 条</span></template>
            </el-table-column>
            <el-table-column label="可同步估算" width="170">
              <template #default="{ row }"><span>{{ row.availableTotalEstimate ?? "-" }} 条</span></template>
            </el-table-column>
            <el-table-column label="同步数量（最多10000）" width="300">
              <template #default="{ row }"><div class="limit-cell"><el-input-number v-model="row.limit" :min="1" :max="batchLimitMax(row)" /><el-button link type="primary" :disabled="!row.remoteTotal" @click="fillBatchLimit(row)">填入总数</el-button></div></template>
            </el-table-column>
          </el-table>
          <el-button class="mt" type="primary" @click="startCountSync">开始同步</el-button>
        </el-tab-pane>
        <el-tab-pane label="查询并勾选" name="select">
          <div class="preview-bar"><el-select v-model="preview.categoryId" filterable placeholder="选择分类" @change="changePreviewCategory"><el-option v-for="item in flatCategories" :key="item.id" :label="item.pathName" :value="item.id" /></el-select><el-input v-model="preview.keyword" clearable placeholder="搜索菜谱" @keyup.enter="searchPreview" /><el-button type="primary" @click="searchPreview">查询分类菜谱</el-button><span>已选 {{ selectedRecipes.size }} 条<span v-if="previewTotal">，共 {{ previewTotal }} 条</span></span></div>
          <el-alert v-if="excluded" :title="`本页已排除本地已有 ${excluded} 条`" type="success" :closable="false" />
          <el-table ref="previewTable" v-loading="previewLoading" :data="previewRows" row-key="sourceRecipeId" border height="360" @selection-change="selectPreview"><el-table-column type="selection" reserve-selection width="46" /><el-table-column prop="title" label="标题" min-width="180" /><el-table-column prop="summary" label="摘要" min-width="300" show-overflow-tooltip /><el-table-column prop="sourceRecipeId" label="远程ID" width="180" /></el-table>
          <div class="preview-footer"><div class="preview-page-info">第 {{ preview.pageNum }} / {{ previewPages || 1 }} 页</div><el-pagination v-model:current-page="preview.pageNum" v-model:page-size="preview.pageSize" background :total="previewTotal" :page-sizes="[10,20]" layout="total, sizes, prev, pager, next, jumper" @current-change="loadPreview" @size-change="searchPreview" /><el-button type="primary" :disabled="!selectedRecipes.size" @click="startSelectedSync">同步勾选菜谱</el-button></div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <el-dialog v-model="ruleVisible" :title="ruleEditingId ? '编辑规则' : '新增规则'" width="700px"><el-form :model="ruleForm" label-width="100px"><el-form-item label="规则编码"><el-input v-model="ruleForm.ruleCode" :disabled="!!ruleEditingId" /></el-form-item><el-form-item label="规则名称"><el-input v-model="ruleForm.ruleName" /></el-form-item><el-row><el-col :span="12"><el-form-item label="最小月龄"><el-input-number v-model="ruleForm.minMonthAge" :min="0" :max="36" /></el-form-item></el-col><el-col :span="12"><el-form-item label="最大月龄"><el-input-number v-model="ruleForm.maxMonthAge" :min="0" :max="36" /></el-form-item></el-col></el-row><el-form-item label="匹配类型"><el-select v-model="ruleForm.matchType"><el-option label="年龄" value="AGE" /><el-option label="食材" value="INGREDIENT" /><el-option label="全文" value="TEXT" /></el-select></el-form-item><el-form-item label="风险级别"><el-select v-model="ruleForm.severity"><el-option label="拦截" value="BLOCK" /><el-option label="人工审核" value="REVIEW" /><el-option label="提示" value="WARN" /></el-select></el-form-item><el-form-item label="关键词"><el-input v-model="ruleForm.keywords" type="textarea" placeholder="多个关键词用英文逗号分隔；AGE规则填写 *" /></el-form-item><el-form-item label="审核建议"><el-input v-model="ruleForm.suggestion" type="textarea" /></el-form-item><el-form-item label="依据名称"><el-input v-model="ruleForm.evidenceSource" /></el-form-item><el-form-item label="依据链接"><el-input v-model="ruleForm.evidenceUrl" /></el-form-item><el-form-item label="启用"><el-switch v-model="ruleEnabled" /></el-form-item></el-form><template #footer><el-button @click="ruleVisible=false">取消</el-button><el-button type="primary" @click="saveRule">保存</el-button></template></el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from "element-plus";
import RecipeAPI from "@/api/recipe";
import type { BabyRecipeDetail, BabyRecipeForm, BabyRecipeItem, BabyRecipeQuery, BabyRecipeStatus, JumdataCategorySyncItem, JumdataRecipeCategory, JumdataRecipePreviewItem, RecipeAudienceType, RecipeRuleForm, RecipeRuleItem, RecipeRuleSeverity, RecipeSyncTask } from "@/api/recipe";

defineOptions({ name: "BabyRecipe" });
const mainTab=ref("recipes"), loading=ref(false), saving=ref(false), formVisible=ref(false), detailVisible=ref(false), syncVisible=ref(false), ruleVisible=ref(false);
const query=reactive<BabyRecipeQuery>({pageNum:1,pageSize:10}), table=reactive({list:[] as BabyRecipeItem[],total:0}), recipeCategories=ref<string[]>([]), editingId=ref<string>(), detail=ref<BabyRecipeDetail>();
const statusOptions=[{label:"草稿",value:"DRAFT"},{label:"待审核",value:"PENDING_REVIEW"},{label:"审核通过",value:"APPROVED"},{label:"审核驳回",value:"REJECTED"},{label:"已发布",value:"PUBLISHED"},{label:"已下线",value:"OFFLINE"}] as const;
const textureOptions=[{label:"泥糊",value:"PUREE"},{label:"碎末",value:"MINCED"},{label:"软烂小块",value:"SOFT_CHUNK"},{label:"家庭软食",value:"FAMILY_SOFT"}] as const;
const emptyForm=():BabyRecipeForm=>({title:"",categoryName:"",audienceType:"INFANT",minMonthAge:6,maxMonthAge:36,textureType:"SOFT_CHUNK",ingredients:[{name:""}],steps:[{content:""}]});
const form=reactive<BabyRecipeForm>(emptyForm()); const formRef=ref();
type RecipeCategoryGroup={label:string;options:Array<{label:string;value:string;sort:number}>};
const categoryGroups=computed<RecipeCategoryGroup[]>(()=>buildCategoryGroups(recipeCategories.value));
function buildCategoryGroups(categories:string[]){const groups=new Map<string,RecipeCategoryGroup>();categories.map(name=>name.trim()).filter(Boolean).forEach(value=>{const parts=value.split("/").map(part=>part.trim()).filter(Boolean);const groupLabel=parts.length>1?parts[0]:"未分组";const optionLabel=formatCategoryOptionLabel(groupLabel,parts.length>1?parts.slice(1).join(" / "):value);if(!groups.has(groupLabel))groups.set(groupLabel,{label:groupLabel,options:[]});groups.get(groupLabel)!.options.push({label:optionLabel,value,sort:categorySortValue(groupLabel,optionLabel)})});return[...groups.values()].map(group=>({...group,options:group.options.sort((a,b)=>a.sort-b.sort||a.label.localeCompare(b.label,"zh-Hans-CN"))})).sort((a,b)=>a.label.localeCompare(b.label,"zh-Hans-CN"))}
function formatCategoryOptionLabel(groupLabel:string,label:string){return groupLabel==="人群"&&label==="宝宝"?"宝宝（泛分类）":label}
function categorySortValue(groupLabel:string,label:string){if(groupLabel!=="人群")return 100;const order=["婴儿","幼儿","一岁宝宝","两岁宝宝","三岁宝宝","宝宝（泛分类）"];const index=order.indexOf(label);return index>=0?index:90}
function mergeRecipeCategories(rows:BabyRecipeItem[]){const names=rows.flatMap(row=>(row.categoryName||"").split("、")).map(name=>name.trim()).filter(Boolean);recipeCategories.value=[...new Set([...recipeCategories.value,...names])].sort()}
async function fetchRecipes(){loading.value=true;try{const data=await RecipeAPI.getPage(query);table.list=data.list||[];table.total=data.total||0;mergeRecipeCategories(table.list)}finally{loading.value=false}}
async function loadRecipeCategories(){const categories=await RecipeAPI.getCategories();recipeCategories.value=[...new Set(categories.filter(Boolean))].sort()}
function search(){query.pageNum=1;fetchRecipes()} function resetSearch(){Object.assign(query,{pageNum:1,pageSize:10,keyword:undefined,status:undefined,audienceType:undefined,categoryName:undefined});fetchRecipes()}
async function openForm(id?:string){editingId.value=id;Object.assign(form,emptyForm());if(id)Object.assign(form,await RecipeAPI.getDetail(id));formVisible.value=true}
async function saveRecipe(){if(!form.title.trim()||!form.ingredients.some(i=>i.name.trim())||!form.steps.some(i=>i.content.trim()))return ElMessage.warning("请完整填写标题、食材和步骤");saving.value=true;try{const payload={...form,ingredients:form.ingredients.filter(i=>i.name.trim()),steps:form.steps.filter(i=>i.content.trim())};editingId.value?await RecipeAPI.update(editingId.value,payload):await RecipeAPI.create(payload);ElMessage.success("保存成功，需重新筛查和审核后才能发布");formVisible.value=false;await fetchRecipes();await loadRecipeCategories()}finally{saving.value=false}}
function removeIngredient(i:number){form.ingredients.splice(i,1);if(!form.ingredients.length)form.ingredients.push({name:""})} function removeStep(i:number){form.steps.splice(i,1);if(!form.steps.length)form.steps.push({content:""})}
async function openDetail(id:string){detail.value=await RecipeAPI.getDetail(id);detailVisible.value=true}
async function screen(id:string){const r=await RecipeAPI.screen(id);ElMessage.success(`筛查完成，命中 ${r.hits.length} 条规则`);fetchRecipes()}
async function approve(id:string){await RecipeAPI.approve(id);ElMessage.success("审核通过");fetchRecipes()} async function reject(id:string){await ElMessageBox.confirm("确认驳回该菜谱？","审核确认");await RecipeAPI.reject(id);fetchRecipes()} async function publishRecipe(id:string){await RecipeAPI.publish(id);ElMessage.success("发布成功");fetchRecipes()} async function offline(id:string){await ElMessageBox.confirm("确认下线该菜谱？","下线确认");await RecipeAPI.offline(id);fetchRecipes()}
function statusType(s:BabyRecipeStatus){return s==="PUBLISHED"?"success":s==="AUTO_REJECTED"||s==="REJECTED"?"danger":s==="PENDING_REVIEW"?"warning":s==="APPROVED"?"primary":"info"}
function riskType(s?:RecipeRuleSeverity){return s==="BLOCK"?"danger":s==="WARN"||s==="REVIEW"?"warning":"info"}

type FlatJumdataCategory = JumdataRecipeCategory & { pathName: string; queryCategoryValue: string; queryKeyword?: string };
type BatchJumdataRow = JumdataCategorySyncItem & { nodeId:string; pathName:string; remoteTotal?:number; availableTotalEstimate?:number; countLoading?:boolean };
const categoryTree=ref<JumdataRecipeCategory[]>([]), syncTab=ref("count"), syncAudience=ref<RecipeAudienceType|"">(""), batchIds=ref<string[]>([]), batchRows=ref<BatchJumdataRow[]>([]);
function flattenCategories(nodes:JumdataRecipeCategory[],parentPath=""):FlatJumdataCategory[]{return nodes.flatMap(node=>{const pathName=parentPath?`${parentPath} / ${node.name}`:node.name;const item={...node,pathName,queryCategoryValue:node.queryValue||node.id||node.name};return[item,...flattenCategories(node.children||[],pathName)]})}
const flatCategories=computed(()=>flattenCategories(categoryTree.value));
const preview=reactive({categoryId:"",keyword:"",pageNum:1,pageSize:20}),previewRows=ref<JumdataRecipePreviewItem[]>([]),previewTotal=ref(0),previewPages=ref(0),excluded=ref(0),previewLoading=ref(false),previewTable=ref(),selectedRecipes=reactive(new Map<string,JumdataRecipePreviewItem>());
const syncTask=ref<RecipeSyncTask>(),recentTasks=ref<RecipeSyncTask[]>([]),activeTaskNo=ref(""),progress=computed(()=>syncTask.value?.requestedCount?Math.min(100,Math.round(syncTask.value.processedCount*100/syncTask.value.requestedCount)):0);let pollTimer:number|undefined;
function openSync(){syncVisible.value=true} async function loadSyncData(){categoryTree.value=await RecipeAPI.getJumdataCategories();recentTasks.value=await RecipeAPI.getRecentSyncTasks();const running=recentTasks.value.find(t=>["PENDING","RUNNING"].includes(t.status));if(running){activeTaskNo.value=running.taskNo;startPolling(running)}}
let batchCountSeq=0;
function changeBatch(ids:string[]){const oldRows=new Map(batchRows.value.map(r=>[r.nodeId,r]));batchRows.value=ids.map(id=>{const c=flatCategories.value.find(x=>x.id===id)!;const old=oldRows.get(id);return{nodeId:id,pathName:c.pathName,categoryId:c.queryCategoryValue,categoryName:c.pathName,keyword:c.queryKeyword,limit:old?.limit||20,remoteTotal:old?.remoteTotal,availableTotalEstimate:old?.availableTotalEstimate,countLoading:old?.countLoading}});refreshBatchCounts()}
async function refreshBatchCounts(){const seq=++batchCountSeq;await Promise.all(batchRows.value.filter(row=>row.remoteTotal===undefined).map(row=>loadBatchCount(row,seq)))}
async function loadBatchCount(row:BatchJumdataRow,seq:number){row.countLoading=true;try{const p=await RecipeAPI.previewJumdata({categoryId:row.categoryId,categoryName:row.categoryName,keyword:row.keyword,pageNum:1,pageSize:1});if(seq!==batchCountSeq)return;row.remoteTotal=Number(p.remoteTotal||0);row.availableTotalEstimate=Number(p.availableTotalEstimate ?? p.remoteTotal ?? 0);row.limit=Math.min(row.limit,batchLimitMax(row))}finally{if(seq===batchCountSeq)row.countLoading=false}}
function batchLimitMax(row:BatchJumdataRow){return Math.max(1,Math.min(row.remoteTotal||10000,10000))}
function fillBatchLimit(row:BatchJumdataRow){row.limit=Math.max(1,Math.min(row.remoteTotal||1,10000))}
async function startCountSync(){if(!batchRows.value.length)return ElMessage.warning("请选择分类");startPolling(await RecipeAPI.startCategorySync({categories:batchRows.value.map(({categoryId,categoryName,keyword,limit})=>({categoryId,categoryName,keyword,limit,audienceType:syncAudience.value||undefined}))}))}
function changePreviewCategory(){selectedRecipes.clear();previewTable.value?.clearSelection();preview.pageNum=1;previewRows.value=[];previewTotal.value=0;previewPages.value=0;excluded.value=0} function searchPreview(){preview.pageNum=1;selectedRecipes.clear();previewTable.value?.clearSelection();loadPreview()}
async function loadPreview(){const c=flatCategories.value.find(x=>x.id===preview.categoryId);if(!c)return ElMessage.warning("请选择分类");previewLoading.value=true;try{const p=await RecipeAPI.previewJumdata({categoryId:c.queryCategoryValue,categoryName:c.pathName,keyword:preview.keyword||c.queryKeyword,pageNum:preview.pageNum,pageSize:preview.pageSize});previewRows.value=p.list||[];previewTotal.value=Number(p.remoteTotal||0);previewPages.value=Number(p.totalPages||Math.ceil(previewTotal.value/preview.pageSize)||0);excluded.value=p.excludedLocalCount||0}finally{previewLoading.value=false}}
function selectPreview(rows:JumdataRecipePreviewItem[]){const ids=new Set(previewRows.value.map(r=>r.sourceRecipeId));ids.forEach(id=>selectedRecipes.delete(id));rows.forEach(r=>selectedRecipes.set(r.sourceRecipeId,r))}
async function startSelectedSync(){const c=flatCategories.value.find(x=>x.id===preview.categoryId);if(!c||!selectedRecipes.size)return;const task=await RecipeAPI.startSelectedSync({categoryId:c.queryCategoryValue,categoryName:c.pathName,audienceType:syncAudience.value||undefined,sourceRecipeIds:[...selectedRecipes.keys()]});selectedRecipes.clear();previewTable.value?.clearSelection();startPolling(task)}
function resumeTask(no:string){const task=recentTasks.value.find(t=>t.taskNo===no);if(task)startPolling(task)}
function startPolling(task:RecipeSyncTask){syncTask.value=task;activeTaskNo.value=task.taskNo;if(pollTimer)clearTimeout(pollTimer);pollTask()}
async function pollTask(){if(!syncTask.value)return;syncTask.value=await RecipeAPI.getSyncTask(syncTask.value.taskNo);if(["SUCCEEDED","PARTIAL_FAILED","FAILED"].includes(syncTask.value.status)){await fetchRecipes();await loadRecipeCategories();recentTasks.value=await RecipeAPI.getRecentSyncTasks();return}pollTimer=window.setTimeout(pollTask,1500)}

const ruleRows=ref<RecipeRuleItem[]>([]),ruleEditingId=ref<string>(),ruleForm=reactive<RecipeRuleForm>({ruleCode:"",ruleName:"",minMonthAge:0,maxMonthAge:36,matchType:"INGREDIENT",keywords:"",severity:"WARN",suggestion:"",evidenceSource:"",evidenceUrl:"",enabled:1}),ruleEnabled=computed({get:()=>ruleForm.enabled===1,set:v=>ruleForm.enabled=v?1:0});
async function loadRules(){ruleRows.value=await RecipeAPI.getRules()} function openRule(row?:RecipeRuleItem){ruleEditingId.value=row?.id;Object.assign(ruleForm,row||{ruleCode:"",ruleName:"",minMonthAge:0,maxMonthAge:36,matchType:"INGREDIENT",keywords:"",severity:"WARN",suggestion:"",evidenceSource:"",evidenceUrl:"",enabled:1});ruleVisible.value=true}
async function saveRule(){if(!ruleForm.ruleCode||!ruleForm.ruleName||!ruleForm.evidenceSource)return ElMessage.warning("请完整填写规则编码、名称和依据");ruleEditingId.value?await RecipeAPI.updateRule(ruleEditingId.value,ruleForm):await RecipeAPI.createRule(ruleForm);ruleVisible.value=false;loadRules()}
async function disableRule(row:RecipeRuleItem){await ElMessageBox.confirm("确认停用该规则？历史命中记录不会删除。","规则确认");await RecipeAPI.deleteRule(row.id);loadRules()}
watch(mainTab,v=>{if(v==="rules")loadRules()});onMounted(()=>{fetchRecipes();loadRecipeCategories()});onUnmounted(()=>{if(pollTimer)clearTimeout(pollTimer)});
</script>

<style scoped>
.toolbar,.section-title,.preview-footer{display:flex;align-items:center;justify-content:space-between;margin-bottom:14px}.cover{width:54px;height:54px;border-radius:6px}.ingredient-row{display:grid;grid-template-columns:1fr 1fr 1fr 60px;gap:8px;margin-bottom:8px}.step-row{display:grid;grid-template-columns:2fr 220px 60px;gap:10px;align-items:start;margin-bottom:10px}.step-image-field{display:grid;grid-template-columns:58px 1fr;gap:8px;align-items:center}.step-image-preview,.step-image-empty{width:58px;height:58px;border-radius:6px}.step-image-empty{display:flex;align-items:center;justify-content:center;color:var(--el-text-color-secondary);background:var(--el-fill-color-light);border:1px dashed var(--el-border-color)}.preview-bar{display:grid;grid-template-columns:260px 220px auto 1fr;gap:10px;align-items:center;margin-bottom:12px}.preview-footer{gap:16px;margin-top:12px}.preview-footer :deep(.el-pagination){flex:1;justify-content:center}.preview-page-info{width:100px;color:var(--el-text-color-secondary);white-space:nowrap}.limit-cell{display:flex;gap:10px;align-items:center}.task-progress{margin:14px 0}.task-select{width:360px;margin-top:14px}.sync-options{display:flex;align-items:center;gap:12px;margin:14px 0}.mt{margin-top:14px}.section-title{margin-top:18px;margin-bottom:8px}
</style>
