package com.jhl.silver.union.biz.risk.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jhl.silver.union.biz.risk.dal.entity.RiskControlReportDO;
import com.jhl.silver.union.biz.risk.dal.mapper.RiskControlReportMapper;
import com.jhl.silver.union.biz.risk.service.RiskControlReportService;
import org.springframework.stereotype.Service;

@Service
public class RiskControlReportServiceImpl extends ServiceImpl<RiskControlReportMapper, RiskControlReportDO>
        implements RiskControlReportService {
}
