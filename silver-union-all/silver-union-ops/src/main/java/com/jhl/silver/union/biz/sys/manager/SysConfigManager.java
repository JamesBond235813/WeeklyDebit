package com.jhl.silver.union.biz.sys.manager;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jhl.silver.union.biz.sys.dal.entity.SysConfigDO;

/**
 * 平台配置信息 服务类
 */
public interface SysConfigManager extends IService<SysConfigDO> {

    /**
     * 根据配置类型与配置key查询有效配置
     *
     * @param cnfType 配置类型
     * @param cnfKey  配置信息 key
     * @return
     */
    SysConfigDO getByTypeAndKey(String cnfType, String cnfKey);
}
