package com.jhl.silver.union.biz.loan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jhl.silver.union.biz.loan.dal.entity.LoanRecordDO;
import com.jhl.silver.union.biz.loan.dal.mapper.LoanRecordMapper;
import com.jhl.silver.union.biz.loan.service.LoanRecordService;
import org.springframework.stereotype.Service;

@Service
public class LoanRecordServiceImpl extends ServiceImpl<LoanRecordMapper, LoanRecordDO> implements LoanRecordService {
}
