package com.jhl.silver.union.biz.data.convert;

import com.jhl.silver.union.biz.common.enums.BizFlagEnum;
import com.jhl.silver.union.biz.common.enums.HouseFlagEnum;
import com.jhl.silver.union.biz.common.enums.MarriageFlagEnum;
import com.jhl.silver.union.biz.common.enums.SexEnum;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerInfoItemDO;
import com.jhl.silver.union.web.data.CustomerItemDTO;
import com.jhl.silver.union.web.data.admin.AddCustomerInfoRequest;
import com.jhl.silver.union.web.data.admin.UpdCustomerInfoRequest;
import com.jhl.silver.union.web.data.customer.CustomerItemDetailDTO;
import com.jhl.silver.union.web.data.customer.UpdateBizCustomerInfoRequest;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * @author: qingren
 * @create_time: 2025/3/30
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        imports = { StringUtils.class, BizFlagEnum.class, HouseFlagEnum.class, MarriageFlagEnum.class, SexEnum.class })
public interface CustomerConvert {

    /**
     * convert2CustomerItemDTO
     *
     * @param itemDO
     * @return
     */
    @Mapping(target = "sexDesc", expression = "java(SexEnum.getDesc(itemDO.getSex()))")
    @Mapping(target = "houseFlagDesc", expression = "java(HouseFlagEnum.getDescByFlag(itemDO.getHouseFlag()))")
    @Mapping(target = "insuranceFlagDesc", expression = "java(BizFlagEnum.getDescByFlag(itemDO.getInsuranceFlag()))")
    @Mapping(target = "socialInsuranceFlagDesc",
             expression = "java(BizFlagEnum.getDescByFlag(itemDO.getSocialInsuranceFlag()))")
    @Mapping(target = "carFlagDesc", expression = "java(BizFlagEnum.getDescByFlag(itemDO.getCarFlag()))")
    @Mapping(target = "providentFlagDesc", expression = "java(BizFlagEnum.getDescByFlag(itemDO.getProvidentFlag()))")
    @Mapping(target = "creditCardFlagDesc", expression = "java(BizFlagEnum.getDescByFlag(itemDO.getCreditCardFlag()))")
    @Mapping(target = "enterpriseFlagDesc",
             expression = "java(BizFlagEnum.getYesNoDescByFlag(itemDO.getEnterpriseFlag()))")
    @Mapping(target = "marriageStatusDesc",
             expression = "java(MarriageFlagEnum.getDescByFlag(itemDO.getMarriageStatus()))")
    @Named("convert2CustomerItemDTO")
    CustomerItemDTO convert2CustomerItemDTO(CustomerInfoItemDO itemDO);

    /**
     * convert2CustomerItemDTOList
     *
     * @param itemDOList
     * @return
     */
    List<CustomerItemDTO> convert2CustomerItemDTOList(List<CustomerInfoItemDO> itemDOList);

    /**
     * convertCustomerItemDetailDTO
     *
     * @param itemDO
     * @return
     */
    @Mapping(target = "sexDesc", expression = "java(SexEnum.getDesc(itemDO.getSex()))")
    @Mapping(target = "houseFlagDesc", expression = "java(HouseFlagEnum.getDescByFlag(itemDO.getHouseFlag()))")
    @Mapping(target = "insuranceFlagDesc", expression = "java(BizFlagEnum.getDescByFlag(itemDO.getInsuranceFlag()))")
    @Mapping(target = "socialInsuranceFlagDesc",
             expression = "java(BizFlagEnum.getDescByFlag(itemDO.getSocialInsuranceFlag()))")
    @Mapping(target = "carFlagDesc", expression = "java(BizFlagEnum.getDescByFlag(itemDO.getCarFlag()))")
    @Mapping(target = "providentFlagDesc", expression = "java(BizFlagEnum.getDescByFlag(itemDO.getProvidentFlag()))")
    @Mapping(target = "creditCardFlagDesc", expression = "java(BizFlagEnum.getDescByFlag(itemDO.getCreditCardFlag()))")
    @Mapping(target = "enterpriseFlagDesc",
             expression = "java(BizFlagEnum.getYesNoDescByFlag(itemDO.getEnterpriseFlag()))")
    @Mapping(target = "marriageStatusDesc",
             expression = "java(MarriageFlagEnum.getDescByFlag(itemDO.getMarriageStatus()))")
    @Named("convertCustomerItemDetailDTO")
    CustomerItemDetailDTO convertCustomerItemDetailDTO(CustomerInfoItemDO itemDO);

    /**
     * convert2CustomerInfoItemDO
     *
     * @param request
     * @return
     */
    CustomerInfoItemDO convert2CustomerInfoItemDO(UpdateBizCustomerInfoRequest request);

    /**
     * convert2CustomerInfoItemDOFromAddCustomerInfoRequest
     *
     * @param request
     * @return
     */
    CustomerInfoItemDO convert2CustomerInfoItemDOFromAddCustomerInfoRequest(AddCustomerInfoRequest request);

    /**
     * convert2CustomerInfoItemDOFromUpdCustomerInfoRequest
     *
     * @param request
     * @return
     */
    CustomerInfoItemDO convert2CustomerInfoItemDOFromUpdCustomerInfoRequest(UpdCustomerInfoRequest request);
}
