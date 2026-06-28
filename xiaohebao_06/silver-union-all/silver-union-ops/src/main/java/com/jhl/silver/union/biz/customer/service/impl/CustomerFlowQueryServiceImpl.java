package com.jhl.silver.union.biz.customer.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.gson.reflect.TypeToken;
import com.jhl.silver.union.biz.customer.dal.entity.CustPushRecordDO;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerInfoItemDO;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerInfoItemTraceDO;
import com.jhl.silver.union.biz.customer.manager.CustPushRecordManager;
import com.jhl.silver.union.biz.customer.manager.CustomerInfoItemManager;
import com.jhl.silver.union.biz.customer.manager.CustomerInfoItemTraceManager;
import com.jhl.silver.union.biz.customer.service.CustomerFlowQueryService;
import com.jhl.silver.union.biz.data.FieldDiffItemInfo;
import com.jhl.silver.union.biz.dept.service.DeptService;
import com.jhl.silver.union.biz.user.service.UserService;
import com.jhl.silver.union.commons.CommonResultCode;
import com.jhl.silver.union.commons.exception.BizException;
import com.jhl.silver.union.commons.gson.GsonHelper;
import com.jhl.silver.union.web.data.customer.CustomerFlowQueryRequest;
import com.jhl.silver.union.web.data.customer.CustomerFlowQueryResult;
import com.jhl.silver.union.web.data.customer.CustomerFlowQueryResult.CustomerFlowNode;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CustomerFlowQueryServiceImpl implements CustomerFlowQueryService {

    private static final String PUBLIC_POOL = "公海";
    private static final String SYSTEM_OPERATOR = "系统";

    private final CustomerInfoItemManager customerManager;
    private final CustPushRecordManager pushRecordManager;
    private final CustomerInfoItemTraceManager traceManager;
    private final UserService userService;
    private final DeptService deptService;
    private final JdbcTemplate jdbcTemplate;

    public CustomerFlowQueryServiceImpl(CustomerInfoItemManager customerManager,
            CustPushRecordManager pushRecordManager,
            CustomerInfoItemTraceManager traceManager,
            UserService userService,
            DeptService deptService,
            JdbcTemplate jdbcTemplate) {
        this.customerManager = customerManager;
        this.pushRecordManager = pushRecordManager;
        this.traceManager = traceManager;
        this.userService = userService;
        this.deptService = deptService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public CustomerFlowQueryResult query(CustomerFlowQueryRequest request) {
        if (request == null || (StringUtils.isBlank(request.getName()) && StringUtils.isBlank(request.getMobile()))) {
            throw new BizException(CommonResultCode.INVALID_PARAMS, "请填写客户姓名或手机号");
        }

        CustomerInfoItemDO customer = findCustomer(request);
        if (customer == null) {
            return new CustomerFlowQueryResult()
                    .setFound(false)
                    .setSummary("没有查到匹配的客户数据。");
        }

        List<CustPushRecordDO> pushRecords = findPushRecords(customer, request);
        Map<String, Object> admission = findAdmission(customer);
        List<CustomerInfoItemTraceDO> traces = findTraces(customer.getId());
        Map<Long, String> userNames = loadUserNames(customer, traces);
        Map<Long, String> deptNames = loadDeptNames(customer);

        List<CustomerFlowNode> nodes = new ArrayList<>();
        addInboundNode(nodes, customer, pushRecords, admission);
        addAdmissionNode(nodes, admission, customer);
        addTraceNodes(nodes, traces, userNames);

        nodes.sort(Comparator.comparing(CustomerFlowNode::getTime, Comparator.nullsLast(String::compareTo)));
        String currentOwner = currentOwner(customer, userNames);
        String currentDept = deptNames.getOrDefault(customer.getOwnerDeptId(), blankDefault(customer.getOwnerDeptId(), "--"));
        String currentStatus = currentStatus(customer, currentOwner);

        return new CustomerFlowQueryResult()
                .setFound(true)
                .setCustomerId(customer.getId())
                .setCustomerName(customer.getName())
                .setMobile(customer.getMobile())
                .setCurrentOwner(currentOwner)
                .setCurrentDept(currentDept)
                .setCurrentStatus(currentStatus)
                .setNodes(nodes)
                .setSummary(buildSummary(customer, nodes, currentStatus));
    }

    private CustomerInfoItemDO findCustomer(CustomerFlowQueryRequest request) {
        List<CustomerInfoItemDO> customers = customerManager.list(Wrappers.<CustomerInfoItemDO>lambdaQuery()
                .eq(StringUtils.isNotBlank(request.getMobile()), CustomerInfoItemDO::getMobile, StringUtils.trim(request.getMobile()))
                .eq(StringUtils.isNotBlank(request.getName()), CustomerInfoItemDO::getName, StringUtils.trim(request.getName()))
                .orderByDesc(CustomerInfoItemDO::getGmtCreate)
                .last("limit 1"));
        return customers.isEmpty() ? null : customers.get(0);
    }

    private List<CustPushRecordDO> findPushRecords(CustomerInfoItemDO customer, CustomerFlowQueryRequest request) {
        String mobile = StringUtils.defaultIfBlank(customer.getMobile(), request.getMobile());
        String name = StringUtils.defaultIfBlank(customer.getName(), request.getName());
        return pushRecordManager.list(Wrappers.<CustPushRecordDO>lambdaQuery()
                .eq(StringUtils.isNotBlank(mobile), CustPushRecordDO::getMobile, mobile)
                .eq(StringUtils.isNotBlank(name), CustPushRecordDO::getCustName, name)
                .orderByAsc(CustPushRecordDO::getGmtCreate)
                .last("limit 10"));
    }

    private Map<String, Object> findAdmission(CustomerInfoItemDO customer) {
        String sql = "select admission_passed, dispatch_result, stat_time, channel_name, user_source "
                + "from cust_api_admission_stat_event "
                + "where mobile = ? or cust_id = ? "
                + "order by stat_time desc, gmt_create desc limit 1";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, customer.getMobile(), customer.getId());
        return rows.isEmpty() ? Map.of() : rows.get(0);
    }

    private List<CustomerInfoItemTraceDO> findTraces(Long customerId) {
        return traceManager.list(Wrappers.<CustomerInfoItemTraceDO>lambdaQuery()
                .eq(CustomerInfoItemTraceDO::getSourceId, customerId)
                .orderByAsc(CustomerInfoItemTraceDO::getGmtCreate));
    }

    private Map<Long, String> loadUserNames(CustomerInfoItemDO customer, List<CustomerInfoItemTraceDO> traces) {
        Set<Long> ids = new HashSet<>();
        addPositive(ids, customer.getOwnerUserId());
        for (CustomerInfoItemTraceDO trace : traces) {
            addPositive(ids, trace.getOptUserId());
            for (FieldDiffItemInfo diff : parseDiff(trace)) {
                if (StringUtils.equals(diff.getFieldName(), "ownerUserId")) {
                    addPositive(ids, parseLong(diff.getOldValue()));
                    addPositive(ids, parseLong(diff.getNewValue()));
                }
            }
        }
        return ids.isEmpty() ? Map.of() : userService.getUserRealNames(ids);
    }

    private Map<Long, String> loadDeptNames(CustomerInfoItemDO customer) {
        Set<Long> ids = new HashSet<>();
        addPositive(ids, customer.getOwnerDeptId());
        return ids.isEmpty() ? Map.of() : deptService.getDeptNameByIds(ids);
    }

    private void addInboundNode(List<CustomerFlowNode> nodes, CustomerInfoItemDO customer,
            List<CustPushRecordDO> pushRecords, Map<String, Object> admission) {
        Date time = customer.getApplyDate() != null ? customer.getApplyDate() : customer.getGmtCreate();
        String source = firstNonBlank(
                mapString(admission, "channel_name"),
                pushRecords.isEmpty() ? null : pushRecords.get(0).getChannelName(),
                channelName(customer.getChannel()));
        String userSource = firstNonBlank(mapString(admission, "user_source"), customer.getUserSource());
        nodes.add(new CustomerFlowNode()
                .setTime(format(time))
                .setTitle("上游入库")
                .setOperator(SYSTEM_OPERATOR)
                .setType("INBOUND")
                .setDescription("客户由" + source + "推送入库"
                        + (StringUtils.isNotBlank(userSource) ? "，用户来源为" + userSource : "")
                        + "。"));
    }

    private void addAdmissionNode(List<CustomerFlowNode> nodes, Map<String, Object> admission, CustomerInfoItemDO customer) {
        if (admission.isEmpty()) {
            return;
        }
        boolean passed = Objects.equals(String.valueOf(admission.get("admission_passed")), "1")
                || Objects.equals(admission.get("admission_passed"), 1);
        String result = StringUtils.defaultIfBlank(mapString(admission, "dispatch_result"), "");
        String title = isPublicPoolResult(result) ? "进入公海" : "准入分配";
        String description = (passed ? "符合准入条件" : "未满足准入条件")
                + "，系统处理结果为" + dispatchResultText(result, customer) + "。";
        nodes.add(new CustomerFlowNode()
                .setTime(format(toDate(admission.get("stat_time"))))
                .setTitle(title)
                .setOperator(SYSTEM_OPERATOR)
                .setType(isPublicPoolResult(result) ? "PUBLIC_POOL" : "ADMISSION")
                .setDescription(description));
    }

    private void addTraceNodes(List<CustomerFlowNode> nodes, List<CustomerInfoItemTraceDO> traces, Map<Long, String> userNames) {
        for (CustomerInfoItemTraceDO trace : traces) {
            List<FieldDiffItemInfo> diffs = parseDiff(trace);
            FieldDiffItemInfo ownerDiff = findDiff(diffs, "ownerUserId");
            if (ownerDiff == null) {
                continue;
            }
            Long oldOwner = parseLong(ownerDiff.getOldValue());
            Long newOwner = parseLong(ownerDiff.getNewValue());
            String operator = firstNonBlank(trace.getOptUserRealName(), userNames.get(trace.getOptUserId()), blankDefault(trace.getOptUserId(), SYSTEM_OPERATOR));
            CustomerFlowNode node = new CustomerFlowNode()
                    .setTime(format(trace.getGmtCreate()))
                    .setOperator(operator);
            if (isZero(oldOwner) && !isZero(newOwner)) {
                String ownerName = userNames.getOrDefault(newOwner, blankDefault(newOwner, "业务员"));
                node.setTitle("业务员领取")
                        .setType("CLAIM")
                        .setDescription(ownerName + "从公海领取了这条客户数据"
                                + (starCleared(diffs) ? "，领取后☆标记消失" : "")
                                + "。");
            } else if (!isZero(oldOwner) && isZero(newOwner)) {
                String oldOwnerName = userNames.getOrDefault(oldOwner, blankDefault(oldOwner, "原归属人"));
                node.setTitle("移回公海")
                        .setType("BACK_PUBLIC_POOL")
                        .setDescription(oldOwnerName + "名下的客户数据被移回公海。");
            } else if (!Objects.equals(oldOwner, newOwner)) {
                String ownerName = userNames.getOrDefault(newOwner, blankDefault(newOwner, "业务员"));
                node.setTitle("归属调整")
                        .setType("ASSIGN")
                        .setDescription("客户归属调整为" + ownerName + "。");
            } else {
                continue;
            }
            nodes.add(node);
        }
    }

    private String buildSummary(CustomerInfoItemDO customer, List<CustomerFlowNode> nodes, String currentStatus) {
        if (nodes.isEmpty()) {
            return "查到客户" + customer.getName() + "，但暂无可展示的流转记录。当前状态：" + currentStatus + "。";
        }
        StringBuilder summary = new StringBuilder();
        summary.append("客户").append(customer.getName()).append("（").append(customer.getMobile()).append("）");
        summary.append("于").append(nodes.get(0).getTime()).append("进入系统。");
        for (CustomerFlowNode node : nodes) {
            if (StringUtils.equals(node.getTitle(), "进入公海")) {
                summary.append(node.getDescription());
                break;
            }
        }
        List<String> afterInbound = nodes.stream()
                .filter(node -> !StringUtils.equals(node.getTitle(), "上游入库")
                        && !StringUtils.equals(node.getTitle(), "进入公海"))
                .map(node -> node.getTime() + "，" + node.getDescription())
                .collect(Collectors.toList());
        if (!afterInbound.isEmpty()) {
            summary.append("后续流转：").append(StringUtils.join(afterInbound, " "));
        }
        summary.append("当前状态：").append(currentStatus).append("。");
        return summary.toString();
    }

    private String currentOwner(CustomerInfoItemDO customer, Map<Long, String> userNames) {
        if (isZero(customer.getOwnerUserId())) {
            return PUBLIC_POOL;
        }
        return userNames.getOrDefault(customer.getOwnerUserId(), blankDefault(customer.getOwnerUserId(), "--"));
    }

    private String currentStatus(CustomerInfoItemDO customer, String currentOwner) {
        if (isZero(customer.getOwnerUserId())) {
            return "在公海中";
        }
        return "归属于" + currentOwner;
    }

    private List<FieldDiffItemInfo> parseDiff(CustomerInfoItemTraceDO trace) {
        if (trace == null || StringUtils.isBlank(trace.getUpdFieldJson())) {
            return List.of();
        }
        try {
            Collection<FieldDiffItemInfo> parsed = GsonHelper.fromJson(trace.getUpdFieldJson(),
                    new TypeToken<List<FieldDiffItemInfo>>() {
                    }.getType());
            return parsed == null ? List.of() : new ArrayList<>(parsed);
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private FieldDiffItemInfo findDiff(List<FieldDiffItemInfo> diffs, String fieldName) {
        return diffs.stream()
                .filter(diff -> StringUtils.equals(diff.getFieldName(), fieldName))
                .findFirst()
                .orElse(null);
    }

    private boolean starCleared(List<FieldDiffItemInfo> diffs) {
        FieldDiffItemInfo diff = findDiff(diffs, "publicPoolStarFlag");
        return diff != null && StringUtils.equals(diff.getOldValue(), "1") && StringUtils.equals(diff.getNewValue(), "0");
    }

    private String dispatchResultText(String result, CustomerInfoItemDO customer) {
        if (isPublicPoolResult(result)) {
            return "进入公海";
        }
        if (StringUtils.equalsAnyIgnoreCase(result, "AUTO_ASSIGNED", "ASSIGNED")) {
            return "自动分配给业务员";
        }
        if (isZero(customer.getOwnerUserId())) {
            return "进入公海";
        }
        return StringUtils.defaultIfBlank(result, "已入库");
    }

    private boolean isPublicPoolResult(String result) {
        return StringUtils.equalsAnyIgnoreCase(result, "PUBLIC_POOL", "STAR_PUBLIC_POOL", "NO_ONLINE_SALES_PUBLIC_POOL");
    }

    private String channelName(Integer channel) {
        if (channel == null) {
            return "上游渠道";
        }
        return "渠道" + channel;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return "--";
    }

    private String mapString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private Date toDate(Object value) {
        if (value instanceof Date date) {
            return date;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }

    private String format(Date date) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    private Long parseLong(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private boolean isZero(Long value) {
        return value == null || value <= 0;
    }

    private void addPositive(Set<Long> ids, Long value) {
        if (value != null && value > 0) {
            ids.add(value);
        }
    }

    private String blankDefault(Long value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }
}
