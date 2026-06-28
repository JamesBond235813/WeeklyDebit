package com.jhl.silver.union.biz.risk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhl.silver.union.biz.risk.dal.entity.RiskControlReportDO;
import com.jhl.silver.union.biz.risk.integration.PanoramaClient;
import com.jhl.silver.union.biz.risk.service.RiskAdmissionRuleEvaluator;
import com.jhl.silver.union.biz.risk.service.RiskControlReportService;
import com.jhl.silver.union.biz.risk.service.RiskDispatchAdmissionService;
import com.jhl.silver.union.commons.gson.GsonHelper;
import com.jhl.silver.union.web.data.admin.PushCustInfoItem;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskDispatchAdmissionServiceImpl implements RiskDispatchAdmissionService {

    private final PanoramaClient panoramaClient;
    private final RiskControlReportService riskControlReportService;

    @Override
    public boolean shouldAutoDispatch(PushCustInfoItem item) {
        if (item == null) {
            return false;
        }
        return shouldAutoDispatch(item.getName(), item.getIdCardNo(), item.getMobile(), item.getUpstreamZhimaCode());
    }

    @Override
    public boolean shouldAutoDispatch(String name, String idCard, String phone, Integer upstreamZhimaCode) {
        try {
            if (!RiskAdmissionRuleEvaluator.isZhima600Plus(upstreamZhimaCode)) {
                return false;
            }
            RiskControlReportDO report = getOrFetchPanoramaOnly(name, idCard, phone);
            return RiskAdmissionRuleEvaluator.isPanoramaQualified(report == null ? null : report.getReportJson());
        } catch (Exception e) {
            log.warn("risk dispatch admission failed, name={}, phone={}, err={}",
                    name, phone, e.getMessage());
            return false;
        }
    }

    private RiskControlReportDO getOrFetchPanoramaOnly(String name, String idCard, String phone) throws Exception {
        if (!panoramaClient.isEnabled()) {
            return null;
        }
        name = StringUtils.trimToEmpty(name);
        idCard = StringUtils.trimToEmpty(idCard);
        phone = StringUtils.trimToEmpty(phone);
        if (StringUtils.isAnyBlank(name, idCard, phone)) {
            return null;
        }

        Date thirtyDaysAgo = Date.from(Instant.now().minus(30, ChronoUnit.DAYS));
        LambdaQueryWrapper<RiskControlReportDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RiskControlReportDO::getName, name)
                .eq(RiskControlReportDO::getIdCard, idCard)
                .eq(RiskControlReportDO::getPhone, phone)
                .ge(RiskControlReportDO::getQueryTime, thirtyDaysAgo)
                .orderByDesc(RiskControlReportDO::getQueryTime)
                .last("limit 1");
        List<RiskControlReportDO> cachedList = riskControlReportService.list(wrapper);
        if (!cachedList.isEmpty() && RiskAdmissionRuleEvaluator.hasPanorama(cachedList.get(0).getReportJson())) {
            return cachedList.get(0);
        }

        String panoramaJson = panoramaClient.fetchProductReport(name, idCard, phone,
                PanoramaClient.PANORAMA_PRODUCT_NO);
        Date now = new Date();
        RiskControlReportDO report = new RiskControlReportDO()
                .setName(name)
                .setIdCard(idCard)
                .setPhone(phone)
                .setReportJson(buildPanoramaOnlyReport(name, idCard, phone, panoramaJson, now))
                .setQueryTime(now)
                .setGmtCreate(now)
                .setGmtModified(now);
        riskControlReportService.save(report);
        return report;
    }

    private String buildPanoramaOnlyReport(String name, String idCard, String phone, String panoramaJson, Date now) {
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("name", name);
        profile.put("phone", phone);
        profile.put("id_card", idCard);

        Map<String, Object> panorama = new LinkedHashMap<>();
        panorama.put("source", "PANORAMA");
        panorama.put("query_time", now);
        panorama.put("payload", parseJson(panoramaJson));

        Map<String, Object> composite = new LinkedHashMap<>();
        composite.put("report_type", "RONGSHUHUA_RISK");
        composite.put("title", "榕树花风控报告");
        composite.put("query_time", now);
        composite.put("cache_days", 30);
        composite.put("user_profile", profile);
        composite.put("panorama", panorama);
        composite.put("probe_c", new LinkedHashMap<>());
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
            return new LinkedHashMap<>();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> nestedMap(Map<String, Object> source, String key) {
        Object value = source.get(key);
        return value instanceof Map ? (Map<String, Object>) value : new LinkedHashMap<>();
    }
}
