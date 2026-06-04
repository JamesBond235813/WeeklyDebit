package com.jhl.silver.union.biz.customer.manager.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jhl.silver.union.biz.customer.dal.entity.CustNoticeDO;
import com.jhl.silver.union.biz.customer.dal.mapper.CustNoticeMapper;
import com.jhl.silver.union.biz.customer.manager.CustNoticeManager;
import org.springframework.stereotype.Service;

/**
 * 客户通知服务实现
 */
@Service
public class CustNoticeManagerImpl
        extends ServiceImpl<CustNoticeMapper, CustNoticeDO>
        implements CustNoticeManager {
}
