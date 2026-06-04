package com.jhl.silver.union.biz.sys.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jhl.silver.union.biz.common.BizConstance;
import com.jhl.silver.union.biz.sys.dal.entity.SysConfigDO;
import com.jhl.silver.union.biz.sys.dal.mapper.SysConfigMapper;
import com.jhl.silver.union.biz.sys.manager.SysConfigManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 平台配置信息 服务实现类
 */
@Service
public class SysConfigManagerImpl extends ServiceImpl<SysConfigMapper, SysConfigDO>
        implements SysConfigManager {

    @Override
    public SysConfigDO getByTypeAndKey(String cnfType, String cnfKey) {
        if (StringUtils.isBlank(cnfType) || StringUtils.isBlank(cnfKey)) {
            return null;
        }
        LambdaQueryWrapper<SysConfigDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfigDO::getCnfType, cnfType)
                .eq(SysConfigDO::getCnfKey, cnfKey)
                .eq(SysConfigDO::getDeleteFlag, BizConstance.NOT_DELETED)
                .last("limit 1");
        return this.getOne(wrapper);
    }
}
