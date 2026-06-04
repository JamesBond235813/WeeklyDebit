package com.jhl.silver.union.web.controller.customer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageInfo;
import com.google.gson.reflect.TypeToken;
import com.jhl.silver.union.biz.common.BizConstance;
import com.jhl.silver.union.biz.common.enums.BizDictConfigTypeEnum;
import com.jhl.silver.union.biz.common.enums.LoginResultEnum;
import com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum;
import com.jhl.silver.union.biz.config.BizProperty;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerInfoItemDO;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerInfoItemTraceDO;
import com.jhl.silver.union.biz.customer.manager.CustomerInfoItemManager;
import com.jhl.silver.union.biz.customer.manager.CustomerInfoItemTraceManager;
import com.jhl.silver.union.biz.customer.service.CustNoticeService;
import com.jhl.silver.union.biz.customer.service.BizConfigService;
import com.jhl.silver.union.biz.customer.service.CustomerInfoService;
import com.jhl.silver.union.biz.data.FieldDiffItemInfo;
import com.jhl.silver.union.biz.data.UserContext;
import com.jhl.silver.union.biz.dept.service.DeptService;
import com.jhl.silver.union.biz.user.dal.entity.SuUserLoginTraceDO;
import com.jhl.silver.union.biz.user.manager.SuUserLoginTraceManager;
import com.jhl.silver.union.commons.CommonResultCode;
import com.jhl.silver.union.commons.SuResult;
import com.jhl.silver.union.commons.exception.BizException;
import com.jhl.silver.union.commons.gson.GsonHelper;
import com.jhl.silver.union.commons.utils.SuDateUtils;
import com.jhl.silver.union.commons.utils.SuResultUtils;
import com.jhl.silver.union.web.data.BizDictItem;
import com.jhl.silver.union.web.data.CustomerItemDTO;
import com.jhl.silver.union.web.data.customer.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 客户信息业务类接口
 *
 * @author: qingren
 * @create_time: 2025/3/30
 */
@RestController
@Tag(name = "客户信息业务类接口")
@RequestMapping("/cust")
@Slf4j
public class CustomerController {

    @Resource
    private BizConfigService bizConfigService;
    @Resource
    private CustomerInfoService customerInfoService;
    @Autowired
    private BizProperty bizProperty;
    @Resource
    private CustomerInfoItemManager customerInfoItemManager;
    @Resource
    private CustomerInfoItemTraceManager traceManager;
    @Resource
    private SuUserLoginTraceManager loginTraceManager;
    @Resource
    private DeptService deptService;
    @Resource
    private CustNoticeService custNoticeService;

    /**
     * 分页获取客户列表信息
     *
     * @return
     */
    @PostMapping("/page-list-customers")
    @Operation(summary = "分页获取客户列表信息")
    public SuResult<PageInfo<CustomerItemDTO>> pagedListCustomer(@RequestBody PagedListCustomerRequest request) {
        request.autoFix();
        log.info("========>pagedListCustomer request: {}", GsonHelper.toJson(request));
        // request.setOwnerUserId(UserContext.getUserId());
        PageInfo<CustomerItemDTO> pageInfo =
                customerInfoService.pageListCustomerInfo(request, UserContext.getUserId(), UserContext.getDeptId(),
                        UserContext.getRoles(), true);
        return SuResultUtils.successResult(pageInfo);
    }

    /**
     * 获取各业务字典信息
     *
     * @return
     */
    @GetMapping("/list-dict-items")
    @Operation(summary = "获取各业务字典信息", description = "返回 Map&lt;业务类型, 业务字典信息列表>")
    public SuResult<Map<String, List<BizDictItem>>> getBizDictItems() {
        Map<String, List<BizDictItem>> bizDictItemMap = new HashMap<>();
        bizDictItemMap.put(BizDictConfigTypeEnum.CALL_RESULT_TIPS.name(),
                bizConfigService.getBizDictItems(BizDictConfigTypeEnum.CALL_RESULT_TIPS, true));
        bizDictItemMap.put(BizDictConfigTypeEnum.PROGRESS.name(),
                bizConfigService.getBizDictItems(BizDictConfigTypeEnum.PROGRESS, true));
        bizDictItemMap.put(BizDictConfigTypeEnum.CUSTOMER_STAR_GROUP.name(),
                bizConfigService.getBizDictItems(BizDictConfigTypeEnum.CUSTOMER_STAR_GROUP, true));
        bizDictItemMap.put(BizDictConfigTypeEnum.DATA_CHANNEL.name(),
                bizConfigService.getBizDictItems(BizDictConfigTypeEnum.DATA_CHANNEL, true));
        bizDictItemMap.put(BizDictConfigTypeEnum.ZHIMA_SCORE_THRESHOLD.name(),
                bizConfigService.getBizDictItems(BizDictConfigTypeEnum.ZHIMA_SCORE_THRESHOLD, true));
        return SuResultUtils.successResult(bizDictItemMap);
    }

    /**
     * 获取客户信息列表字段配置
     */
    @GetMapping("/field-config")
    @Operation(summary = "获取客户信息列表字段配置")
    public SuResult<List<CustomerFieldConfigItem>> getCustomerFieldConfig() {
        List<BizDictItem> list = bizConfigService.getBizDictItems(BizDictConfigTypeEnum.CUSTOMER_LIST_FIELD, true);
        List<CustomerFieldConfigItem> result = list.stream()
                .filter(item -> StringUtils.isNotBlank(item.getDescription()))
                .map(item -> new CustomerFieldConfigItem()
                        .setFieldKey(item.getDescription())
                        .setLabel(item.getLabel()))
                .collect(Collectors.toList());
        return SuResultUtils.successResult(result);
    }

    /**
     * 客户提醒汇总
     */
    @GetMapping("/notice-summary")
    @Operation(summary = "客户提醒汇总")
    public SuResult<CustomerNoticeSummaryDTO> getCustomerNoticeSummary() {
        Long userId = UserContext.getUserId();
        Long deptId = UserContext.getDeptId();
        Set<UserAuthRoleEnum> roles = UserContext.getRoles();
        Date lastLogin = getPreviousLoginTime(userId);
        CustomerNoticeSummaryDTO summary = new CustomerNoticeSummaryDTO()
                .setNewCustomerCount(0)
                .setAssignedCustomerCount(0)
                .setSinceTime(lastLogin == null ? null
                        : SuDateUtils.format(lastLogin, SuDateUtils.DF_YYYY_MM_DDHHMMSS));
        if (lastLogin == null || userId == null) {
            return SuResultUtils.successResult(summary);
        }
        if (roles.contains(UserAuthRoleEnum.ROLE_SUPPER) || roles.contains(UserAuthRoleEnum.ROLE_DEPT_DATA_ADMIN)) {
            summary.setNewCustomerCount(countNewCustomers(lastLogin, deptId, roles));
        }
        if (roles.contains(UserAuthRoleEnum.ROLE_SALES)) {
            summary.setAssignedCustomerCount(countAssignedCustomers(lastLogin, userId));
        }
        return SuResultUtils.successResult(summary);
    }

    /**
     * 客户通知未读列表
     */
    @GetMapping("/notice/unread")
    @Operation(summary = "客户通知未读列表")
    public SuResult<List<CustomerNoticeItemDTO>> listUnreadNotices(
            @RequestParam(name = "limit", required = false, defaultValue = "20") Integer limit) {
        Long userId = UserContext.getUserId();
        List<CustomerNoticeItemDTO> list = custNoticeService.listUnread(userId, limit == null ? 20 : limit);
        return SuResultUtils.successResult(list);
    }

    /**
     * 标记客户通知已读
     */
    @PostMapping("/notice/mark-read")
    @Operation(summary = "标记客户通知已读")
    public SuResult<Void> markNoticeRead(@RequestBody CustomerNoticeReadRequest request) {
        request.validate();
        custNoticeService.markRead(UserContext.getUserId(), request.getIds());
        return SuResultUtils.successResult();
    }

    private Date getPreviousLoginTime(Long userId) {
        if (userId == null) {
            return null;
        }
        LambdaQueryWrapper<SuUserLoginTraceDO> qw = new LambdaQueryWrapper<>();
        qw.eq(SuUserLoginTraceDO::getUserId, userId)
                .eq(SuUserLoginTraceDO::getLoginResult, LoginResultEnum.SUCC.name())
                .orderByDesc(SuUserLoginTraceDO::getGmtCreate)
                .last("limit 2");
        List<SuUserLoginTraceDO> list = loginTraceManager.list(qw);
        if (list.size() < 2) {
            return null;
        }
        return list.get(1).getGmtCreate();
    }

    private int countNewCustomers(Date lastLogin, Long deptId, Set<UserAuthRoleEnum> roles) {
        LambdaQueryWrapper<CustomerInfoItemDO> qw = new LambdaQueryWrapper<>();
        qw.gt(CustomerInfoItemDO::getGmtCreate, lastLogin);
        if (roles.contains(UserAuthRoleEnum.ROLE_SUPPER)) {
            return (int) customerInfoItemManager.count(qw);
        }
        if (roles.contains(UserAuthRoleEnum.ROLE_DEPT_DATA_ADMIN)) {
            Long realDeptId = deptId == null ? BizConstance.NONE_DEPT_ID : deptId;
            List<Long> deptIds = deptService.getAllChildrenIdByParentDeptId(realDeptId, true);
            if (deptIds.isEmpty()) {
                return 0;
            }
            qw.in(CustomerInfoItemDO::getOwnerDeptId, deptIds);
            return (int) customerInfoItemManager.count(qw);
        }
        return 0;
    }

    private int countAssignedCustomers(Date lastLogin, Long userId) {
        LambdaQueryWrapper<CustomerInfoItemTraceDO> qw = new LambdaQueryWrapper<>();
        qw.gt(CustomerInfoItemTraceDO::getGmtCreate, lastLogin)
                .like(CustomerInfoItemTraceDO::getUpdFieldJson, "\"ownerUserId\"");
        List<CustomerInfoItemTraceDO> traces = traceManager.list(qw);
        if (traces.isEmpty()) {
            return 0;
        }
        Set<Long> custIds = new HashSet<>();
        for (CustomerInfoItemTraceDO trace : traces) {
            String json = trace.getUpdFieldJson();
            if (StringUtils.isBlank(json)) {
                continue;
            }
            List<FieldDiffItemInfo> diffList = GsonHelper.fromJson(json,
                    new TypeToken<List<FieldDiffItemInfo>>() {
                    }.getType());
            if (diffList == null) {
                continue;
            }
            for (FieldDiffItemInfo diff : diffList) {
                if (!"ownerUserId".equals(diff.getFieldName())) {
                    continue;
                }
                if (StringUtils.equals(String.valueOf(userId), diff.getNewValue())) {
                    custIds.add(trace.getSourceId());
                }
            }
        }
        return custIds.size();
    }

    /**
     * 更新客户信息。 不可更新姓名等客观事实信息
     *
     * @param request
     * @return
     */
    @PostMapping("/upd-biz-cust-info")
    @Operation(summary = "更新客户业务信息。 不可更新姓名等客观事实信息")
    public SuResult<Void> updateBizCustomerInfo(@RequestBody UpdateBizCustomerInfoRequest request) {
        customerInfoService.updateCustomerBizInfo(request, UserContext.getUserId(), UserContext.getRoles());
        return SuResultUtils.successResult();
    }

    /**
     * 获取客户信息详情
     *
     * @param id
     * @return
     */
    @GetMapping("/detail")
    @Operation(summary = "获取客户信息详情")
    public SuResult<CustomerItemDetailDTO> getCustomerDetailInfo(
            @Parameter(name = "cid", description = "客户 ID", required = true)
            @RequestParam(name = "cid") Long id) {
        CustomerItemDetailDTO detail =
                customerInfoService.getCustomerItemDetail(id, UserContext.getUserId(), UserContext.getDeptId(),
                        UserContext.getRoles());
        return SuResultUtils.successResult(detail);
    }

    /**
     * 将客户信息退回至公海
     *
     * @param id
     * @return
     */
    @GetMapping("/rtn-cust")
    @Operation(summary = "将客户信息退回至公海")
    public SuResult<Void> backToOcean(
            @Parameter(name = "cid", description = "客户 ID", required = true)
            @RequestParam(name = "cid") Long id) {
        customerInfoService.backCustomer(id, UserContext.getUserId());
        return SuResultUtils.successResult();
    }

    /**
     * 将客户信息批量退回至公海
     *
     * @param request
     * @return
     */
    @PostMapping("/btc-rtn-cust")
    @Operation(summary = "将客户信息批量退回至公海")
    public SuResult<Void> batchBackToOcean(@RequestBody BatchBackToOceanRequest request) {
        request.validate();
        customerInfoService.backCustomers(request.getCustIdList(), UserContext.getUserId(), UserContext.getDeptId());
        return SuResultUtils.successResult();
    }

    /**
     * 从公海领取客户信息
     *
     * @param id 客户 ID
     * @return
     */
    @GetMapping("/claim-cust")
    @Operation(summary = "从公海领取客户信息")
    public SuResult<Void> claimCustomer(
            @Parameter(name = "cid", description = "客户 ID", required = true)
            @RequestParam(name = "cid") Long id) {
        customerInfoService.claimCustomer(id, UserContext.getUserId(), UserContext.getDeptId());
        return SuResultUtils.successResult();
    }

    /**
     * 将客户信息批量标记为不同的收藏类型
     *
     * @param request
     * @return
     */
    @PostMapping("/btc-swt-fav")
    @Operation(summary = "将客户信息批量标记为不同的收藏类型")
    public SuResult<Void> batchSwitchFavorite(@RequestBody BatchSwitchFavoriteRequest request) {
        request.validate();
        customerInfoService.switchFav4Customers(request.getCustIdList(), request.getFavoriteTypeEnum(),
                UserContext.getUserId(), UserContext.getDeptId());
        return SuResultUtils.successResult();
    }

    /**
     * 查询指定的数据来源渠道， 若不存在则新增数据渠道信息
     *
     * @return
     */
    @PostMapping("/ensure-data-channel")
    @Operation(summary = "查询指定的数据来源渠道， 若不存在则新增数据渠道信息",
               description = "本接口不验证用户登录信息")
    public SuResult<BizDictItem> ensureDataChannel(@RequestBody EnsureDataChannelRequest request) {
        request.validate();
        if (!StringUtils.equals(bizProperty.getExtraToken(), request.getToken())) {
            throw new BizException(CommonResultCode.INVALID_REQUEST, "invalid token: " + request.getToken());
        }
        Set<String> channelNameSet = Set.of(request.getChannelName());
        bizConfigService.generateChannelDictByNames(channelNameSet, 0L);
        BizDictItem item = bizConfigService.getSingleBizDictItemByLabel(BizDictConfigTypeEnum.DATA_CHANNEL,
                request.getChannelName());
        return SuResultUtils.successResult(item);
    }


}
