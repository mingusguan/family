package com.youlai.boot.album.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youlai.boot.album.model.entity.AlbumUploadBatch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/** 相册直传批次 Mapper。 */
@Mapper
public interface AlbumUploadBatchMapper extends BaseMapper<AlbumUploadBatch> {
    @Update("UPDATE app_album_upload_batch SET processing_count = #{processing}, failed_count = #{failed}, "
            + "status = CASE WHEN #{processing} = 0 THEN 'COMPLETED' ELSE 'PROCESSING' END, update_time = NOW() "
            + "WHERE batch_id = #{batchId} AND status = 'INIT'")
    int confirm(@Param("batchId") String batchId, @Param("processing") int processing,
                @Param("failed") int failed);

    @Update("UPDATE app_album_upload_batch SET success_count = success_count + 1, "
            + "status = CASE WHEN processing_count <= 1 THEN 'COMPLETED' ELSE status END, "
            + "processing_count = GREATEST(processing_count - 1, 0), update_time = NOW() "
            + "WHERE batch_id = #{batchId}")
    int incrementSuccess(@Param("batchId") String batchId);

    @Update("UPDATE app_album_upload_batch SET failed_count = failed_count + 1, "
            + "status = CASE WHEN processing_count <= 1 THEN 'COMPLETED' ELSE status END, "
            + "processing_count = GREATEST(processing_count - 1, 0), update_time = NOW() "
            + "WHERE batch_id = #{batchId}")
    int incrementFailure(@Param("batchId") String batchId);
}
