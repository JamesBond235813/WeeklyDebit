package com.jhl.silver.union.biz.risk.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum;
import com.jhl.silver.union.biz.risk.dal.entity.RiskControlReportDO;
import com.jhl.silver.union.web.data.risk.RiskCustomerItemDTO;
import com.jhl.silver.union.web.data.risk.RiskHistoryPageRequest;
import java.util.List;
import java.util.Set;

public interface RiskReportService {

    List<RiskCustomerItemDTO> searchCustomers(String keyword, Long optUserId, Long optUserDeptId,
            Set<UserAuthRoleEnum> roles);

    RiskControlReportDO getReport(String name, String idCard, String phone, Long optUserId, Long optUserDeptId,
            Set<UserAuthRoleEnum> roles);

    RiskControlReportDO getRadarReport(String name, String idCard, String phone, Long optUserId, Long optUserDeptId,
            Set<UserAuthRoleEnum> roles);

    IPage<RiskControlReportDO> pageHistory(RiskHistoryPageRequest request, Long optUserId, Long optUserDeptId,
            Set<UserAuthRoleEnum> roles);
}
