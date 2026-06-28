package com.jhl.silver.union.biz.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhl.silver.union.biz.common.ResultCode;
import com.jhl.silver.union.biz.common.enums.BizDictConfigTypeEnum;
import com.jhl.silver.union.biz.common.enums.CommonStatusEnum;
import com.jhl.silver.union.biz.customer.dal.entity.BizDictConfigDO;
import com.jhl.silver.union.biz.customer.manager.BizDictConfigManager;
import com.jhl.silver.union.biz.customer.service.CustRiskRegionService;
import com.jhl.silver.union.commons.exception.BizException;
import com.jhl.silver.union.commons.utils.VerifyUtils;
import com.jhl.silver.union.web.data.CustomerItemDTO;
import com.jhl.silver.union.web.data.admin.AddRiskRegionRequest;
import com.jhl.silver.union.web.data.admin.RiskRegionItemDTO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CustRiskRegionServiceImpl implements CustRiskRegionService {

    @Resource
    private BizDictConfigManager bizDictConfigManager;

    @Override
    public List<RiskRegionItemDTO> list(String regionType) {
        BizDictConfigTypeEnum typeEnum = toBizType(regionType);
        List<BizDictConfigDO> list = bizDictConfigManager.listByDictType(typeEnum, true);
        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        }
        return list.stream()
                .map(item -> new RiskRegionItemDTO()
                        .setId(item.getId())
                        .setRegionType(regionType)
                        .setProvince(item.getLabel())
                        .setCity(item.getDescription())
                        .setGmtCreate(item.getGmtCreate()))
                .collect(Collectors.toList());
    }

    @Override
    public void add(AddRiskRegionRequest request, Long optUserId) {
        request.normalize();
        BizDictConfigTypeEnum typeEnum = toBizType(request.getRegionType());
        VerifyUtils.notBlank(request.getProvince(), "province", "请选择省份", true);
        VerifyUtils.notBlank(request.getCity(), "city", "请选择城市", true);
        List<BizDictConfigDO> existing = bizDictConfigManager.list(new LambdaQueryWrapper<BizDictConfigDO>()
                .eq(BizDictConfigDO::getBizType, typeEnum.name())
                .eq(BizDictConfigDO::getLabel, request.getProvince())
                .eq(BizDictConfigDO::getDescription, request.getCity()));
        if (!CollectionUtils.isEmpty(existing)) {
            BizDictConfigDO first = existing.get(0);
            if (Objects.equals(first.getStatus(), CommonStatusEnum.OK.status)) {
                throw new BizException(ResultCode.ADD_FAILED_FOR_DUPLICATED_BIZ_TYPE_VALUE,
                        typeEnum.desc + ":" + request.getProvince() + request.getCity());
            }
            bizDictConfigManager.updateById(new BizDictConfigDO()
                    .setId(first.getId())
                    .setStatus(CommonStatusEnum.OK.status)
                    .setOptUserId(optUserId));
            bizDictConfigManager.clearCache(typeEnum);
            return;
        }
        int nextValue = nextIntValue(typeEnum);
        BizDictConfigDO config = new BizDictConfigDO()
                .setBizType(typeEnum.name())
                .setTypeDesc(typeEnum.desc)
                .setIntValue(nextValue)
                .setLabel(request.getProvince())
                .setDescription(request.getCity())
                .setStatus(CommonStatusEnum.OK.status)
                .setOptUserId(optUserId);
        bizDictConfigManager.save(config);
        bizDictConfigManager.clearCache(typeEnum);
    }

    @Override
    public void delete(Long id) {
        VerifyUtils.notNull(id, "id", "请选择需要删除的地区", true);
        BizDictConfigDO existing = bizDictConfigManager.getById(id);
        if (existing == null) {
            return;
        }
        BizDictConfigTypeEnum typeEnum = BizDictConfigTypeEnum.findByName(existing.getBizType());
        if (typeEnum != BizDictConfigTypeEnum.CUSTOMER_RISK_REGION
                && typeEnum != BizDictConfigTypeEnum.CUSTOMER_BLACK_REGION) {
            throw new BizException(ResultCode.SYS_NO_AUTH, "risk region config");
        }
        bizDictConfigManager.updateById(new BizDictConfigDO()
                .setId(id)
                .setStatus(CommonStatusEnum.FORBIDDEN.status));
        bizDictConfigManager.clearCache(typeEnum);
    }

    @Override
    public void setupRiskFlags(List<CustomerItemDTO> customers) {
        if (CollectionUtils.isEmpty(customers)) {
            return;
        }
        List<RiskRegionItemDTO> riskList = list(TYPE_RISK);
        List<RiskRegionItemDTO> blackList = list(TYPE_BLACK);
        customers.forEach(item -> {
            item.setRiskRegionHit(hitAny(item, riskList));
            item.setBlackRegionHit(hitAny(item, blackList));
        });
    }

    private int nextIntValue(BizDictConfigTypeEnum typeEnum) {
        List<BizDictConfigDO> list = bizDictConfigManager.listByDictType(typeEnum, false);
        if (CollectionUtils.isEmpty(list)) {
            return 1;
        }
        return list.stream()
                .map(BizDictConfigDO::getIntValue)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    private boolean hitAny(CustomerItemDTO item, List<RiskRegionItemDTO> regions) {
        if (CollectionUtils.isEmpty(regions)) {
            return false;
        }
        return regions.stream().anyMatch(region -> hit(item, region));
    }

    private boolean hit(CustomerItemDTO item, RiskRegionItemDTO region) {
        String province = normalizeArea(region.getProvince());
        String city = normalizeArea(region.getCity());
        if (StringUtils.isBlank(province) || StringUtils.isBlank(city)) {
            return false;
        }
        String hukouProvince = normalizeArea(item.getHukouProvince());
        String hukouCity = normalizeArea(item.getHukouCity());
        boolean idCardHit = StringUtils.equals(hukouProvince, province)
                && (StringUtils.equals(hukouCity, city) || StringUtils.equals(province, city));
        String mobileArea = normalizeArea(item.getMobileArea());
        boolean mobileHit = StringUtils.contains(mobileArea, province) && StringUtils.contains(mobileArea, city);
        return idCardHit || mobileHit;
    }

    private String normalizeArea(String value) {
        if (StringUtils.isBlank(value)) {
            return "";
        }
        return StringUtils.deleteWhitespace(value)
                .replace("特别行政区", "")
                .replace("维吾尔自治区", "")
                .replace("壮族自治区", "")
                .replace("回族自治区", "")
                .replace("自治区", "")
                .replace("省", "")
                .replace("市", "")
                .replace("地区", "")
                .replace("盟", "");
    }

    private BizDictConfigTypeEnum toBizType(String regionType) {
        String normalized = StringUtils.upperCase(StringUtils.trim(regionType));
        if (TYPE_RISK.equals(normalized)) {
            return BizDictConfigTypeEnum.CUSTOMER_RISK_REGION;
        }
        if (TYPE_BLACK.equals(normalized)) {
            return BizDictConfigTypeEnum.CUSTOMER_BLACK_REGION;
        }
        throw new BizException(ResultCode.IMPORT_CUST_FAILED, "请指定正确的地区类型");
    }
}
