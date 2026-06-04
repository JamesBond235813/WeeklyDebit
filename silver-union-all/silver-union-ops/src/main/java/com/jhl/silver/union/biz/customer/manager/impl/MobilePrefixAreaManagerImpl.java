package com.jhl.silver.union.biz.customer.manager.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jhl.silver.union.biz.customer.dal.entity.MobilePrefixAreaDO;
import com.jhl.silver.union.biz.customer.dal.mapper.MobilePrefixAreaMapper;
import com.jhl.silver.union.biz.customer.manager.MobilePrefixAreaManager;
import org.springframework.stereotype.Service;

/**
 * Mobile prefix area service implementation
 */
@Service
public class MobilePrefixAreaManagerImpl extends ServiceImpl<MobilePrefixAreaMapper, MobilePrefixAreaDO>
        implements MobilePrefixAreaManager {
}
