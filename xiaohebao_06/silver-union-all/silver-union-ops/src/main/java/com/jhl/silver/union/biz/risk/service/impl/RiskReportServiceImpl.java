package com.jhl.silver.union.biz.risk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.jhl.silver.union.commons.CommonResultCode;
import com.jhl.silver.union.commons.gson.GsonHelper;
import com.jhl.silver.union.commons.utils.OtherUtils;
import com.jhl.silver.union.commons.utils.VerifyUtils;
import com.jhl.silver.union.web.data.risk.RiskCustomerItemDTO;
import com.jhl.silver.union.web.data.risk.RiskHistoryPageRequest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
        RiskCustomerItemDTO customer = resolveCustomer(name, idCard, phone, optUserId, optUserDeptId, roles);
        name = customer.getName();
        idCard = customer.getIdCard();
        phone = customer.getPhone();
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

        log.info("Fetching new composite risk report for {}", name);
        try {
            String panoramaJson = panoramaClient.fetchProductReport(name, idCard, phone, PanoramaClient.PANORAMA_PRODUCT_NO);
            String probeCJson = panoramaClient.fetchProductReport(name, idCard, phone, PanoramaClient.PROBE_C_PRODUCT_NO);
            String reportJson = buildCompositeReport(name, idCard, phone, panoramaJson, probeCJson);
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

    @Override
    public IPage<RiskControlReportDO> pageHistory(RiskHistoryPageRequest request, Long optUserId, Long optUserDeptId,
            Set<UserAuthRoleEnum> roles) {
        if (request == null) {
            request = new RiskHistoryPageRequest();
        }
        request.autoFix();
        Page<RiskControlReportDO> page = new Page<>(request.getPage(), request.getPageSize());
        LambdaQueryWrapper<RiskControlReportDO> wrapper = new LambdaQueryWrapper<>();
        String keyword = StringUtils.trimToEmpty(request.getKeyword());
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(qw -> qw.like(RiskControlReportDO::getName, keyword)
                    .or()
                    .like(RiskControlReportDO::getPhone, keyword)
                    .or()
                    .like(RiskControlReportDO::getIdCard, keyword));
        }
        wrapper.orderByDesc(RiskControlReportDO::getQueryTime);
        return riskControlReportService.page(page, wrapper);
    }

    private RiskCustomerItemDTO resolveCustomer(String name, String idCard, String phone, Long optUserId,
            Long optUserDeptId, Set<UserAuthRoleEnum> roles) {
        boolean hasName = StringUtils.isNotBlank(name);
        boolean hasIdCard = StringUtils.isNotBlank(idCard);
        boolean hasPhone = StringUtils.isNotBlank(phone);
        if (!hasName && !hasIdCard && !hasPhone) {
            throw new BizException(CommonResultCode.INVALID_PARAMS, "请至少填写姓名、身份证号、手机号中的一项");
        }

        LambdaQueryWrapper<CustomerInfoItemDO> wrapper = new LambdaQueryWrapper<>();
        if (hasName) {
            wrapper.eq(CustomerInfoItemDO::getName, StringUtils.trim(name));
        }
        if (hasIdCard) {
            wrapper.eq(CustomerInfoItemDO::getIdCardNo, StringUtils.trim(idCard));
        }
        if (hasPhone) {
            wrapper.eq(CustomerInfoItemDO::getMobile, StringUtils.trim(phone));
        }
        applyPermission(wrapper, optUserId, optUserDeptId, roles);
        List<CustomerInfoItemDO> matched = customerInfoItemManager.list(wrapper);
        if (matched.size() != 1) {
            throw new BizException(CommonResultCode.INVALID_PARAMS,
                    matched.isEmpty() ? "未匹配到唯一客户，请补齐三要素后重试" : "匹配到多个客户，请补齐三要素后重试");
        }
        CustomerInfoItemDO item = matched.get(0);
        RiskCustomerItemDTO dto = new RiskCustomerItemDTO();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setIdCard(item.getIdCardNo());
        dto.setPhone(item.getMobile());
        return dto;
    }

    private String buildCompositeReport(String name, String idCard, String phone, String panoramaJson,
            String probeCJson) {
        Map<String, Object> panoramaPayload = parseJson(panoramaJson);
        Map<String, Object> probeCPayload = parseJson(probeCJson);
        Map<String, Object> probeCData = nestedMap(probeCPayload, "data");

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("name", name);
        profile.put("phone", phone);
        profile.put("id_card", idCard);

        Map<String, Object> systemRisk = new LinkedHashMap<>();
        systemRisk.put("blacklist_hit", false);
        systemRisk.put("location_risk_hit", false);
        systemRisk.put("login_location_blocked", false);
        systemRisk.put("same_phone_binding_count", 0);

        Map<String, Object> panorama = new LinkedHashMap<>();
        panorama.put("source", "PANORAMA");
        panorama.put("query_time", new Date());
        panorama.put("payload", panoramaPayload);

        Map<String, Object> probeC = new LinkedHashMap<>();
        probeC.put("source", "PROBE_C");
        probeC.put("query_time", new Date());
        probeC.put("result_label", probeResultLabel(probeCData.get("result_code")));
        probeC.put("payload", probeCPayload);

        Map<String, Object> composite = new LinkedHashMap<>();
        composite.put("report_type", "XIAOHEBAO_RISK");
        composite.put("title", "小荷包风险报告");
        composite.put("query_time", new Date());
        composite.put("cache_days", 30);
        composite.put("user_profile", profile);
        composite.put("system_risk", systemRisk);
        composite.put("latest_order", new LinkedHashMap<>());
        composite.put("recent_access", List.of());
        composite.put("panorama", panorama);
        composite.put("probe_c", probeC);
        return GsonHelper.toJson(composite);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json) {
        if (StringUtils.isBlank(json)) {
            return new LinkedHashMap<>();
        }
        try {
            return GsonHelper.fromJson(json, Map.class);
        } catch (Exception e) {
            Map<String, Object> raw = new LinkedHashMap<>();
            raw.put("raw", json);
            return raw;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> nestedMap(Map<String, Object> source, String key) {
        Object value = source.get(key);
        return value instanceof Map ? (Map<String, Object>) value : new LinkedHashMap<>();
    }

    private String probeResultLabel(Object code) {
        String value = Objects.toString(code, "");
        return switch (value) {
            case "1" -> "逾期未还款";
            case "2" -> "正常履约";
            case "3" -> "逾期后已还款";
            case "4" -> "无法确认";
            default -> "未知";
        };
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
