package com.jhl.silver.union.biz.region.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhl.silver.union.biz.region.dal.entity.RegionAreaDO;
import com.jhl.silver.union.biz.region.dal.entity.IdCardAreaDO;
import com.jhl.silver.union.biz.region.manager.IdCardAreaManager;
import com.jhl.silver.union.biz.region.manager.RegionAreaManager;
import com.jhl.silver.union.biz.region.service.RegionAreaService;
import com.jhl.silver.union.web.data.region.RegionItemDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RegionAreaServiceImpl implements RegionAreaService {
    private static final Set<String> MUNICIPALITY_CODES = Set.of("110000", "120000", "310000", "500000");

    @Resource
    private RegionAreaManager regionAreaManager;
    @Resource
    private IdCardAreaManager idCardAreaManager;

    @Override
    public List<RegionItemDTO> listChildren(Long parentId) {
        Long targetParentId = parentId == null ? 0L : parentId;
        List<RegionAreaDO> children = regionAreaManager.list(
                new LambdaQueryWrapper<RegionAreaDO>()
                        .eq(RegionAreaDO::getParentId, targetParentId)
                        .orderByAsc(RegionAreaDO::getId));
        if (CollectionUtils.isEmpty(children)) {
            return listFromIdCardArea(targetParentId);
        }
        List<Long> childIds = children.stream().map(RegionAreaDO::getId).collect(Collectors.toList());
        Set<Long> parentIdsWithChildren = Collections.emptySet();
        if (!CollectionUtils.isEmpty(childIds)) {
            List<RegionAreaDO> parentList = regionAreaManager.list(
                    new LambdaQueryWrapper<RegionAreaDO>()
                            .select(RegionAreaDO::getParentId)
                            .in(RegionAreaDO::getParentId, childIds)
                            .groupBy(RegionAreaDO::getParentId));
            parentIdsWithChildren = parentList.stream()
                    .map(RegionAreaDO::getParentId)
                    .collect(Collectors.toSet());
        }
        final Set<Long> finalParentIdsWithChildren = parentIdsWithChildren;
        return children.stream()
                .map(item -> new RegionItemDTO()
                        .setId(item.getId())
                        .setName(item.getName())
                        .setLevel(item.getLevel())
                        .setLeaf(!finalParentIdsWithChildren.contains(item.getId())))
                .collect(Collectors.toList());
    }

    private List<RegionItemDTO> listFromIdCardArea(Long parentId) {
        List<IdCardAreaDO> all = idCardAreaManager.list();
        if (CollectionUtils.isEmpty(all)) {
            return List.of();
        }
        if (parentId == null || parentId == 0L) {
            return all.stream()
                    .filter(item -> item.getCode() != null && item.getCode().endsWith("0000"))
                    .sorted(Comparator.comparing(IdCardAreaDO::getCode))
                    .map(item -> new RegionItemDTO()
                            .setId(Long.valueOf(item.getCode()))
                            .setName(cleanAreaName(item.getName()))
                            .setLevel(1)
                            .setLeaf(false))
                    .collect(Collectors.toList());
        }
        String provinceCode = String.format("%06d", parentId);
        if (!provinceCode.endsWith("0000")) {
            return List.of();
        }
        IdCardAreaDO province = all.stream()
                .filter(item -> Objects.equals(item.getCode(), provinceCode))
                .findFirst()
                .orElse(null);
        if (province == null) {
            return List.of();
        }
        if (MUNICIPALITY_CODES.contains(provinceCode)) {
            return List.of(new RegionItemDTO()
                    .setId(Long.valueOf(provinceCode))
                    .setName(cleanAreaName(province.getName()))
                    .setLevel(2)
                    .setLeaf(true));
        }
        String prefix = provinceCode.substring(0, 2);
        return all.stream()
                .filter(item -> {
                    String code = item.getCode();
                    return code != null
                            && code.startsWith(prefix)
                            && (code.endsWith("00") || code.startsWith(prefix + "90"))
                            && !code.endsWith("0000");
                })
                .sorted(Comparator.comparing(IdCardAreaDO::getCode))
                .map(item -> new RegionItemDTO()
                        .setId(Long.valueOf(item.getCode()))
                        .setName(cleanAreaName(item.getName()))
                        .setLevel(2)
                        .setLeaf(true))
                .collect(Collectors.toList());
    }

    private String cleanAreaName(String name) {
        if (name == null) {
            return null;
        }
        return name.replaceAll("\\*$", "");
    }
}
