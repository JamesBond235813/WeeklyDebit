package com.jhl.silver.union.biz.region.service.impl;

import com.jhl.silver.union.biz.data.IdCardAreaInfo;
import com.jhl.silver.union.biz.region.dal.entity.IdCardAreaDO;
import com.jhl.silver.union.biz.region.manager.IdCardAreaManager;
import com.jhl.silver.union.biz.region.service.IdCardAreaService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class IdCardAreaServiceImpl implements IdCardAreaService {
    private static final String ID_CARD_REGEX = "[0-9]{17}([0-9]|[xX])";
    private static final String AREA_CODE_REGEX = "[0-9]{6}";
    private static final Set<String> MUNICIPALITY_CODES = Set.of("110000", "120000", "310000", "500000");

    @Resource
    private IdCardAreaManager idCardAreaManager;

    @Override
    public IdCardAreaInfo getAreaInfoByIdCard(String idCardNo) {
        String normalized = StringUtils.upperCase(StringUtils.trim(idCardNo));
        if (StringUtils.isBlank(normalized)) {
            return null;
        }
        String areaCode;
        if (normalized.matches(AREA_CODE_REGEX)) {
            areaCode = normalized;
        } else if (normalized.matches(ID_CARD_REGEX)) {
            areaCode = normalized.substring(0, 6);
        } else {
            return null;
        }
        return getAreaInfoByCode(areaCode);
    }

    @Override
    public IdCardAreaInfo getAreaInfoByCode(String areaCode) {
        if (StringUtils.isBlank(areaCode) || !areaCode.matches(AREA_CODE_REGEX)) {
            return null;
        }
        String provinceCode = areaCode.substring(0, 2) + "0000";
        String cityCode = areaCode.substring(0, 4) + "00";
        Set<String> codes = new LinkedHashSet<>(List.of(provinceCode, cityCode, areaCode));
        List<IdCardAreaDO> list = idCardAreaManager.listByIds(codes);
        if (list == null || list.isEmpty()) {
            return null;
        }
        Map<String, String> nameMap = list.stream()
                .collect(Collectors.toMap(IdCardAreaDO::getCode, IdCardAreaDO::getName, (v1, v2) -> v2));
        String province = nameMap.get(provinceCode);
        String city = nameMap.get(cityCode);
        if (StringUtils.isBlank(city) && MUNICIPALITY_CODES.contains(provinceCode)) {
            city = province;
        }
        return new IdCardAreaInfo()
                .setProvince(province)
                .setCity(city)
                .setDistrict(nameMap.get(areaCode));
    }
}
