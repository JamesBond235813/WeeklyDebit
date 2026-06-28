package com.jhl.silver.union.biz.risk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhl.silver.union.biz.common.ResultCode;
import com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerInfoItemDO;
import com.jhl.silver.union.biz.customer.manager.CustomerInfoItemManager;
import com.jhl.silver.union.biz.risk.dal.entity.RiskControlReportDO;
import com.jhl.silver.union.biz.risk.integration.PanoramaClient;
import com.jhl.silver.union.biz.risk.service.RiskControlReportService;
import com.jhl.silver.union.biz.risk.service.RiskReportService;
import com.jhl.silver.union.commons.exception.BizException;
import com.jhl.silver.union.commons.CommonResultCode;
import com.jhl.silver.union.commons.gson.GsonHelper;
import com.jhl.silver.union.web.data.risk.RiskCustomerItemDTO;
import com.jhl.silver.union.web.data.risk.RiskHistoryPageRequest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskReportServiceImpl implements RiskReportService {

    private final PanoramaClient panoramaClient;
    private final RiskControlReportService riskControlReportService;
    private final CustomerInfoItemManager customerInfoItemManager;

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
        RiskIdentity identity = normalizeIdentity(name, idCard, phone, optUserId, optUserDeptId, roles);
        RiskControlReportDO cached = findRecentCachedReport(identity.name(), identity.idCard(), identity.phone());
        if (isObserver(roles)) {
            if (Objects.nonNull(cached) && hasPanorama(cached.getReportJson()) && !isProbeCMissing(cached.getReportJson())) {
                return cached;
            }
            throw new BizException(ResultCode.RISK_REPORT_NO_AUTH, "观察员暂无缓存风控报告");
        }
        if (!panoramaClient.isEnabled()){
            throw new BizException(ResultCode.RISK_REPORT_SEARCH_UNABLED,"[OUT] phone: "+phone);
        }
        if (Objects.nonNull(cached)) {
            log.info("Found cached risk report for {}", identity.name());
            if (hasPanorama(cached.getReportJson()) && isProbeCMissing(cached.getReportJson())) {
                log.info("Completing cached risk report probe_c for {}", identity.name());
                return completeProbeC(cached, identity.name(), identity.idCard(), identity.phone());
            }
            return cached;
        }

        log.info("Fetching new composite risk report for {}", identity.name());
        try {
            String panoramaJson = panoramaClient.fetchProductReport(identity.name(), identity.idCard(), identity.phone(), PanoramaClient.PANORAMA_PRODUCT_NO);
            String probeCJson = panoramaClient.fetchProductReport(identity.name(), identity.idCard(), identity.phone(), PanoramaClient.PROBE_C_PRODUCT_NO);
            String reportJson = buildCompositeReport(identity.name(), identity.idCard(), identity.phone(), panoramaJson, probeCJson);
            RiskControlReportDO report = new RiskControlReportDO()
                    .setName(identity.name())
                    .setIdCard(identity.idCard())
                    .setPhone(identity.phone())
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
    public RiskControlReportDO getRadarReport(String name, String idCard, String phone, Long optUserId,
            Long optUserDeptId, Set<UserAuthRoleEnum> roles) {
        RiskIdentity identity = normalizeIdentity(name, idCard, phone, optUserId, optUserDeptId, roles);
        RiskControlReportDO cached = findRecentCachedReport(identity.name(), identity.idCard(), identity.phone());
        if (isObserver(roles)) {
            if (Objects.nonNull(cached) && hasPanorama(cached.getReportJson())) {
                return cached;
            }
            throw new BizException(ResultCode.RISK_REPORT_NO_AUTH, "观察员暂无缓存雷达报告");
        }
        if (!panoramaClient.isEnabled()){
            throw new BizException(ResultCode.RISK_REPORT_SEARCH_UNABLED,"[OUT] phone: "+phone);
        }
        if (Objects.nonNull(cached) && hasPanorama(cached.getReportJson())) {
            log.info("Found cached radar report for {}", identity.name());
            return cached;
        }

        log.info("Fetching new radar report for {}", identity.name());
        try {
            String panoramaJson = panoramaClient.fetchProductReport(identity.name(), identity.idCard(), identity.phone(),
                    PanoramaClient.PANORAMA_PRODUCT_NO);
            String reportJson = buildRadarOnlyReport(identity.name(), identity.idCard(), identity.phone(), panoramaJson);
            RiskControlReportDO report = new RiskControlReportDO()
                    .setName(identity.name())
                    .setIdCard(identity.idCard())
                    .setPhone(identity.phone())
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
            throw new RuntimeException("获取全景雷达报告失败: " + e.getMessage(), e);
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

        if (hasName && hasIdCard && hasPhone) {
            CustomerInfoItemDO exactMatched = findUniqueCustomer(qw -> qw.eq(CustomerInfoItemDO::getName, name)
                    .eq(CustomerInfoItemDO::getIdCardNo, idCard)
                    .eq(CustomerInfoItemDO::getMobile, phone), optUserId, optUserDeptId, roles);
            if (Objects.nonNull(exactMatched)) {
                return toRiskCustomerItemDTO(exactMatched);
            }
            CustomerInfoItemDO strongMatched = findUniqueCustomerByStrongIdentity(idCard, phone, optUserId, optUserDeptId,
                    roles);
            if (Objects.nonNull(strongMatched)) {
                return toRiskCustomerItemDTO(strongMatched);
            }
            return null;
        }

        CustomerInfoItemDO strongMatched = findUniqueCustomerByStrongIdentity(idCard, phone, optUserId, optUserDeptId,
                roles);
        if (Objects.nonNull(strongMatched)) {
            return toRiskCustomerItemDTO(strongMatched);
        }

        LambdaQueryWrapper<CustomerInfoItemDO> wrapper = new LambdaQueryWrapper<>();
        if (hasName) {
            wrapper.eq(CustomerInfoItemDO::getName, name);
        }
        if (hasIdCard) {
            wrapper.eq(CustomerInfoItemDO::getIdCardNo, idCard);
        }
        if (hasPhone) {
            wrapper.eq(CustomerInfoItemDO::getMobile, phone);
        }
        List<CustomerInfoItemDO> matched = customerInfoItemManager.list(wrapper);
        if (matched.size() != 1) {
            throw new BizException(CommonResultCode.INVALID_PARAMS,
                    matched.isEmpty() ? "未匹配到唯一客户，请补齐三要素后重试" : "匹配到多个客户，请补齐三要素后重试");
        }
        return toRiskCustomerItemDTO(matched.get(0));
    }

    private void verifyTemporaryRiskIdentity(String name, String idCard, String phone) {
        if (StringUtils.isAnyBlank(name, idCard, phone)) {
            throw new BizException(CommonResultCode.INVALID_PARAMS,
                    "临时查询客户必须填写完整姓名、身份证号、手机号");
        }
    }

    private CustomerInfoItemDO findUniqueCustomerByStrongIdentity(String idCard, String phone, Long optUserId,
            Long optUserDeptId, Set<UserAuthRoleEnum> roles) {
        if (StringUtils.isNotBlank(idCard) && StringUtils.isNotBlank(phone)) {
            CustomerInfoItemDO item = findUniqueCustomer(qw -> qw.eq(CustomerInfoItemDO::getIdCardNo, idCard)
                    .eq(CustomerInfoItemDO::getMobile, phone), optUserId, optUserDeptId, roles);
            if (Objects.nonNull(item)) {
                return item;
            }
        }
        if (StringUtils.isNotBlank(phone)) {
            CustomerInfoItemDO item = findUniqueCustomer(qw -> qw.eq(CustomerInfoItemDO::getMobile, phone), optUserId,
                    optUserDeptId, roles);
            if (Objects.nonNull(item)) {
                return item;
            }
        }
        if (StringUtils.isNotBlank(idCard)) {
            return findUniqueCustomer(qw -> qw.eq(CustomerInfoItemDO::getIdCardNo, idCard), optUserId, optUserDeptId,
                    roles);
        }
        return null;
    }

    private CustomerInfoItemDO findUniqueCustomer(java.util.function.Consumer<LambdaQueryWrapper<CustomerInfoItemDO>> customizer,
            Long optUserId, Long optUserDeptId, Set<UserAuthRoleEnum> roles) {
        LambdaQueryWrapper<CustomerInfoItemDO> wrapper = new LambdaQueryWrapper<>();
        customizer.accept(wrapper);
        wrapper.last("limit 2");
        List<CustomerInfoItemDO> matched = customerInfoItemManager.list(wrapper);
        return matched.size() == 1 ? matched.get(0) : null;
    }

    private RiskCustomerItemDTO toRiskCustomerItemDTO(CustomerInfoItemDO item) {
        RiskCustomerItemDTO dto = new RiskCustomerItemDTO();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setIdCard(item.getIdCardNo());
        dto.setPhone(item.getMobile());
        return dto;
    }

    private RiskIdentity normalizeIdentity(String name, String idCard, String phone, Long optUserId, Long optUserDeptId,
            Set<UserAuthRoleEnum> roles) {
        name = StringUtils.trim(name);
        idCard = StringUtils.trim(idCard);
        phone = StringUtils.trim(phone);
        RiskCustomerItemDTO customer = resolveCustomer(name, idCard, phone, optUserId, optUserDeptId, roles);
        if (Objects.nonNull(customer)) {
            return new RiskIdentity(customer.getName(), customer.getIdCard(), customer.getPhone());
        }
        verifyTemporaryRiskIdentity(name, idCard, phone);
        return new RiskIdentity(name, idCard, phone);
    }

    private RiskControlReportDO findRecentCachedReport(String name, String idCard, String phone) {
        Date thirtyDaysAgo = Date.from(Instant.now().minus(30, ChronoUnit.DAYS));
        LambdaQueryWrapper<RiskControlReportDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RiskControlReportDO::getName, name)
                .eq(RiskControlReportDO::getIdCard, idCard)
                .eq(RiskControlReportDO::getPhone, phone)
                .ge(RiskControlReportDO::getQueryTime, thirtyDaysAgo)
                .orderByDesc(RiskControlReportDO::getQueryTime)
                .last("limit 1");

        List<RiskControlReportDO> cachedList = riskControlReportService.list(wrapper);
        return cachedList.isEmpty() ? null : cachedList.get(0);
    }

    private boolean isObserver(Set<UserAuthRoleEnum> roles) {
        return roles != null && roles.contains(UserAuthRoleEnum.ROLE_OBSERVER);
    }

    private String buildCompositeReport(String name, String idCard, String phone, String panoramaJson,
            String probeCJson) {
        Map<String, Object> composite = buildPanoramaSection(name, idCard, phone, panoramaJson);
        composite.put("probe_c", buildProbeCSection(probeCJson, new Date()));
        return GsonHelper.toJson(composite);
    }

    private String buildRadarOnlyReport(String name, String idCard, String phone, String panoramaJson) {
        Map<String, Object> composite = buildPanoramaSection(name, idCard, phone, panoramaJson);
        composite.put("probe_c", new LinkedHashMap<>());
        return GsonHelper.toJson(composite);
    }

    private Map<String, Object> buildPanoramaSection(String name, String idCard, String phone, String panoramaJson) {
        Map<String, Object> panoramaPayload = parseJson(panoramaJson);

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("name", name);
        profile.put("phone", phone);
        profile.put("id_card", idCard);

        Date now = new Date();
        Map<String, Object> panorama = new LinkedHashMap<>();
        panorama.put("source", "PANORAMA");
        panorama.put("query_time", now);
        panorama.put("payload", panoramaPayload);

        Map<String, Object> composite = new LinkedHashMap<>();
        composite.put("report_type", "RONGSHUHUA_RISK");
        composite.put("title", "榕树花风控报告");
        composite.put("query_time", now);
        composite.put("cache_days", 30);
        composite.put("user_profile", profile);
        composite.put("panorama", panorama);
        return composite;
    }

    private RiskControlReportDO completeProbeC(RiskControlReportDO cached, String name, String idCard, String phone) {
        try {
            String probeCJson = panoramaClient.fetchProductReport(name, idCard, phone, PanoramaClient.PROBE_C_PRODUCT_NO);
            Map<String, Object> composite = parseJson(cached.getReportJson());
            Date now = new Date();
            composite.put("probe_c", buildProbeCSection(probeCJson, now));
            composite.put("query_time", now);
            cached.setReportJson(GsonHelper.toJson(composite));
            cached.setQueryTime(now);
            cached.setGmtModified(now);
            riskControlReportService.updateById(cached);
            return cached;
        } catch (Exception e) {
            throw new RuntimeException("补充探针C报告失败: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> buildProbeCSection(String probeCJson, Date queryTime) {
        Map<String, Object> probeCPayload = parseJson(probeCJson);
        Map<String, Object> probeCData = nestedMap(probeCPayload, "data");
        Map<String, Object> probeC = new LinkedHashMap<>();
        probeC.put("source", "PROBE_C");
        probeC.put("query_time", queryTime);
        probeC.put("result_label", probeResultLabel(probeCData.get("result_code")));
        probeC.put("payload", probeCPayload);
        return probeC;
    }

    @SuppressWarnings("unchecked")
    private boolean hasPanorama(String reportJson) {
        Map<String, Object> composite = parseJson(reportJson);
        Object panorama = composite.get("panorama");
        return panorama instanceof Map && !((Map<String, Object>) panorama).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private boolean isProbeCMissing(String reportJson) {
        Map<String, Object> composite = parseJson(reportJson);
        Object probeC = composite.get("probe_c");
        if (!(probeC instanceof Map<?, ?> probeCMap) || probeCMap.isEmpty()) {
            return true;
        }
        Object payload = ((Map<String, Object>) probeCMap).get("payload");
        return !(payload instanceof Map<?, ?> payloadMap) || payloadMap.isEmpty();
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

    private record RiskIdentity(String name, String idCard, String phone) {
    }

}
