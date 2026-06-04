package com.jhl.silver.union.biz.customer.manager.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jhl.silver.union.biz.customer.dal.entity.CustDispatchCursorDO;
import com.jhl.silver.union.biz.customer.dal.mapper.CustDispatchCursorMapper;
import com.jhl.silver.union.biz.customer.manager.CustDispatchCursorManager;
import org.springframework.stereotype.Service;

/**
 * 数据分配轮转游标 Manager 实现
 */
@Service
public class CustDispatchCursorManagerImpl
        extends ServiceImpl<CustDispatchCursorMapper, CustDispatchCursorDO>
        implements CustDispatchCursorManager {
}
