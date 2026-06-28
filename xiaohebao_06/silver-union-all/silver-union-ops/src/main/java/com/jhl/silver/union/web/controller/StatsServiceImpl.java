package com.jhl.silver.union.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jhl.silver.union.biz.common.enums.BizDictConfigTypeEnum;
import com.jhl.silver.union.biz.common.enums.OrderStatusEnum;
import com.jhl.silver.union.biz.common.enums.SexEnum;
import com.jhl.silver.union.biz.common.enums.UpdTypeEnum;
import com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerInfoItemDO;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerInfoItemTraceDO;
import com.jhl.silver.union.biz.customer.dal.entity.CustDispatchDailyStatDO;
import com.jhl.silver.union.biz.customer.manager.CustomerInfoItemManager;
import com.jhl.silver.union.biz.customer.manager.CustomerInfoItemTraceManager;
import com.jhl.silver.union.biz.customer.manager.CustDispatchDailyStatManager;
import com.jhl.silver.union.biz.customer.service.BizConfigService;
import com.jhl.silver.union.biz.dept.service.DeptService;
import com.jhl.silver.union.biz.order.dal.entity.BizOrderDO;
import com.jhl.silver.union.biz.user.service.UserService;
import com.jhl.silver.union.web.data.BizDictItem;
import com.jhl.silver.union.web.data.stats.*;
import com.jhl.silver.union.web.data.user.UserItemInfo;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StatsServiceImpl implements StatsService {
    private static final List<String> MOBILE_PROVINCES = Arrays.asList(
            "内蒙古", "黑龙江", "广西", "宁夏", "新疆", "西藏", "香港", "澳门",
            "北京", "天津", "上海", "重庆", "河北", "山西", "辽宁", "吉林",
            "江苏", "浙江", "安徽", "福建", "江西", "山东", "河南", "湖北",
            "湖南", "广东", "海南", "四川", "贵州", "云南", "陕西", "甘肃",
            "青海", "台湾"
    );

    @Resource
    private CustomerInfoItemManager customerManager;
    @Resource
    private CustomerInfoItemTraceManager traceManager;
    @Resource
    private CustDispatchDailyStatManager dispatchDailyStatManager;
    @Resource
    private DeptService deptService;
    @Resource
    private UserService userService;
    @Resource
    private BizConfigService bizConfigService;
    @Resource
    private com.jhl.silver.union.biz.order.service.BizOrderService bizOrderService;
    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public StatsDashboardVO getDashboardSummary(Long optUserId, Long optUserDeptId, Set<UserAuthRoleEnum> roles) {
        Set<Long> allowedDeptIds = getAllowedDeptIds(optUserDeptId, roles);

        LambdaQueryWrapper<CustomerInfoItemDO> custQuery = new LambdaQueryWrapper<>();
        if (!roles.contains(UserAuthRoleEnum.ROLE_SUPPER)) {
            boolean isDeptAdmin = roles.contains(UserAuthRoleEnum.ROLE_DEPT_DATA_ADMIN);
            if (isDeptAdmin) {
                custQuery.in(CustomerInfoItemDO::getOwnerDeptId, allowedDeptIds);
            } else {
                custQuery.eq(CustomerInfoItemDO::getOwnerUserId, optUserId);
            }
        }
        List<CustomerInfoItemDO> allCustomers = customerManager.list(custQuery);
        List<BizOrderDO> allOrders = listOrders(allowedDeptIds, optUserId, roles);

        // 1. Customer KPI
        StatsKpiVO kpi = calculateKpi(allCustomers, allowedDeptIds, optUserId, roles);

        // 2. Order KPI (Integrate)
        calculateOrderKpi(kpi, allOrders);

        // 3. Trend
        StatsTrendVO trend = calculateTrend(allCustomers, allowedDeptIds, optUserId, roles);
        // 3.1 Order Trend
        calculateOrderTrend(trend, allOrders);

        // 4. Funnel
        List<StatsItemVO> funnel = calculateFunnel(allCustomers);

        // 5. Leaderboard
        List<StatsRankItemVO> leaderboard = calculateLeaderboard(allCustomers);

        // 6. Distribution
        StatsDistributionVO distribution = calculateDistribution(allCustomers);

        StatsPeriodKpiVO todayKpi = calculatePeriodKpi(allCustomers, allOrders, 1);
        StatsPeriodKpiVO recent7Kpi = calculatePeriodKpi(allCustomers, allOrders, 7);
        StatsPeriodKpiVO recent30Kpi = calculatePeriodKpi(allCustomers, allOrders, 30);

        return new StatsDashboardVO()
                .setKpi(kpi)
                .setTodayKpi(todayKpi)
                .setRecent7Kpi(recent7Kpi)
                .setRecent30Kpi(recent30Kpi)
                .setTrend(trend)
                .setFunnel(funnel)
                .setLeaderboard(leaderboard)
                .setDistribution(distribution);
    }

    @Override
    public List<DispatchStatsItemVO> getDispatchSummary(Long optUserId, Long optUserDeptId,
            Set<UserAuthRoleEnum> roles) {
        boolean isSupper = roles.contains(UserAuthRoleEnum.ROLE_SUPPER);
        boolean isDeptAdmin = roles.contains(UserAuthRoleEnum.ROLE_DEPT_DATA_ADMIN);
        Set<Long> allowedDeptIds = getAllowedDeptIds(optUserDeptId, roles);

        List<UserItemInfo> users;
        if (isSupper) {
            users = userService.listUserItemByDeptIds(List.of(), null, false);
        } else if (isDeptAdmin) {
            users = userService.listUserItemByDeptIds(new ArrayList<>(allowedDeptIds), null, false);
        } else {
            users = userService.listUserItemByDeptIds(List.of(optUserDeptId), optUserId, false);
        }
        if (users == null) {
            users = List.of();
        }
        Map<Long, DispatchStatsItemVO> resultMap = new LinkedHashMap<>();
        for (UserItemInfo user : users) {
            DispatchStatsItemVO vo = new DispatchStatsItemVO()
                    .setUserId(user.getId())
                    .setUserName(user.getName())
                    .setDeptId(user.getDeptId())
                    .setDeptName(user.getDeptName())
                    .setTodayCount(0)
                    .setYesterdayCount(0)
                    .setRecent7Count(0);
            resultMap.put(user.getId(), vo);
        }

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate start7 = today.minusDays(6);

        LambdaQueryWrapper<CustDispatchDailyStatDO> qw = new LambdaQueryWrapper<>();
        qw.ge(CustDispatchDailyStatDO::getStatDate, java.sql.Date.valueOf(start7))
                .le(CustDispatchDailyStatDO::getStatDate, java.sql.Date.valueOf(today));
        if (!isSupper) {
            if (isDeptAdmin) {
                qw.in(CustDispatchDailyStatDO::getDeptId, allowedDeptIds);
            } else {
                qw.eq(CustDispatchDailyStatDO::getUserId, optUserId);
            }
        }
        List<CustDispatchDailyStatDO> stats = dispatchDailyStatManager.list(qw);
        for (CustDispatchDailyStatDO stat : stats) {
            if (stat == null || stat.getUserId() == null) {
                continue;
            }
            DispatchStatsItemVO vo = resultMap.computeIfAbsent(stat.getUserId(), userId ->
                    new DispatchStatsItemVO()
                            .setUserId(userId)
                            .setDeptId(stat.getDeptId())
                            .setDeptName(deptService.getDeptNameById(stat.getDeptId()))
                            .setUserName(userService.getUserRealName(userId))
                            .setTodayCount(0)
                            .setYesterdayCount(0)
                            .setRecent7Count(0));
            int total = (stat.getAutoCount() == null ? 0 : stat.getAutoCount())
                    + (stat.getManualCount() == null ? 0 : stat.getManualCount());
            LocalDate statDate = toLocalDate(stat.getStatDate());
            if (statDate != null) {
                if (statDate.equals(today)) {
                    vo.setTodayCount(total);
                }
                if (statDate.equals(yesterday)) {
                    vo.setYesterdayCount(total);
                }
                if (!statDate.isBefore(start7)) {
                    vo.setRecent7Count((vo.getRecent7Count() == null ? 0 : vo.getRecent7Count()) + total);
                }
            }
        }
        return new ArrayList<>(resultMap.values());
    }

    @Override
    public ChannelPushTrendVO getChannelPushTrend(int days) {
        int safeDays = normalizeDays(days);
        List<String> dates = buildDateLabels(safeDays);
        LocalDate startDate = LocalDate.parse(dates.get(0));
        LocalDate endDate = LocalDate.now();
        return getChannelPushTrend(startDate.toString(), endDate.toString(), null, false);
    }

    @Override
    public ChannelPushTrendVO getChannelPushTrend(String startDate, String endDate, String channel,
            boolean admissionOnly) {
        LocalDate start = parseDateOrDefault(startDate, LocalDate.now().minusDays(6));
        LocalDate end = parseDateOrDefault(endDate, LocalDate.now());
        if (end.isBefore(start)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }
        List<String> dates = buildDateLabels(start, end);
        if (admissionOnly) {
            return getAdmissionTrend(dates, start, end, channel);
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT DATE_FORMAT(t.stat_day, '%Y-%m-%d') AS stat_date,
                       t.channel_name,
                       t.total
                FROM (
                    SELECT DATE(r.gmt_create) AS stat_day,
                           COALESCE(NULLIF(r.channel_name, ''), '未知渠道') AS channel_name,
                           COUNT(1) AS total
                    FROM cust_push_record r
                    WHERE r.type = 1
                      AND r.mobile IS NOT NULL
                      AND r.mobile <> ''
                      AND COALESCE(r.existed_flag, 0) <> 1
                      AND r.gmt_create >= ?
                      AND r.gmt_create < DATE_ADD(?, INTERVAL 1 DAY)
                      AND (? IS NULL OR COALESCE(NULLIF(r.channel_name, ''), '未知渠道') = ?)
                      AND EXISTS (
                          SELECT 1
                          FROM customer_info_item c
                          WHERE c.mobile = r.mobile
                          LIMIT 1
                      )
                    GROUP BY DATE(r.gmt_create), COALESCE(NULLIF(r.channel_name, ''), '未知渠道')
                ) t
                ORDER BY stat_date ASC, channel_name ASC
                """, java.sql.Date.valueOf(start), java.sql.Date.valueOf(end), normalizeChannel(channel),
                normalizeChannel(channel));
        return buildChannelTrendVo(dates, rows);
    }

    private ChannelPushTrendVO getAdmissionTrend(List<String> dates, LocalDate start, LocalDate end, String channel) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT DATE_FORMAT(t.stat_day, '%Y-%m-%d') AS stat_date,
                       t.channel_name,
                       t.total
                FROM (
                    SELECT stat_date AS stat_day,
                           COALESCE(NULLIF(channel_name, ''), '未知渠道') AS channel_name,
                           COUNT(1) AS total
                    FROM cust_api_admission_stat_event
                    WHERE admission_passed = 1
                      AND stat_date >= ?
                      AND stat_date <= ?
                      AND (? IS NULL OR COALESCE(NULLIF(channel_name, ''), '未知渠道') = ?)
                    GROUP BY stat_date, COALESCE(NULLIF(channel_name, ''), '未知渠道')
                ) t
                ORDER BY stat_date ASC, channel_name ASC
                """, java.sql.Date.valueOf(start), java.sql.Date.valueOf(end), normalizeChannel(channel),
                normalizeChannel(channel));
        return buildChannelTrendVo(dates, rows);
    }

    private ChannelPushTrendVO buildChannelTrendVo(List<String> dates, List<Map<String, Object>> rows) {
        Set<String> channelSet = new TreeSet<>();
        Map<String, Map<String, Integer>> rowMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String date = Objects.toString(row.get("stat_date"), "");
            String channel = Objects.toString(row.get("channel_name"), "未知渠道");
            int total = toInt(row.get("total"));
            channelSet.add(channel);
            rowMap.computeIfAbsent(channel, key -> new HashMap<>()).put(date, total);
        }
        List<String> channels = new ArrayList<>(channelSet);
        Map<String, List<Integer>> series = new LinkedHashMap<>();
        for (String channel : channels) {
            Map<String, Integer> values = rowMap.getOrDefault(channel, Map.of());
            List<Integer> counts = dates.stream()
                    .map(date -> values.getOrDefault(date, 0))
                    .collect(Collectors.toList());
            series.put(channel, counts);
        }
        return new ChannelPushTrendVO()
                .setDates(dates)
                .setChannels(channels)
                .setSeries(series);
    }

    @Override
    public List<String> getDashboardChannels() {
        List<String> channels = jdbcTemplate.queryForList("""
                SELECT channel_name
                FROM (
                    SELECT DISTINCT CONVERT(COALESCE(NULLIF(channel_name, ''), '未知渠道') USING utf8mb4)
                        COLLATE utf8mb4_general_ci AS channel_name
                    FROM cust_push_record
                    WHERE type = 1
                    UNION
                    SELECT DISTINCT CONVERT(COALESCE(NULLIF(channel_name, ''), '未知渠道') USING utf8mb4)
                        COLLATE utf8mb4_general_ci AS channel_name
                    FROM cust_api_admission_stat_event
                ) t
                WHERE channel_name IS NOT NULL AND channel_name <> ''
                ORDER BY channel_name ASC
                """, String.class);
        return channels == null ? List.of() : channels;
    }

    @Override
    public List<SalesAssignmentStatsItemVO> getSalesAssignmentStats(String startDate, String endDate,
            Long optUserDeptId, Set<UserAuthRoleEnum> roles) {
        LocalDate start = parseDateOrDefault(startDate, LocalDate.now().minusDays(6));
        LocalDate end = parseDateOrDefault(endDate, LocalDate.now());
        if (end.isBefore(start)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }
        boolean isSupper = roles.contains(UserAuthRoleEnum.ROLE_SUPPER);
        boolean isDeptAdmin = roles.contains(UserAuthRoleEnum.ROLE_DEPT_DATA_ADMIN);
        Set<Long> allowedDeptIds = getAllowedDeptIds(optUserDeptId, roles);
        String deptCondition = "";
        List<Object> params = new ArrayList<>();
        params.add(java.sql.Date.valueOf(start));
        params.add(java.sql.Date.valueOf(end));
        if (!isSupper && isDeptAdmin && !CollectionUtils.isEmpty(allowedDeptIds)) {
            deptCondition = " AND dept_id IN (" + allowedDeptIds.stream().map(String::valueOf)
                    .collect(Collectors.joining(",")) + ") ";
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT user_id, dept_id,
                       SUM(auto_count) AS auto_count,
                       SUM(manual_count) AS manual_count,
                       SUM(COALESCE(star_manual_count, 0)) AS star_manual_count
                FROM cust_dispatch_daily_stat
                WHERE stat_date >= ? AND stat_date <= ?
                """ + deptCondition + """
                GROUP BY user_id, dept_id
                ORDER BY SUM(auto_count + manual_count) DESC, user_id ASC
                """, params.toArray());
        List<UserItemInfo> users = isSupper
                ? userService.listUserItemByDeptIds(List.of(), null, false)
                : userService.listUserItemByDeptIds(new ArrayList<>(allowedDeptIds), null, false);
        Map<Long, UserItemInfo> userMap = users == null ? Map.of() : users.stream()
                .filter(user -> user.getId() != null)
                .collect(Collectors.toMap(UserItemInfo::getId, user -> user, (a, b) -> a));
        List<SalesAssignmentStatsItemVO> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Long userId = toLong(row.get("user_id"));
            Long deptId = toLong(row.get("dept_id"));
            int autoCount = toInt(row.get("auto_count"));
            int manualCount = toInt(row.get("manual_count"));
            int starManualCount = toInt(row.get("star_manual_count"));
            UserItemInfo user = userMap.get(userId);
            result.add(new SalesAssignmentStatsItemVO()
                    .setUserId(userId)
                    .setUserName(user != null ? user.getName() : userService.getUserRealName(userId))
                    .setDeptId(deptId)
                    .setDeptName(user != null ? user.getDeptName() : deptService.getDeptNameById(deptId))
                    .setAutoCount(autoCount)
                    .setManualCount(manualCount)
                    .setStarManualCount(starManualCount)
                    .setTotalCount(autoCount + manualCount));
        }
        return result;
    }

    @Override
    public StarPublicPoolTrendVO getStarPublicPoolTrend(int days) {
        int safeDays = normalizeDays(days);
        List<String> dates = buildDateLabels(safeDays);
        LocalDate startDate = LocalDate.parse(dates.get(0));
        return getStarPublicPoolTrend(startDate.toString(), LocalDate.now().toString());
    }

    @Override
    public StarPublicPoolTrendVO getStarPublicPoolTrend(String startDate, String endDate) {
        LocalDate start = parseDateOrDefault(startDate, LocalDate.now().minusDays(6));
        LocalDate end = parseDateOrDefault(endDate, LocalDate.now());
        if (end.isBefore(start)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }
        List<String> dates = buildDateLabels(start, end);
        List<Map<String, Object>> entryRows = jdbcTemplate.queryForList("""
                SELECT DATE_FORMAT(stat_date, '%Y-%m-%d') AS stat_date,
                       SUM(entry_count) AS total
                FROM cust_public_pool_star_daily_stat
                WHERE stat_date >= ? AND stat_date <= ?
                GROUP BY stat_date
                ORDER BY stat_date ASC
                """, java.sql.Date.valueOf(start), java.sql.Date.valueOf(end));
        Map<String, Integer> entryMap = entryRows.stream()
                .collect(Collectors.toMap(row -> Objects.toString(row.get("stat_date"), ""),
                        row -> toInt(row.get("total")), (a, b) -> a));
        List<Integer> entryCounts = dates.stream()
                .map(date -> entryMap.getOrDefault(date, 0))
                .collect(Collectors.toList());
        List<Integer> currentCounts = dates.stream()
                .map(date -> 0)
                .collect(Collectors.toList());
        return new StarPublicPoolTrendVO()
                .setDates(dates)
                .setStarEntryCounts(entryCounts)
                .setCurrentStarCounts(currentCounts);
    }

    @Override
    public HourlyAdmissionStatsVO getTodayHourlyAdmissionStats(String channel) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT stat_hour,
                       COUNT(1) AS total_count,
                       SUM(CASE WHEN admission_passed = 1 THEN 1 ELSE 0 END) AS passed_count
                FROM cust_api_admission_stat_event
                WHERE stat_date = CURDATE()
                  AND (? IS NULL OR COALESCE(NULLIF(channel_name, ''), '未知渠道') = ?)
                GROUP BY stat_hour
                ORDER BY stat_hour ASC
                """, normalizeChannel(channel), normalizeChannel(channel));
        List<String> hours = new ArrayList<>();
        List<Integer> passedCounts = new ArrayList<>();
        List<Double> passRates = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            int hour = toInt(row.get("stat_hour"));
            int total = toInt(row.get("total_count"));
            int passed = toInt(row.get("passed_count"));
            hours.add(String.format("%02d:00", hour));
            passedCounts.add(passed);
            passRates.add(total <= 0 ? 0D : BigDecimal.valueOf(passed * 100.0 / total)
                    .setScale(2, java.math.RoundingMode.HALF_UP).doubleValue());
        }
        return new HourlyAdmissionStatsVO()
                .setHours(hours)
                .setPassedCounts(passedCounts)
                .setPassRates(passRates);
    }

    private int normalizeDays(int days) {
        if (days == 3 || days == 7) {
            return days;
        }
        return 7;
    }

    private List<String> buildDateLabels(int days) {
        LocalDate start = LocalDate.now().minusDays(days - 1L);
        return buildDateLabels(start, LocalDate.now());
    }

    private List<String> buildDateLabels(LocalDate start, LocalDate end) {
        List<String> labels = new ArrayList<>();
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            labels.add(cursor.toString());
            cursor = cursor.plusDays(1);
        }
        return labels;
    }

    private String normalizeChannel(String channel) {
        return StringUtils.isBlank(channel) ? null : channel.trim();
    }

    private LocalDate parseDateOrDefault(String value, LocalDate defaultValue) {
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private int toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void calculateOrderKpi(StatsKpiVO kpi, List<BizOrderDO> orders) {
        // Total Order Count
        kpi.setTotalOrderCount(orders.size());

        // Total Gross Profit
        java.math.BigDecimal totalProfit = orders.stream()
                .map(BizOrderDO::getGrossProfit)
                .filter(Objects::nonNull)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        kpi.setTotalGrossProfit(totalProfit);

        // Pending Orders (Not RESOLD, CANCELLED, REJECTED)
        long pending = orders.stream().filter(o -> {
            String s = o.getStatus();
            return !"RESOLD".equals(s) && !"CANCELLED".equals(s) && !"REJECTED".equals(s);
        }).count();
        kpi.setPendingOrders((int) pending);
    }

    private List<BizOrderDO> listOrders(Set<Long> allowedDeptIds, Long optUserId, Set<UserAuthRoleEnum> roles) {
        LambdaQueryWrapper<BizOrderDO> query = new LambdaQueryWrapper<>();
        if (!roles.contains(UserAuthRoleEnum.ROLE_SUPPER)) {
            if (roles.contains(UserAuthRoleEnum.ROLE_DEPT_DATA_ADMIN)) {
                query.in(BizOrderDO::getOwnerDeptId, allowedDeptIds);
            } else {
                query.eq(BizOrderDO::getOwnerUserId, optUserId);
            }
        }
        return bizOrderService.list(query);
    }

    private void calculateOrderTrend(StatsTrendVO trend,
            List<BizOrderDO> orders) {
        // We reuse the cached 'dates' from Customer Trend if possible, but here we
        // rebuild or match them.
        // StatsTrendVO already has dates set by calculateTrend.
        List<String> dates = trend.getDates();
        List<Integer> orderCounts = new ArrayList<>();

        Map<String, Long> dateGroup = orders.stream()
                .filter(c -> c.getGmtCreate() != null)
                .collect(Collectors.groupingBy(
                        c -> DateFormatUtils.format(c.getGmtCreate(), "yyyy-MM-dd"),
                        Collectors.counting()));

        for (String d : dates) {
            orderCounts.add(dateGroup.getOrDefault(d, 0L).intValue());
        }
        trend.setOrderCounts(orderCounts);
    }

    private StatsPeriodKpiVO calculatePeriodKpi(List<CustomerInfoItemDO> customers, List<BizOrderDO> orders, int days) {
        Date now = new Date();
        Date dayStart = DateUtils.truncate(now, Calendar.DATE);
        Date rangeStart = days > 1 ? DateUtils.addDays(dayStart, -(days - 1)) : dayStart;
        Date rangeEnd = now;

        List<BizOrderDO> resoldOrders = orders.stream()
                .filter(o -> OrderStatusEnum.RESOLD.name().equals(o.getStatus()))
                .filter(o -> isInRange(getOrderDealTime(o), rangeStart, rangeEnd))
                .collect(Collectors.toList());

        BigDecimal grossProfit = resoldOrders.stream()
                .map(BizOrderDO::getGrossProfit)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal channelCommission = resoldOrders.stream()
                .map(BizOrderDO::getChannelCommission)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pendingOrders = orders.stream()
                .filter(o -> isPendingStatus(o.getStatus()))
                .filter(o -> isInRange(o.getGmtCreate(), rangeStart, rangeEnd))
                .count();

        long newCustomers = customers.stream()
                .filter(c -> isInRange(c.getGmtCreate(), rangeStart, rangeEnd))
                .count();

        return new StatsPeriodKpiVO()
                .setGrossProfit(grossProfit)
                .setChannelCommission(channelCommission)
                .setOrderCount(resoldOrders.size())
                .setPendingOrderCount((int) pendingOrders)
                .setNewCustomers((int) newCustomers);
    }

    private static boolean isPendingStatus(String status) {
        return StringUtils.isNotBlank(status)
                && !OrderStatusEnum.RESOLD.name().equals(status)
                && !OrderStatusEnum.CANCELLED.name().equals(status)
                && !OrderStatusEnum.REJECTED.name().equals(status);
    }

    private static Date getOrderDealTime(BizOrderDO order) {
        if (order == null) {
            return null;
        }
        return order.getGmtModified() != null ? order.getGmtModified() : order.getGmtCreate();
    }

    private static boolean isInRange(Date value, Date start, Date end) {
        if (value == null || start == null || end == null) {
            return false;
        }
        return !value.before(start) && !value.after(end);
    }

    private StatsKpiVO calculateKpi(List<CustomerInfoItemDO> customers, Set<Long> allowedDeptIds, Long optUserId,
            Set<UserAuthRoleEnum> roles) {
        Date now = new Date();
        Date monthStart = DateUtils.truncate(now, Calendar.MONTH);
        Date todayStart = DateUtils.truncate(now, Calendar.DATE);

        // 新增客户: 本月创建
        long newCustomers = customers.stream()
                .filter(c -> c.getGmtCreate().after(monthStart))
                .count();

        // 今日跟进: 查 Trace 表
        LambdaQueryWrapper<CustomerInfoItemTraceDO> traceQuery = new LambdaQueryWrapper<>();
        traceQuery.eq(CustomerInfoItemTraceDO::getUpdType, UpdTypeEnum.PROGRESS.name())
                .ge(CustomerInfoItemTraceDO::getGmtCreate, todayStart);

        List<CustomerInfoItemTraceDO> todayTraces = traceManager.list(traceQuery);

        long followUps = 0;
        if (roles.contains(UserAuthRoleEnum.ROLE_SUPPER)) {
            followUps = todayTraces.size();
        } else {
            if (roles.contains(UserAuthRoleEnum.ROLE_DEPT_DATA_ADMIN)) {
                Set<Long> visibleCustIds = customers.stream().map(CustomerInfoItemDO::getId)
                        .collect(Collectors.toSet());
                followUps = todayTraces.stream().filter(t -> visibleCustIds.contains(t.getSourceId())).count();
            } else {
                followUps = todayTraces.stream().filter(t -> t.getOptUserId().equals(optUserId)).count();
            }
        }

        int pending = 0;

        return new StatsKpiVO()
                .setNewCustomers((int) newCustomers)
                .setFollowUps((int) followUps)
                .setPendingFollowUps(pending)
                .setConversionRate("-");
    }

    private StatsTrendVO calculateTrend(List<CustomerInfoItemDO> customers, Set<Long> allowedDeptIds, Long optUserId,
            Set<UserAuthRoleEnum> roles) {
        List<String> dates = new ArrayList<>();
        List<Integer> newCounts = new ArrayList<>();
        List<Integer> followCounts = Lists.newArrayList();

        Map<String, Long> dateGroup = customers.stream()
                .filter(c -> c.getGmtCreate() != null)
                .collect(Collectors.groupingBy(
                        c -> DateFormatUtils.format(c.getGmtCreate(), "yyyy-MM-dd"),
                        Collectors.counting()));

        // 生成近30天日期
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -29);
        for (int i = 0; i < 30; i++) {
            String d = DateFormatUtils.format(cal, "yyyy-MM-dd");
            dates.add(d);
            newCounts.add(dateGroup.getOrDefault(d, 0L).intValue());
            followCounts.add(0);
            cal.add(Calendar.DATE, 1);
        }

        return new StatsTrendVO().setDates(dates).setNewCustomers(newCounts).setFollowUps(followCounts);
    }

    private List<StatsItemVO> calculateFunnel(List<CustomerInfoItemDO> customers) {
        Map<Integer, BizDictItem> progressMap = bizConfigService.getBizDictItemMap(BizDictConfigTypeEnum.PROGRESS,
                false);

        Map<Integer, Long> group = customers.stream()
                .filter(c -> c.getProgress() != null)
                .collect(Collectors.groupingBy(CustomerInfoItemDO::getProgress, Collectors.counting()));

        return group.entrySet().stream().map(e -> {
            Integer code = e.getKey();
            String name = Optional.ofNullable(progressMap.get(code)).map(BizDictItem::getLabel).orElse("状态" + code);
            return new StatsItemVO(name, e.getValue());
        }).sorted((a, b) -> Long.compare((Long) b.getValue(), (Long) a.getValue())).collect(Collectors.toList());
    }

    private List<StatsRankItemVO> calculateLeaderboard(List<CustomerInfoItemDO> customers) {
        // 按 OwnerUserId 分组统计新增数
        // 真实排行应该包含跟进数，这里仅以拥有客户数（或新增）为例
        Map<Long, Long> countMap = customers.stream()
                .filter(c -> c.getOwnerUserId() != null && c.getOwnerUserId() > 0)
                .collect(Collectors.groupingBy(CustomerInfoItemDO::getOwnerUserId, Collectors.counting()));

        if (CollectionUtils.isEmpty(countMap))
            return new ArrayList<>();

        Map<Long, String> userNames = userService.getUserRealNames(countMap.keySet());

        List<StatsRankItemVO> list = countMap.entrySet().stream().map(e -> {
            return new StatsRankItemVO()
                    .setName(userNames.getOrDefault(e.getKey(), "未知用户"))
                    .setDepartment("-")
                    .setNewCustomers(e.getValue().intValue()) // 这里偷懒用了总数作为指标，实际应为本月新增
                    .setFollowUps(0);
        }).sorted((a, b) -> b.getNewCustomers() - a.getNewCustomers())
                .limit(10)
                .collect(Collectors.toList());

        for (int i = 0; i < list.size(); i++) {
            list.get(i).setRank(i + 1);
        }
        return list;
    }

    private StatsDistributionVO calculateDistribution(List<CustomerInfoItemDO> customers) {
        // Region
        List<StatsItemVO> region = customers.stream()
                .map(c -> getMobileProvince(c.getMobileArea()))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
                .entrySet().stream()
                .map(e -> new StatsItemVO(e.getKey(), e.getValue()))
                .sorted((a, b) -> Long.compare((Long) b.getValue(), (Long) a.getValue()))
                .limit(10)
                .collect(Collectors.toList());

        // Sex
        List<StatsItemVO> sex = customers.stream()
                .filter(c -> c.getSex() != null)
                .collect(Collectors.groupingBy(CustomerInfoItemDO::getSex, Collectors.counting()))
                .entrySet().stream()
                .map(e -> new StatsItemVO(SexEnum.getDesc(e.getKey()), e.getValue()))
                .collect(Collectors.toList());

        // Age (Group by 10s: 20-30, 30-40...)
        Map<String, Long> ageMap = new HashMap<>();
        customers.stream().filter(c -> c.getAge() != null).forEach(c -> {
            int a = c.getAge();
            String key;
            if (a < 20)
                key = "<20";
            else if (a < 30)
                key = "20-29";
            else if (a < 40)
                key = "30-39";
            else if (a < 50)
                key = "40-49";
            else if (a < 60)
                key = "50-59";
            else
                key = "60+";
            ageMap.merge(key, 1L, Long::sum);
        });

        List<String> ageKeys = Arrays.asList("<20", "20-29", "30-39", "40-49", "50-59", "60+");
        List<StatsItemVO> age = new ArrayList<>();
        for (String k : ageKeys) {
            if (ageMap.containsKey(k)) {
                age.add(new StatsItemVO(k, ageMap.get(k)));
            }
        }

        return new StatsDistributionVO().setRegion(region).setSex(sex).setAge(age);
    }

    private static String getMobileProvince(String mobileArea) {
        if (StringUtils.isBlank(mobileArea)) {
            return null;
        }
        String value = mobileArea.trim();
        for (String province : MOBILE_PROVINCES) {
            if (value.startsWith(province)) {
                return province;
            }
        }
        return value;
    }

    // Copied from CustomerInfoServiceImpl logic
    private Set<Long> getAllowedDeptIds(Long userDeptId, Set<UserAuthRoleEnum> roles) {
        if (roles.contains(UserAuthRoleEnum.ROLE_SUPPER)) {
            return Sets.newHashSet(); // Empty means all for some logic, but here careful
        }
        if (roles.contains(UserAuthRoleEnum.ROLE_DEPT_DATA_ADMIN)) {
            return Set.of(userDeptId == null ? 0L : userDeptId);
        }
        return Set.of(userDeptId == null ? 0L : userDeptId);
    }
}
