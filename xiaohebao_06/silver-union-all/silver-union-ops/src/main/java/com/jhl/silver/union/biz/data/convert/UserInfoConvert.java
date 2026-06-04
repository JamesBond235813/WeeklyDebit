package com.jhl.silver.union.biz.data.convert;

import com.jhl.silver.union.biz.user.dal.entity.SuUserInfoDO;
import com.jhl.silver.union.commons.gson.GsonHelper;
import com.jhl.silver.union.web.data.UserInfoDTO;
import com.jhl.silver.union.web.data.admin.AddUserInfoRequest;
import com.jhl.silver.union.web.data.admin.UpdateUserInfoRequest;
import com.jhl.silver.union.web.data.user.UserItemInfo;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author: qingren
 * @create_time: 2025/3/27
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        imports = { StringUtils.class, GsonHelper.class, CollectionUtils.class })
public interface UserInfoConvert {

    /**
     * convert2UserInfoDTO
     *
     * @param infoDO
     * @return
     */
    @Mapping(target = "roles",
             expression = "java(StringUtils.isNotBlank(infoDO.getRoles()) ? GsonHelper.fromJson(infoDO.getRoles(), List.class) : List.of())")
    UserInfoDTO convert2UserInfoDTO(SuUserInfoDO infoDO);

    /**
     * convert2UserInfoDTOList
     *
     * @param infoDO
     * @return
     */
    List<UserInfoDTO> convert2UserInfoDTOList(List<SuUserInfoDO> infoDO);

    /**
     * convert2SuUserInfoDO
     *
     * @param request
     * @return
     */
    @Mapping(target = "roles",
             expression = "java(CollectionUtils.isEmpty(request.getRoles()) ? StringUtils.EMPTY : GsonHelper.toJson(request.getRoles()))")
    SuUserInfoDO convert2SuUserInfoDO(AddUserInfoRequest request);

    /**
     * convert2SuUserInfoDO
     *
     * @param request
     * @return
     */
    @Mapping(target = "roles",
             expression = "java(CollectionUtils.isEmpty(request.getRoles()) ? StringUtils.EMPTY : GsonHelper.toJson(request.getRoles()))")
    SuUserInfoDO convert2SuUserInfoDO(UpdateUserInfoRequest request);

    /**
     * convert2UserItemInfo
     *
     * @param infoDO
     * @return
     */
    @Mapping(target = "name",source = "infoDO.realName")
    @Mapping(target = "deptId",source = "infoDO.departmentId")
    UserItemInfo convert2UserItemInfo(SuUserInfoDO infoDO);

    /**
     * convert2UserItemInfoList
     *
     * @param infoDOList
     * @return
     */
    List<UserItemInfo> convert2UserItemInfoList(List<SuUserInfoDO> infoDOList);
}
