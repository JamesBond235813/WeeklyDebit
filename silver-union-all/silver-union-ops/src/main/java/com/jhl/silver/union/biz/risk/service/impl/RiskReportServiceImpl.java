package com.jhl.silver.union.biz.risk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhl.silver.union.biz.common.BizConstance;
import com.jhl.silver.union.biz.common.ResultCode;
import com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerInfoItemDO;
import com.jhl.silver.union.biz.customer.manager.CustomerInfoItemManager;
import com.jhl.silver.union.biz.dept.service.DeptService;
import com.jhl.silver.union.biz.risk.dal.entity.RiskControlReportDO;
import com.jhl.silver.union.biz.risk.integration.PanoramaClient;
import com.jhl.silver.union.biz.risk.service.RiskControlReportService;
import com.jhl.silver.union.biz.risk.service.RiskReportService;
import com.jhl.silver.union.commons.exception.BizException;
import com.jhl.silver.union.commons.utils.OtherUtils;
import com.jhl.silver.union.commons.utils.VerifyUtils;
import com.jhl.silver.union.web.data.risk.RiskCustomerItemDTO;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskReportServiceImpl implements RiskReportService {

    private final PanoramaClient panoramaClient;
    private final RiskControlReportService riskControlReportService;
    private final CustomerInfoItemManager customerInfoItemManager;
    private final DeptService deptService;

    @Override
    public List<RiskCustomerItemDTO> searchCustomers(String keyword, Long optUserId, Long optUserDeptId,
            Set<UserAuthRoleEnum> roles) {
        if (StringUtils.isBlank(keyword)) {
            return List.of();
        }

        LambdaQueryWrapper<CustomerInfoItemDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(qw -> qw.like(CustomerInfoItemDO::getName, keyword)
                .or()
                .like(CustomerInfoItemDO::getMobile, keyword)
                .or()
                .like(CustomerInfoItemDO::getIdCardNo, keyword))
                .orderByDesc(CustomerInfoItemDO::getId);
        applyPermission(wrapper, optUserId, optUserDeptId, roles);

        List<CustomerInfoItemDO> list = customerInfoItemManager.list(wrapper);
        return list.stream()
                .map(item -> {
                    RiskCustomerItemDTO dto = new RiskCustomerItemDTO();
                    dto.setId(item.getId());
                    dto.setName(item.getName());
                    dto.setIdCard(item.getIdCardNo());
                    dto.setPhone(item.getMobile());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public RiskControlReportDO getReport(String name, String idCard, String phone, Long optUserId, Long optUserDeptId,
            Set<UserAuthRoleEnum> roles) {
        if (!panoramaClient.isEnabled()){
            throw new BizException(ResultCode.RISK_REPORT_SEARCH_UNABLED,"[OUT] phone: "+phone);
        }
        verifyCustomerAccess(name, idCard, phone, optUserId, optUserDeptId, roles);
        Date thirtyDaysAgo = Date.from(Instant.now().minus(30, ChronoUnit.DAYS));

        LambdaQueryWrapper<RiskControlReportDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RiskControlReportDO::getName, name)
                .eq(RiskControlReportDO::getIdCard, idCard)
                .ge(RiskControlReportDO::getQueryTime, thirtyDaysAgo)
                .orderByDesc(RiskControlReportDO::getQueryTime)
                .last("limit 1");

        List<RiskControlReportDO> cachedList = riskControlReportService.list(wrapper);
        if (!cachedList.isEmpty()) {
            RiskControlReportDO cached = cachedList.get(0);
            log.info("Found cached risk report for {}", name);
            return cached;
        }

        log.info("Fetching new risk report for {}", name);
        try {
            String reportJson = panoramaClient.fetchReport(name, idCard, phone);
            RiskControlReportDO report = new RiskControlReportDO()
                    .setName(name)
                    .setIdCard(idCard)
                    .setPhone(phone)
                    .setReportJson(reportJson)
                    .setQueryTime(new Date());
            if (Objects.isNull(report.getGmtCreate())) {
                report.setGmtCreate(new Date());
            }
            if (Objects.isNull(report.getGmtModified())) {
                report.setGmtModified(new Date());
            }
            riskControlReportService.save(report);
            return report;
        } catch (Exception e) {
            throw new RuntimeException("获取风控报告失败: " + e.getMessage(), e);
        }
    }

    private void verifyCustomerAccess(String name, String idCard, String phone, Long optUserId, Long optUserDeptId,
            Set<UserAuthRoleEnum> roles) {
        LambdaQueryWrapper<CustomerInfoItemDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerInfoItemDO::getIdCardNo, idCard)
                .eq(CustomerInfoItemDO::getMobile, phone);
        applyPermission(wrapper, optUserId, optUserDeptId, roles);
        boolean allowed = customerInfoItemManager.count(wrapper) > 0;
        VerifyUtils.judge(allowed, ResultCode.RISK_REPORT_NO_AUTH, true,
                "name=" + name + ", idCard=" + idCard + ", phone=" + phone);
    }

    private void applyPermission(LambdaQueryWrapper<CustomerInfoItemDO> wrapper, Long optUserId, Long optUserDeptId,
            Set<UserAuthRoleEnum> roles) {
        roles = OtherUtils.defaultIfNull(roles, Set.of());
        if (roles.contains(UserAuthRoleEnum.ROLE_SUPPER)) {
            return;
        }
        Long userId = OtherUtils.defaultIfNull(optUserId, -1L);
        Long deptId = OtherUtils.defaultIfNull(optUserDeptId, BizConstance.NONE_DEPT_ID);
        if (roles.contains(UserAuthRoleEnum.ROLE_DEPT_DATA_ADMIN)) {
            Set<Long> allowedDeptIds = getAllowedDeptIds(deptId, roles);
            if (CollectionUtils.isEmpty(allowedDeptIds)) {
                wrapper.eq(CustomerInfoItemDO::getOwnerDeptId, deptId);
                return;
            }
            wrapper.in(CustomerInfoItemDO::getOwnerDeptId, allowedDeptIds);
            return;
        }
        wrapper.eq(CustomerInfoItemDO::getOwnerUserId, userId);
    }

    private Set<Long> getAllowedDeptIds(Long userDeptId, Set<UserAuthRoleEnum> roles) {
        if (roles.contains(UserAuthRoleEnum.ROLE_SUPPER)) {
            return Set.of();
        }
        if (roles.contains(UserAuthRoleEnum.ROLE_DEPT_DATA_ADMIN)) {
            List<Long> list = deptService.getAllChildrenIdByParentDeptId(userDeptId, true);
            return CollectionUtils.isEmpty(list) ? Set.of() : new HashSet<>(list);
        }
        return Set.of(Objects.isNull(userDeptId) ? BizConstance.NONE_DEPT_ID : userDeptId);
    }
}
