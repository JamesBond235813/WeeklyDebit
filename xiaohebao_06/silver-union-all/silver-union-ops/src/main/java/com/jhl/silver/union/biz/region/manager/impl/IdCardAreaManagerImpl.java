package com.jhl.silver.union.biz.region.manager.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jhl.silver.union.biz.region.dal.entity.IdCardAreaDO;
import com.jhl.silver.union.biz.region.dal.mapper.IdCardAreaMapper;
import com.jhl.silver.union.biz.region.manager.IdCardAreaManager;
import org.springframework.stereotype.Service;

/**
 * ID card area manager implementation
 */
@Service
public class IdCardAreaManagerImpl extends ServiceImpl<IdCardAreaMapper, IdCardAreaDO>
        implements IdCardAreaManager {
}
