package com.jhl.silver.union.biz.region.service;

import com.jhl.silver.union.web.data.region.RegionItemDTO;

import java.util.List;

public interface RegionAreaService {
    List<RegionItemDTO> listChildren(Long parentId);
}
