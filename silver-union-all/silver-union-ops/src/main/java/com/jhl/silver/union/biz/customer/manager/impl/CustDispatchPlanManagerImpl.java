package com.jhl.silver.union.biz.customer.manager.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jhl.silver.union.biz.customer.dal.entity.CustDispatchPlanDO;
import com.jhl.silver.union.biz.customer.dal.mapper.CustDispatchPlanMapper;
import com.jhl.silver.union.biz.customer.manager.CustDispatchPlanManager;
import org.springframework.stereotype.Service;

/**
 * 数据分配方案 Manager 实现
 */
@Service
public class CustDispatchPlanManagerImpl
        extends ServiceImpl<CustDispatchPlanMapper, CustDispatchPlanDO>
        implements CustDispatchPlanManager {
}
