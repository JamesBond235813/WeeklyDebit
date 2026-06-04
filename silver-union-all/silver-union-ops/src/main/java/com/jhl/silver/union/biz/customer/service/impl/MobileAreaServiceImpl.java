package com.jhl.silver.union.biz.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhl.silver.union.biz.customer.dal.entity.MobilePrefixAreaDO;
import com.jhl.silver.union.biz.customer.manager.MobilePrefixAreaManager;
import com.jhl.silver.union.biz.customer.service.MobileAreaService;
import com.jhl.silver.union.biz.data.MobileAreaInfo;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class MobileAreaServiceImpl implements MobileAreaService {
    private static final int MOBILE_PREFIX_LEN = 7;

    @Resource
    private MobilePrefixAreaManager mobilePrefixAreaManager;

    @Override
    public MobileAreaInfo getMobileArea(String mobile) {
        if (StringUtils.isBlank(mobile) || mobile.length() < MOBILE_PREFIX_LEN) {
            return null;
        }
        String prefix = mobile.substring(0, MOBILE_PREFIX_LEN);
        MobilePrefixAreaDO areaDO = mobilePrefixAreaManager.getOne(
                new LambdaQueryWrapper<MobilePrefixAreaDO>()
                        .eq(MobilePrefixAreaDO::getMobilePre, prefix)
                        .last("limit 1"));
        if (areaDO == null) {
            return null;
        }
        return new MobileAreaInfo()
                .setProvince(areaDO.getProvince())
                .setCity(areaDO.getCity());
    }
}
