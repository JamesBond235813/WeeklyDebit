package com.jhl.silver.union.biz.region.manager.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jhl.silver.union.biz.region.dal.entity.RegionAreaDO;
import com.jhl.silver.union.biz.region.dal.mapper.RegionAreaMapper;
import com.jhl.silver.union.biz.region.manager.RegionAreaManager;
import org.springframework.stereotype.Service;

/**
 * Region area manager implementation
 */
@Service
public class RegionAreaManagerImpl extends ServiceImpl<RegionAreaMapper, RegionAreaDO>
        implements RegionAreaManager {
}
