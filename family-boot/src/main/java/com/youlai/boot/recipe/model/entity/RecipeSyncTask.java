package com.youlai.boot.recipe.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.youlai.boot.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Persistent progress record for a recipe synchronization job. */
@Getter
@Setter
@TableName("recipe_sync_task")
public class RecipeSyncTask extends BaseEntity {
    private String taskNo;
    private String provider;
    private String mode;
    private String status;
    private String requestPayload;
    private Integer requestedCount;
    private Integer totalCount;
    private Integer processedCount;
    private Integer importedCount;
    private Integer duplicatedCount;
    private Integer blockedCount;
    private Integer warnedCount;
    private Integer failedCount;
    private String errorMessage;
    private String failureDetailsJson;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    public static RecipeSyncTask pending(String taskNo, String mode, String requestPayload, int totalCount) {
        RecipeSyncTask task = new RecipeSyncTask();
        task.taskNo = taskNo;
        task.provider = "JISU";
        task.mode = mode;
        task.status = "PENDING";
        task.requestPayload = requestPayload;
        task.requestedCount = totalCount;
        task.totalCount = totalCount;
        task.processedCount = 0;
        task.importedCount = 0;
        task.duplicatedCount = 0;
        task.blockedCount = 0;
        task.warnedCount = 0;
        task.failedCount = 0;
        return task;
    }

    public void start() {
        status = "RUNNING";
        startedAt = LocalDateTime.now();
    }

    public void addProgress(int processed, int imported, int duplicated, int blocked, int warned, int failed) {
        processedCount += processed;
        importedCount += imported;
        duplicatedCount += duplicated;
        blockedCount += blocked;
        warnedCount += warned;
        failedCount += failed;
    }

    public void succeed() {
        boolean notEnoughRemoteData = requestedCount != null
                && processedCount != null
                && processedCount < requestedCount;
        status = failedCount > 0 || notEnoughRemoteData ? "PARTIAL_FAILED" : "SUCCEEDED";
        if (notEnoughRemoteData && errorMessage == null) {
            errorMessage = "远端可同步菜谱少于本次请求数量，已导入可获取部分";
        }
        finishedAt = LocalDateTime.now();
    }

    public void recordFailures(String detailsJson) {
        this.failureDetailsJson = detailsJson;
    }

    public void recoverAsFailed() {
        status = "FAILED";
        errorMessage = "服务重启导致任务中断，请重新发起同步";
        finishedAt = LocalDateTime.now();
    }

    public void fail(Throwable throwable) {
        status = "FAILED";
        failedCount += 1;
        String message = throwable == null ? "Unknown error" : throwable.getMessage();
        errorMessage = message == null ? throwable.getClass().getSimpleName() : message;
        if (errorMessage.length() > 1000) errorMessage = errorMessage.substring(0, 1000);
        finishedAt = LocalDateTime.now();
    }
}
