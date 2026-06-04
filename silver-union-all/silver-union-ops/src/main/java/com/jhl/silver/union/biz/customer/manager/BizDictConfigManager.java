package com.jhl.silver.union.biz.customer.manager;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jhl.silver.union.biz.common.enums.BizDictConfigTypeEnum;
import com.jhl.silver.union.biz.customer.dal.entity.BizDictConfigDO;

import java.util.List;

/**
 * <p>
 * 业务字典配置信息。 这里配置的字典信息仅用于标记、展示，不进行业务处理 服务类
 * </p>
 *
 * @author Way
 * @since 2025-03-29 00:47:28
 */
public interface BizDictConfigManager extends IService<BizDictConfigDO> {

    /**
     * 根据字典类型取字典配置信息
     *
     * @param configTypeEnum
     * @param forceValid     true: 仅返回生效状态的字典配置信息
     * @return
     */
    List<BizDictConfigDO> listByDictType(BizDictConfigTypeEnum configTypeEnum, boolean forceValid);

    /**
     * 清缓存
     *
     * @param configTypeEnum 业务字典类型 若参数 为null 表示全量清缓存
     */
    void clearCache(BizDictConfigTypeEnum configTypeEnum);

}
