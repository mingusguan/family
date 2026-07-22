package com.youlai.boot.family.service;

import com.youlai.boot.family.model.FamilyModels.FamilyAlbumCreateRequest;
import com.youlai.boot.family.model.FamilyModels.FamilyAlbumVO;
import com.youlai.boot.family.model.FamilyModels.FamilyCreateRequest;
import com.youlai.boot.family.model.FamilyModels.FamilyDetailVO;
import com.youlai.boot.family.model.FamilyModels.FamilyInviteVO;
import com.youlai.boot.family.model.FamilyModels.FamilyVO;

import java.util.List;

/** APP 家庭与家庭相册服务。 */
public interface FamilyService {
    List<FamilyVO> listMyFamilies();

    FamilyVO createFamily(FamilyCreateRequest request);

    FamilyDetailVO getFamilyDetail(Long familyId);

    FamilyInviteVO createInvite(Long familyId);

    FamilyInviteVO getInvite(String code);

    FamilyVO acceptInvite(String code);

    List<FamilyAlbumVO> listAlbums(Long familyId);

    FamilyAlbumVO createAlbum(Long familyId, FamilyAlbumCreateRequest request);

    void ensureCurrentUserMember(Long familyId);

    void ensureAlbumBelongsToFamily(Long albumId, Long familyId);
}
