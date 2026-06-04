package com.jhl.silver.union.biz.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jhl.silver.union.biz.platform.dal.entity.BizPlatformDO;
import com.jhl.silver.union.biz.platform.dal.mapper.BizPlatformMapper;
import com.jhl.silver.union.biz.platform.service.BizPlatformService;
import org.springframework.stereotype.Service;

@Service
public class BizPlatformServiceImpl extends ServiceImpl<BizPlatformMapper, BizPlatformDO>
        implements BizPlatformService {
}
