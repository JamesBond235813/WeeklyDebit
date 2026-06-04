package com.jhl.silver.union.biz.customer.manager.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jhl.silver.union.biz.customer.dal.entity.CustDispatchDailyStatDO;
import com.jhl.silver.union.biz.customer.dal.mapper.CustDispatchDailyStatMapper;
import com.jhl.silver.union.biz.customer.manager.CustDispatchDailyStatManager;
import org.springframework.stereotype.Service;

/**
 * 数据分配每日统计 Manager 实现
 */
@Service
public class CustDispatchDailyStatManagerImpl
        extends ServiceImpl<CustDispatchDailyStatMapper, CustDispatchDailyStatDO>
        implements CustDispatchDailyStatManager {
}
