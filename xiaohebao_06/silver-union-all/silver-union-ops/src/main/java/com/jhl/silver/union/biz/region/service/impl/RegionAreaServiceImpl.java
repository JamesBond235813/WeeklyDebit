package com.jhl.silver.union.biz.region.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhl.silver.union.biz.region.dal.entity.RegionAreaDO;
import com.jhl.silver.union.biz.region.manager.RegionAreaManager;
import com.jhl.silver.union.biz.region.service.RegionAreaService;
import com.jhl.silver.union.web.data.region.RegionItemDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RegionAreaServiceImpl implements RegionAreaService {
    @Resource
    private RegionAreaManager regionAreaManager;

    @Override
    public List<RegionItemDTO> listChildren(Long parentId) {
        Long targetParentId = parentId == null ? 0L : parentId;
        List<RegionAreaDO> children = regionAreaManager.list(
                new LambdaQueryWrapper<RegionAreaDO>()
                        .eq(RegionAreaDO::getParentId, targetParentId)
                        .orderByAsc(RegionAreaDO::getId));
        if (CollectionUtils.isEmpty(children)) {
            return List.of();
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
}
