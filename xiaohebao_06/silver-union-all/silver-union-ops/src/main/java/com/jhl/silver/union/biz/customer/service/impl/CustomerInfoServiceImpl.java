package com.jhl.silver.union.biz.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jhl.silver.union.biz.common.BizConstance;
import com.jhl.silver.union.biz.common.ResultCode;
import com.jhl.silver.union.biz.common.enums.*;
import com.jhl.silver.union.biz.common.utils.FieldUtils;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerInfoItemDO;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerInfoItemTraceDO;
import com.jhl.silver.union.biz.customer.manager.CustomerInfoItemManager;
import com.jhl.silver.union.biz.customer.manager.CustomerInfoItemTraceManager;
import com.jhl.silver.union.biz.customer.service.BizConfigService;
import com.jhl.silver.union.biz.customer.service.CustomerInfoService;
import com.jhl.silver.union.biz.customer.service.CustomerUpdTraceStrategyContext;
import com.jhl.silver.union.biz.customer.service.CustDispatchService;
import com.jhl.silver.union.biz.customer.service.CustNoticeService;
import com.jhl.silver.union.biz.customer.service.MobileAreaService;
import com.jhl.silver.union.biz.data.CustomerInfoQry;
import com.jhl.silver.union.biz.data.DeptInfo;
import com.jhl.silver.union.biz.data.FieldDiffItemInfo;
import com.jhl.silver.union.biz.data.IdCardAreaInfo;
import com.jhl.silver.union.biz.data.MobileAreaInfo;
import com.jhl.silver.union.biz.data.convert.CustomerConvert;
import com.jhl.silver.union.biz.dept.service.DeptService;
import com.jhl.silver.union.biz.region.service.IdCardAreaService;
import com.jhl.silver.union.biz.user.service.UserService;
import com.jhl.silver.union.commons.CommonConstant;
import com.jhl.silver.union.commons.db.PageInfoUtils;
import com.jhl.silver.union.commons.db.SuSqlUtils;
import com.jhl.silver.union.commons.db.TransactionUtils;
import com.jhl.silver.union.commons.exception.BizException;
import com.jhl.silver.union.commons.gson.GsonHelper;
import com.jhl.silver.union.commons.utils.IdCardUtils;
import com.jhl.silver.union.commons.utils.OtherUtils;
import com.jhl.silver.union.commons.utils.SuDateUtils;
import com.jhl.silver.union.commons.utils.VerifyUtils;
import com.jhl.silver.union.web.data.BizDictItem;
import com.jhl.silver.union.web.data.CustomerItemDTO;
import com.jhl.silver.union.web.data.LeaderRemarkDTO;
import com.jhl.silver.union.web.data.admin.AddCustomerInfoRequest;
import com.jhl.silver.union.web.data.admin.AddLeaderRemarkRequest;
import com.jhl.silver.union.web.data.admin.DispatchCustomerInfoRequest;
import com.jhl.silver.union.web.data.admin.UpdCustomerInfoRequest;
import com.jhl.silver.union.web.data.customer.CustomerItemDetailDTO;
import com.jhl.silver.union.web.data.customer.CustomerUpdTraceDTO;
import com.jhl.silver.union.web.data.customer.PagedListCustomerRequest;
import com.jhl.silver.union.web.data.customer.UpdateBizCustomerInfoRequest;
import com.jhl.silver.union.web.data.user.BriefUserInfoDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: qingren
 * @create_time: 2025/3/30
 */
@Service
@Slf4j
public class CustomerInfoServiceImpl implements CustomerInfoService {
    private final static Set<String> IGNORE_FIELD_NAMES = Set.of("version", "gmtModified", "gmtCreate", "followTime");
    /**
     * 表示全量部门ID
     */
    private final static Set<Long> ALLOW_ALL_DEPT_IDS = Sets.newHashSet();
    @Resource
    private DeptService deptService;
    @Resource
    private CustomerConvert customerConvert;
    @Resource
    private CustomerInfoItemManager customerManager;
    @Resource
    private UserService userService;
    @Resource
    private BizConfigService bizConfigService;
    @Resource
    private CustomerInfoItemTraceManager traceManager;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private MobileAreaService mobileAreaService;
    @Resource
    private IdCardAreaService idCardAreaService;
    @Resource
    private CustDispatchService custDispatchService;
    @Resource
    private CustNoticeService custNoticeService;

    @Override
    public PageInfo<CustomerItemDTO> pageListCustomerInfo(PagedListCustomerRequest request, Long optUserId,
            Long optUserDeptId, Set<UserAuthRoleEnum> roles, boolean needExtendQry) {
        VerifyUtils.notNull(optUserId, "optUserId", "请指定操作人ID", true);
        CustomerInfoQry qry = CustomerInfoQry.makeupFrom(request);
        // 校验数据可见范围
        this.adaptQryByRoles(qry, optUserId, optUserDeptId, roles);
        PageInfo<CustomerInfoItemDO> innerPage = SuSqlUtils.pagedListQuery(request.getPage(), request.getPageSize(),
                true,
                () -> customerManager.list(qry.toQueryWrapper()));
        PageInfo<CustomerItemDTO> pageInfo = PageInfoUtils.copyPageInfoWithoutListFrom(innerPage);
        if (PageInfoUtils.isEmpty(innerPage)) {
            return pageInfo;
        }
        Set<Long> userIds = Sets.newHashSet();
        Set<Long> deptIds = Sets.newHashSet();

        List<CustomerItemDTO> list = innerPage.getList().stream()
                .peek(e -> {
                    OtherUtils.processIf(Objects.nonNull(e.getOwnerUserId()) && e.getOwnerUserId() > 0L,
                            () -> userIds.add(e.getOwnerUserId()));
                    OtherUtils.processIf(Objects.nonNull(e.getFollowerUserId()) && e.getFollowerUserId() > 0L,
                            () -> userIds.add(e.getFollowerUserId()));
                    OtherUtils.processIf(Objects.nonNull(e.getOwnerDeptId()) && e.getOwnerDeptId() > 0L,
                            () -> deptIds.add(e.getOwnerDeptId()));
                })
                .map(e -> customerConvert.convert2CustomerItemDTO(e))
                .collect(Collectors.toList());
        pageInfo.setList(list);
        if (!needExtendQry) {
            return pageInfo;
        }
        Map<Long, String> userIdNameMap = userService.getUserRealNames(userIds);
        Map<Long, String> deptIdNameMap = deptService.getDeptNameByIds(deptIds);

        Map<Integer, BizDictItem> callResultTipsMap = bizConfigService
                .getBizDictItemMap(BizDictConfigTypeEnum.CALL_RESULT_TIPS, false);
        Map<Integer, BizDictItem> progressMap = bizConfigService.getBizDictItemMap(BizDictConfigTypeEnum.PROGRESS,
                false);
        Map<Integer, BizDictItem> starGroup = bizConfigService
                .getBizDictItemMap(BizDictConfigTypeEnum.CUSTOMER_STAR_GROUP, false);
        Map<Integer, BizDictItem> channelMap = bizConfigService.getBizDictItemMap(BizDictConfigTypeEnum.DATA_CHANNEL,
                false);
        list.stream().forEach(
                item -> this.setupExtendInfo(item, userIdNameMap, deptIdNameMap, callResultTipsMap, progressMap,
                        starGroup, channelMap));
        return pageInfo;
    }

    /**
     * 组装扩展信息
     */
    private void setupExtendInfo(CustomerItemDTO item, Map<Long, String> userIdNameMap, Map<Long, String> deptIdNameMap,
            Map<Integer, BizDictItem> callResultTipsMap, Map<Integer, BizDictItem> progressMap,
            Map<Integer, BizDictItem> starGroup, Map<Integer, BizDictItem> channelMap) {

        item.setOwnerUserName(userIdNameMap.get(item.getOwnerUserId()))
                .setFollowerUserName(userIdNameMap.get(item.getFollowerUserId()))
                .setOwnerDeptName(deptIdNameMap.get(item.getOwnerDeptId()))
                .setCallTipsDesc(
                        Optional.ofNullable(callResultTipsMap.get(item.getCallTips())).map(BizDictItem::getLabel)
                                .orElse(StringUtils.EMPTY))
                .setProgressDesc(Optional.ofNullable(progressMap.get(item.getProgress())).map(BizDictItem::getLabel)
                        .orElse(StringUtils.EMPTY))
                .setCustomerGroupDesc(
                        Optional.ofNullable(starGroup.get(item.getCustomerGroup())).map(BizDictItem::getLabel)
                                .orElse(StringUtils.EMPTY))
                .setChannelDesc(Optional.ofNullable(channelMap.get(item.getChannel())).map(BizDictItem::getLabel)
                        .orElse(StringUtils.EMPTY));
        this.setupQualification(item);
    }

    /**
     * 组装资质信息描述
     *
     * @param item
     */
    private void setupQualification(CustomerItemDTO item) {
        StringBuilder builder = new StringBuilder();
        // 房产
        if (Objects.equals(item.getHouseFlag(), HouseFlagEnum.JING_HOUSE.flag)) {
            builder.append(HouseFlagEnum.JING_HOUSE.desc).append("/");
        }
        if (Objects.equals(item.getHouseFlag(), HouseFlagEnum.YES.flag)) {
            builder.append(HouseFlagEnum.YES.desc).append("/");
        }
        if (Objects.equals(item.getSocialInsuranceFlag(), BizFlagEnum.YES.flag)) {
            builder.append("社保/");
        }
        if (Objects.equals(item.getProvidentFlag(), BizFlagEnum.YES.flag)) {
            builder.append("公积金/");
        }
        if (Objects.equals(item.getCarFlag(), BizFlagEnum.YES.flag)) {
            builder.append("车产/");
        }
        if (Objects.equals(item.getEnterpriseFlag(), BizFlagEnum.YES.flag)) {
            builder.append("企业主/");
        }
        if (Objects.equals(item.getCreditCardFlag(), BizFlagEnum.YES.flag)) {
            builder.append("信用卡/");
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        item.setQualification(builder.toString());
    }

    @Override
    public void updateCustomerBizInfo(UpdateBizCustomerInfoRequest request, Long optUserId,
            Set<UserAuthRoleEnum> roles) {
        VerifyUtils.notNull(optUserId, "optUserId", "请指定操作人ID", true);
        request.validate();
        CustomerInfoQry qry = new CustomerInfoQry()
                .setIds(List.of(request.getId()))
        // .setOwnerUserId(optUserId)
        ;
        CustomerInfoItemDO existed = customerManager.getOne(qry.toQueryWrapper());
        this.checkUpdateRules(request, existed, optUserId, roles);
        CustomerInfoItemDO toUpdate = customerConvert.convert2CustomerInfoItemDO(request);
        this.applyIdCardInfo(toUpdate);
        this.fillMobileAreaIfBlank(toUpdate, existed.getMobile());
        this.fillHukouAreaIfBlank(toUpdate);
        boolean isOwner = Objects.equals(existed.getOwnerUserId(), optUserId);
        if (isOwner) {
            toUpdate.setFollowerUserId(optUserId);
        }
        this.updateCustomerInfoWithTraceInfo(toUpdate, existed, optUserId, false, request);
        if (isOwner) {
            custNoticeService.markReadByCustomer(optUserId, request.getId());
        }
    }

    /**
     * 检查更新客户业务信息需要满足的条件
     *
     * @param existed
     * @param optUserId
     */
    private void checkUpdateRules(UpdateBizCustomerInfoRequest request, CustomerInfoItemDO existed, Long optUserId,
            Set<UserAuthRoleEnum> roles) {
        if (Objects.isNull(existed)) {
            throw new BizException(ResultCode.CUST_NOT_FOUND,
                    "custId: " + request.getId() + ", optUserId:" + optUserId);
        }
        if (Objects.equals(existed.getOwnerUserId(), 0L)) {
            throw new BizException(ResultCode.CUST_CANT_NOT_EDIT_4_OCEAN,
                    "custId: " + request.getId() + ", optUserId:" + optUserId);
        }
        if (roles.contains(UserAuthRoleEnum.ROLE_SUPPER)) {
            return;
        }
        if (!Objects.equals(optUserId, existed.getOwnerUserId())) {
            throw new BizException(ResultCode.CUST_CANT_NOT_EDIT_4_NO_AUTH,
                    "custId: " + request.getId() + ", optUserId:" + optUserId);
        }
        // 若操作人即为数据归属人， 则不可更新【领导评价】
        request.setLeaderRemark(null);
    }

    /**
     * 更新客户信息，并记录更新历史信息
     *
     * @param pairList      待更新对象 - 既有对象 pair 的列表
     * @param optUserId
     * @param resetFavorite true： 重置收藏状态
     * @param request       原始信息， 用于打日志
     */
    private void updateCustomerInfoWithTraceInfo(List<Pair<CustomerInfoItemDO, CustomerInfoItemDO>> pairList,
            Long optUserId, boolean resetFavorite, Object request) {
        List<CustomerInfoItemDO> toUpdateList = Lists.newArrayList();
        List<CustomerInfoItemTraceDO> traceList = Lists.newArrayList();
        for (Pair<CustomerInfoItemDO, CustomerInfoItemDO> pair : pairList) {
            CustomerInfoItemDO toUpdate = pair.getFirst();
            List<CustomerInfoItemTraceDO> innerTraceList = this.parseDiffAndMakeupTraces(toUpdate, pair.getSecond(),
                    optUserId, resetFavorite);
            toUpdateList.add(toUpdate);
            traceList.addAll(innerTraceList);
        }

        TransactionUtils.executeTransaction(transactionTemplate, () -> {
            try {
                boolean rs = customerManager.updateBatchById(toUpdateList);
                if (!rs) {
                    throw new BizException(ResultCode.CUST_UPDATE_FAILED_4_VERSION,
                            "toUpdateInfo: " + GsonHelper.toJson(request) + ", optUserId:" + optUserId);
                }
            } catch (Exception e) {
                log.error("Update customer info [list: {}] failed: {}", GsonHelper.toJson(toUpdateList),
                        e.getMessage());
                throw new BizException(ResultCode.CUST_UPDATE_FAILED_4_VERSION,
                        "toUpdateInfo: " + GsonHelper.toJson(request) + ", optUserId:" + optUserId);
            }
            traceManager.saveBatch(traceList);
        });
    }

    private List<CustomerInfoItemTraceDO> parseDiffAndMakeupTraces(CustomerInfoItemDO toUpdate,
            CustomerInfoItemDO existed, Long optUserId, boolean resetFavorite) {
        List<FieldDiffItemInfo> diffList = FieldUtils.getFieldDiffItemInfoListWith(existed, toUpdate,
                CustomerInfoItemDO.class,
                IGNORE_FIELD_NAMES);
        if (CollectionUtils.isEmpty(diffList)) {
            throw new BizException(ResultCode.CUST_NOT_CHANGED,
                    "custId: " + toUpdate.getId() + ", optUserId:" + optUserId);
        }
        // 是否需要再次生成字段
        boolean needDiffAgain = false;
        if (resetFavorite) {
            needDiffAgain = true;
            toUpdate.setOwnerFavorite(FavoriteTypeEnum.NORMAL.code);
        }

        Date progressDate = null;
        if (CustomerUpdTraceStrategyContext.containProgressUpdating(diffList, UpdTypeEnum.PROGRESS)) {
            needDiffAgain = true;
            // 增加跟进次数
            toUpdate.setFollowCnt(existed.getFollowCnt() + 1);
            // 强行将中跟进时间与更新历史的创建时间对齐，避免时间误差带来的麻烦
            progressDate = new Date();
            toUpdate.setFollowTime(progressDate);
        }

        if (needDiffAgain) {
            diffList = FieldUtils.getFieldDiffItemInfoListWith(existed, toUpdate, CustomerInfoItemDO.class,
                    IGNORE_FIELD_NAMES);
        }

        List<CustomerInfoItemTraceDO> traceList = this.makeupTraceDO(diffList, toUpdate.getId(), optUserId,
                progressDate);
        return traceList;
    }

    /**
     * 更新客户信息，并记录更新历史信息
     *
     * @param toUpdate
     * @param existed
     * @param optUserId
     * @param resetFavorite true： 重置收藏状态
     * @param request       原始信息， 用于打日志
     */
    private void updateCustomerInfoWithTraceInfo(CustomerInfoItemDO toUpdate, CustomerInfoItemDO existed,
            Long optUserId, boolean resetFavorite, Object request) {

        List<CustomerInfoItemTraceDO> traceList = this.parseDiffAndMakeupTraces(toUpdate, existed, optUserId,
                resetFavorite);

        TransactionUtils.executeTransaction(transactionTemplate, () -> {
            try {
                boolean rs = customerManager.updateById(toUpdate);
                if (!rs) {
                    throw new BizException(ResultCode.CUST_UPDATE_FAILED_4_VERSION,
                            "toUpdateInfo: " + GsonHelper.toJson(request) + ", optUserId:" + optUserId);
                }
            } catch (Exception e) {
                log.error("Update customer info [id: {}] failed: {}", toUpdate.getId(), e.getMessage());
                throw new BizException(ResultCode.CUST_UPDATE_FAILED_4_VERSION,
                        "toUpdateInfo: " + GsonHelper.toJson(request) + ", optUserId:" + optUserId);
            }
            traceManager.saveBatch(traceList);
        });
    }

    @Override
    public CustomerItemDetailDTO getCustomerItemDetail(Long custId, Long optUserId, Long optUserDeptId,
            Set<UserAuthRoleEnum> roles) {
        VerifyUtils.notNull(optUserId, "optUserId", "请指定操作人ID", true);

        // 优先查看归属人为自己的客户信息
        CustomerInfoQry qry = new CustomerInfoQry()
                .setIds(List.of(custId))
                .setOwnerUserId(optUserId);
        CustomerInfoItemDO custInfoDO = customerManager.getOne(qry.toQueryWrapper());

        if (Objects.isNull(custInfoDO)) {
            qry = new CustomerInfoQry()
                    .setIds(List.of(custId));
            // 校验数据可见范围
            this.adaptQryByRoles(qry, optUserId, optUserDeptId, roles);
            custInfoDO = customerManager.getOne(qry.toQueryWrapper());
            if (Objects.isNull(custInfoDO)) {
                throw new BizException(ResultCode.CUST_NOT_FOUND,
                        "id: " + custId + ", optUserId:" + optUserId + ", optUserDeptId:" + optUserDeptId);
            }
        }

        this.applyIdCardInfo(custInfoDO);
        this.fillMobileAreaIfBlank(custInfoDO, custInfoDO.getMobile());
        this.fillHukouAreaIfBlank(custInfoDO);

        CustomerItemDetailDTO item = customerConvert.convertCustomerItemDetailDTO(custInfoDO);
        Set<Long> uids = Sets.newHashSet();
        uids.add(custInfoDO.getFollowerUserId());
        uids.add(custInfoDO.getOwnerUserId());
        Map<Long, String> userIdNameMap = userService.getUserRealNames(uids);
        Map<Long, String> deptIdNameMap = deptService.getDeptNameByIds(List.of(custInfoDO.getOwnerDeptId()));

        Map<Integer, BizDictItem> callResultTipsMap = bizConfigService
                .getBizDictItemMap(BizDictConfigTypeEnum.CALL_RESULT_TIPS, false);
        Map<Integer, BizDictItem> progressMap = bizConfigService.getBizDictItemMap(BizDictConfigTypeEnum.PROGRESS,
                false);
        Map<Integer, BizDictItem> starGroup = bizConfigService
                .getBizDictItemMap(BizDictConfigTypeEnum.CUSTOMER_STAR_GROUP, false);
        Map<Integer, BizDictItem> channelMap = bizConfigService.getBizDictItemMap(BizDictConfigTypeEnum.DATA_CHANNEL,
                false);
        this.setupExtendInfo(item, userIdNameMap, deptIdNameMap, callResultTipsMap, progressMap, starGroup, channelMap);
        List<CustomerInfoItemTraceDO> traceDOList = traceManager.listByCustomerId(custId, UpdTypeEnum.PROGRESS,
                UpdTypeEnum.LEAD_REMARK);
        if (CollectionUtils.isEmpty(traceDOList)) {
            return item;
        }
        List<CustomerUpdTraceDTO> progressList = Lists.newArrayList();
        List<CustomerUpdTraceDTO> leaderRemarklist = Lists.newArrayList();
        traceDOList.stream()
                .forEachOrdered(e -> {
                    CustomerUpdTraceDTO trace = new CustomerUpdTraceDTO()
                            .setUpdTime(e.getGmtCreate())
                            .setOptUserName(e.getOptUserRealName())
                            .setUpdTypeDesc(UpdTypeEnum.getDescByName(e.getUpdType()))
                            .setId(e.getId());
                    if (StringUtils.isNotBlank(e.getUpdDispContentJson())) {
                        try {
                            List<String> contentList = GsonHelper.fromJson(e.getUpdDispContentJson(),
                                    new com.google.gson.reflect.TypeToken<List<String>>() {
                                    }.getType());
                            trace.setDescList(contentList);
                        } catch (Exception ex) {
                            log.error("Parsing json failed. traceId: {},  json:{} , error: {}", e.getId(),
                                    e.getUpdDispContentJson(), ex.getMessage());
                        }
                    }
                    if (StringUtils.equals(e.getUpdType(), UpdTypeEnum.PROGRESS.name())) {
                        progressList.add(trace);
                    } else if (StringUtils.equals(e.getUpdType(), UpdTypeEnum.LEAD_REMARK.name())) {
                        leaderRemarklist.add(trace);
                    }
                });

        item.setLeaderRemarkList(leaderRemarklist)
                .setProgressList(progressList);
        return item;
    }

    @Override
    public Long addCustomerFactInfo(AddCustomerInfoRequest request, Long optUserId) {
        VerifyUtils.notNull(optUserId, "optUserId", "请指定操作人ID", true);
        request.validate();
        this.checkBizDict(request);
        this.getValidDept(request.getOwnerDeptId());
        CustomerInfoItemDO customerInfoItemDO = customerConvert
                .convert2CustomerInfoItemDOFromAddCustomerInfoRequest(request);
        this.applyIdCardInfo(customerInfoItemDO);
        this.fillMobileAreaIfBlank(customerInfoItemDO, request.getMobile());
        this.fillHukouAreaIfBlank(customerInfoItemDO);

        try {
            customerManager.save(customerInfoItemDO);
            custNoticeService.notifyNewAssignment(List.of(customerInfoItemDO), List.of(), "MANUAL");
            return customerInfoItemDO.getId();
        } catch (DuplicateKeyException e) {
            throw BizException.makeupBy(ResultCode.CUST_INFO_ALREADY_EXISTS, List.of(request.getName(),
                    request.getMobile()), "request: " + GsonHelper.toJson(request));
        }
    }

    @Override
    public void updateCustomerFactInfo(UpdCustomerInfoRequest request, Long optUserId) {
        VerifyUtils.notNull(optUserId, "optUserId", "请指定操作人ID", true);
        request.validate();
        this.checkBizDict(request);
        this.getValidDept(request.getOwnerDeptId());
        CustomerInfoItemDO toUpdate = customerConvert.convert2CustomerInfoItemDOFromUpdCustomerInfoRequest(request);
        this.applyIdCardInfo(toUpdate);
        this.fillMobileAreaIfBlank(toUpdate, request.getMobile());
        this.fillHukouAreaIfBlank(toUpdate);
        CustomerInfoQry qry = new CustomerInfoQry()
                .setIds(List.of(request.getId()));
        CustomerInfoItemDO existed = customerManager.getOne(qry.toQueryWrapper());
        if (Objects.isNull(existed)) {
            throw new BizException(ResultCode.CUST_NOT_FOUND,
                    "custId: " + request.getId() + ", optUserId:" + optUserId);
        }
        toUpdate.setVersion(existed.getVersion());
        this.updateCustomerInfoWithTraceInfo(toUpdate, existed, optUserId, false, request);
    }

    private void applyIdCardInfo(CustomerInfoItemDO target) {
        if (target == null || StringUtils.isBlank(target.getIdCardNo())) {
            return;
        }
        IdCardUtils.IdCardInfo info = IdCardUtils.parse(target.getIdCardNo());
        if (info == null) {
            return;
        }
        target.setBirthday(info.getBirthday());
        target.setSex(info.getSex());
        target.setAge(info.getAge());
    }

    private void fillMobileAreaIfBlank(CustomerInfoItemDO target, String mobile) {
        if (target == null || StringUtils.isNotBlank(target.getMobileArea())) {
            return;
        }
        MobileAreaInfo area = mobileAreaService.getMobileArea(mobile);
        if (area == null || StringUtils.isBlank(area.toDisplay())) {
            return;
        }
        target.setMobileArea(area.toDisplay());
    }

    private void fillHukouAreaIfBlank(CustomerInfoItemDO target) {
        if (target == null || StringUtils.isBlank(target.getIdCardNo())) {
            return;
        }
        boolean needFill = StringUtils.isBlank(target.getHukouProvince())
                || StringUtils.isBlank(target.getHukouCity())
                || StringUtils.isBlank(target.getHukouDistrict());
        if (!needFill) {
            return;
        }
        IdCardAreaInfo info = idCardAreaService.getAreaInfoByIdCard(target.getIdCardNo());
        if (info == null) {
            return;
        }
        if (StringUtils.isBlank(target.getHukouProvince())) {
            target.setHukouProvince(info.getProvince());
        }
        if (StringUtils.isBlank(target.getHukouCity())) {
            target.setHukouCity(info.getCity());
        }
        if (StringUtils.isBlank(target.getHukouDistrict())) {
            target.setHukouDistrict(info.getDistrict());
        }
    }

    @Override
    public void dispatchCustomer(DispatchCustomerInfoRequest request, Long optUserId, Long optUserDeptId,
            Set<UserAuthRoleEnum> roles) {
        VerifyUtils.notNull(optUserId, "optUserId", "请指定操作人ID", true);
        request.validate();
        // 校验部门信息
        DeptInfo deptInfo = getValidDept(request.getOwnerDeptId());
        if (Objects.nonNull(request.getOwnerId()) && !Objects.equals(request.getOwnerId(), 0L)) {
            // 校验人员信息
            this.checkUserDept(request.getOwnerId(), deptInfo);
        }
        // 分配客户信息
        this.dispatchCustomerWithRoles(request, optUserId, optUserDeptId, roles);
    }

    @Override
    public void backCustomer(Long id, Long optUserId) {
        VerifyUtils.notNull(optUserId, "optUserId", "请指定操作人ID", true);
        // 取客户信息数据
        CustomerInfoItemDO itemDO = customerManager.getById(id);
        VerifyUtils.judge(Objects.nonNull(itemDO) && Objects.equals(itemDO.getOwnerUserId(), optUserId),
                ResultCode.CUST_NOT_FOUND, true, "id:" + id + ",optUserId:" + optUserId);
        CustomerInfoItemDO toUpdate = new CustomerInfoItemDO()
                .setId(id)
                .setOwnerFavorite(CommonConstant.NO)
                .setOwnerUserId(0L)
                .setVersion(itemDO.getVersion());
        this.updateCustomerInfoWithTraceInfo(toUpdate, itemDO, optUserId, false, Map.of("id", id));
    }

    @Override
    public void backCustomers(List<Long> idList, Long optUserId, Long optUserDeptId) {
        VerifyUtils.notNull(optUserId, "optUserId", "请指定操作人ID", true);
        idList = idList.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(idList)) {
            return;
        }
        // 归属人为操作人即可
        List<CustomerInfoItemDO> itemList = customerManager.getByIds(idList, List.of());
        VerifyUtils.judge(!CollectionUtils.isEmpty(itemList), ResultCode.CUST_NOT_FOUND, true,
                "idList:" + idList + ",optUserId:" + optUserId);
        // toUpdate - existed
        List<Pair<CustomerInfoItemDO, CustomerInfoItemDO>> list = itemList.stream()
                .map(item -> {
                    CustomerInfoItemDO toUpdate = new CustomerInfoItemDO()
                            .setId(item.getId())
                            .setOwnerFavorite(CommonConstant.NO)
                            .setOwnerUserId(0L)
                            .setVersion(item.getVersion());
                    return Pair.of(toUpdate, item);
                }).collect(Collectors.toList());
        this.updateCustomerInfoWithTraceInfo(list, optUserId, false, Map.of("idList", idList));
    }

    @Override
    public void claimCustomer(Long id, Long optUserId, Long optUserDeptId) {
        VerifyUtils.notNull(optUserId, "optUserId", "请指定操作人ID", true);
        VerifyUtils.notNull(id, "id", "请选择要领取的客户", true);
        BriefUserInfoDTO user = userService.getUserInfo(optUserId);
        VerifyUtils.notNull(user, "user", ResultCode.USER_NOT_EXIST, true);
        VerifyUtils.verifyTrue(Objects.equals(user.getOnlineStatus(), CommonConstant.YES),
                "当前用户处于离线状态，不可领取客户数据", true);
        CustomerInfoItemDO existed = customerManager.getById(id);
        VerifyUtils.judge(Objects.nonNull(existed), ResultCode.CUST_NOT_FOUND, true,
                "id:" + id + ",optUserId:" + optUserId);
        VerifyUtils.verifyTrue(Objects.equals(existed.getOwnerUserId(), 0L),
                "该客户已被领取或分配，请刷新后再试", true);
        CustomerInfoItemDO toUpdate = new CustomerInfoItemDO()
                .setId(id)
                .setOwnerUserId(optUserId)
                .setOwnerDeptId(optUserDeptId)
                .setOwnerFavorite(FavoriteTypeEnum.NORMAL.code)
                .setFollowTime(new Date())
                .setVersion(existed.getVersion());
        this.updateCustomerInfoWithTraceInfo(toUpdate, existed, optUserId, false, Map.of("id", id));
    }

    @Override
    public int recycleTimeoutCustomers(int limit) {
        int batchLimit = limit <= 0 ? 200 : Math.min(limit, 1000);
        Date cutoff = SuDateUtils.add(new Date(), Calendar.HOUR_OF_DAY, -24);
        LambdaQueryWrapper<CustomerInfoItemDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.gt(CustomerInfoItemDO::getOwnerUserId, 0L)
                .and(qw -> qw.le(CustomerInfoItemDO::getFollowTime, cutoff)
                        .or(inner -> inner.isNull(CustomerInfoItemDO::getFollowTime)
                                .le(CustomerInfoItemDO::getApplyDate, cutoff)))
                .orderByAsc(CustomerInfoItemDO::getFollowTime)
                .last("limit " + batchLimit);
        List<CustomerInfoItemDO> itemList = customerManager.list(wrapper);
        if (CollectionUtils.isEmpty(itemList)) {
            return 0;
        }
        List<Pair<CustomerInfoItemDO, CustomerInfoItemDO>> list = itemList.stream()
                .map(item -> {
                    CustomerInfoItemDO toUpdate = new CustomerInfoItemDO()
                            .setId(item.getId())
                            .setOwnerFavorite(CommonConstant.NO)
                            .setOwnerUserId(0L)
                            .setVersion(item.getVersion());
                    return Pair.of(toUpdate, item);
                }).collect(Collectors.toList());
        this.updateCustomerInfoWithTraceInfo(list, 0L, false, Map.of("timeoutHours", 24, "limit", batchLimit));
        return list.size();
    }

    @Override
    public void switchFav4Customers(List<Long> idList, FavoriteTypeEnum favType, Long optUserId, Long optUserDeptId) {
        VerifyUtils.notNull(optUserId, "optUserId", "请指定操作人ID", true);
        idList = idList.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(idList)) {
            return;
        }
        // 查询归属人为 optUserId 的客户信息即可， 此处需要兼容转部门的数据，当前用户的部门不作为查询条件
        List<CustomerInfoItemDO> itemList = customerManager.getByIds(idList, List.of());
        VerifyUtils.judge(!CollectionUtils.isEmpty(itemList), ResultCode.CUST_NOT_FOUND, true,
                "idList:" + idList + ",optUserId:" + optUserId);
        // toUpdate - existed
        List<Pair<CustomerInfoItemDO, CustomerInfoItemDO>> list = itemList.stream()
                .map(item -> {
                    CustomerInfoItemDO toUpdate = new CustomerInfoItemDO()
                            .setId(item.getId())
                            .setOwnerFavorite(favType.code)
                            .setVersion(item.getVersion());
                    return Pair.of(toUpdate, item);
                }).collect(Collectors.toList());
        this.updateCustomerInfoWithTraceInfo(list, optUserId, false, Map.of("idList", idList));
    }

    @Override
    public void addLeaderRemark(AddLeaderRemarkRequest request, Long optUserId, Set<UserAuthRoleEnum> roles) {
        VerifyUtils.notNull(optUserId, "optUserId", "请指定操作人ID", true);
        request.validate();
        // 权限校验
        VerifyUtils.judge(
                roles.contains(UserAuthRoleEnum.ROLE_SUPPER)
                        || roles.contains(UserAuthRoleEnum.ROLE_DEPT_DATA_ADMIN),
                ResultCode.CUST_ADD_COMMENT_FAILED_OUT_AUTH, true,
                "request: " + GsonHelper.toJson(request) + ", current roles: " + roles);
        CustomerInfoItemDO existed = customerManager.getById(request.getId());
        VerifyUtils.judge(Objects.nonNull(existed), ResultCode.CUST_NOT_FOUND, true,
                "id:" + request.getId() + ",optUserId:" + optUserId);
        // 检查权限
        String userRealName = userService.getUserRealName(optUserId);
        LeaderRemarkDTO leaderRemarkDTO = LeaderRemarkDTO.of(userRealName, request.getLeaderRemark(),
                SuDateUtils.format(new Date(), SuDateUtils.DF_YYYY_MM_DDHHMMSS));
        CustomerInfoItemDO toUpdate = new CustomerInfoItemDO()
                .setId(request.getId())
                .setVersion(existed.getVersion())
                .setLeaderRemark(GsonHelper.toJson(leaderRemarkDTO, true));
        this.updateCustomerInfoWithTraceInfo(toUpdate, existed, optUserId, false, request);
    }

    /**
     * 根据用户权限，进行对应的分配客户信息操作。 <br>
     * 调用本方法的前提是， request 中的 userID, deptId 均已通过合法性校验
     *
     * @param request
     * @param optUserId
     * @param optUserDeptId
     * @param roles
     */
    private void dispatchCustomerWithRoles(DispatchCustomerInfoRequest request, Long optUserId, Long optUserDeptId,
            Set<UserAuthRoleEnum> roles) {
        // 权限校验
        VerifyUtils.judge(
                roles.contains(UserAuthRoleEnum.ROLE_SUPPER)
                        || roles.contains(UserAuthRoleEnum.ROLE_DEPT_DATA_ADMIN),
                ResultCode.CUST_DISPATCH_FAILED_OUT_AUTH, true,
                "request: " + GsonHelper.toJson(request) + ", current roles: " + roles);
        // 待更新对象 - 即有对象
        List<Pair<CustomerInfoItemDO, CustomerInfoItemDO>> list = this.makeupDispatchUpdatePairs(request, optUserId,
                optUserDeptId, roles);
        VerifyUtils.notEmpty(list, "List<Pair<CustomerInfoItemDO, CustomerInfoItemDO>> list",
                "没有需要分配的客户信息，或者待分配的客户信息已分配给目标用户", true);
        this.updateCustomerInfoWithTraceInfo(list, optUserId, false, request);
        notifyDispatch(list);
        recordManualDispatchCounts(list);
    }

    private void notifyDispatch(List<Pair<CustomerInfoItemDO, CustomerInfoItemDO>> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<CustomerInfoItemDO> items = new ArrayList<>();
        for (Pair<CustomerInfoItemDO, CustomerInfoItemDO> pair : list) {
            CustomerInfoItemDO toUpdate = pair.getFirst();
            CustomerInfoItemDO existed = pair.getSecond();
            if (toUpdate == null || existed == null) {
                continue;
            }
            Long custId = toUpdate.getId() != null ? toUpdate.getId() : existed.getId();
            if (custId == null) {
                continue;
            }
            Long ownerUserId = toUpdate.getOwnerUserId() != null
                    ? toUpdate.getOwnerUserId()
                    : existed.getOwnerUserId();
            Long ownerDeptId = toUpdate.getOwnerDeptId() != null
                    ? toUpdate.getOwnerDeptId()
                    : existed.getOwnerDeptId();
            Integer ownerFavorite = toUpdate.getOwnerFavorite() != null
                    ? toUpdate.getOwnerFavorite()
                    : existed.getOwnerFavorite();
            CustomerInfoItemDO item = new CustomerInfoItemDO()
                    .setId(custId)
                    .setName(existed.getName())
                    .setMobile(existed.getMobile())
                    .setIdCardNo(existed.getIdCardNo())
                    .setOwnerUserId(ownerUserId)
                    .setOwnerDeptId(ownerDeptId)
                    .setOwnerFavorite(ownerFavorite);
            items.add(item);
        }
        if (!items.isEmpty()) {
            custNoticeService.notifyAssign(items, "DISPATCH");
        }
    }

    private void recordManualDispatchCounts(List<Pair<CustomerInfoItemDO, CustomerInfoItemDO>> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        Map<Long, Integer> countMap = new HashMap<>();
        Map<Long, Long> deptMap = new HashMap<>();
        for (Pair<CustomerInfoItemDO, CustomerInfoItemDO> pair : list) {
            CustomerInfoItemDO toUpdate = pair.getFirst();
            if (toUpdate == null || toUpdate.getOwnerUserId() == null || toUpdate.getOwnerUserId() <= 0L) {
                continue;
            }
            countMap.merge(toUpdate.getOwnerUserId(), 1, Integer::sum);
            Long deptId = toUpdate.getOwnerDeptId();
            if (deptId == null) {
                CustomerInfoItemDO existed = pair.getSecond();
                deptId = existed != null ? existed.getOwnerDeptId() : null;
            }
            if (deptId != null) {
                deptMap.putIfAbsent(toUpdate.getOwnerUserId(), deptId);
            }
        }
        countMap.forEach((userId, count) -> {
            Long deptId = deptMap.get(userId);
            custDispatchService.recordManualAssignment(userId, deptId, count);
        });
    }

    /**
     * 构造 待更新对象 - 即有对象 配对
     *
     * @return
     */
    private List<Pair<CustomerInfoItemDO, CustomerInfoItemDO>> makeupDispatchUpdatePairs(
            DispatchCustomerInfoRequest request,
            Long optUserId, Long optUserDeptId, Set<UserAuthRoleEnum> roles) {
        Long ownerDeptId = OtherUtils.defaultIfNull(request.getOwnerDeptId(), 0L);
        Long ownerUserId = OtherUtils.defaultIfNull(request.getOwnerId(), 0L);
        List<Long> cids = request.getCids();
        Set<Long> allowedDeptIds = this.getAllowedDeptIds(optUserDeptId, roles);
        if (!roles.contains(UserAuthRoleEnum.ROLE_SUPPER)) {
            // 判断是否越权
            VerifyUtils.judge(allowedDeptIds.contains(request.getOwnerDeptId()) || request.getOwnerDeptId() == 0L,
                    ResultCode.CUST_DISPATCH_FAILED_OUT_AUTH, true,
                    "request: " + GsonHelper.toJson(request) + ", allowDeptIds: " + allowedDeptIds);
        }

        List<CustomerInfoItemDO> existedList = customerManager.getByIds(cids, allowedDeptIds);
        if (CollectionUtils.isEmpty(existedList)) {
            throw new BizException(ResultCode.CUST_NOT_FOUND,
                    "custIds: " + request.getCids() + ", optUserId:" + optUserId);
        }
        if (existedList.size() != cids.size()) {
            throw new BizException(ResultCode.CUST_BATCH_DISPATCH_FAILED_4_NOT_AUTH,
                    "custIds: " + request.getCids() + ", optUserId:" + optUserId);
        }
        Map<Long, CustomerInfoItemDO> existedMap = existedList.stream()
                .collect(Collectors.toMap(e -> e.getId(), e -> e));
        // 判断是否移入企业公海
        boolean moveBaseOcean = Objects.equals(ownerDeptId, 0L);
        // 判断是否需要构建跟进时间
        Date followTime = (Objects.nonNull(request.getOwnerId()) && request.getOwnerId() > 0L) ? new Date() : null;
        List<Pair<CustomerInfoItemDO, CustomerInfoItemDO>> list = new ArrayList<>(cids.size());
        for (Long cid : cids) {
            CustomerInfoItemDO existed = existedMap.get(cid);
            CustomerInfoItemDO toUpdate = new CustomerInfoItemDO()
                    .setId(cid)
                    .setOwnerFavorite(FavoriteTypeEnum.NORMAL.code)
                    .setVersion(existed.getVersion());
            if (moveBaseOcean) {
                // 表示投入企业公海， ownerId直接被无视
                toUpdate.setOwnerDeptId(0L)
                        .setOwnerUserId(0L)
                        .setOwnerFavorite(CommonConstant.NO);
                list.add(Pair.of(toUpdate, existed));
                continue;
            }
            // 若分配给新的人， 需要更新跟进时间，以保证用户后续展示的时候， 能将这个条记录往上移
            toUpdate.setOwnerDeptId(request.getOwnerDeptId())
                    .setOwnerUserId(request.getOwnerId())// ownerId 可能为 null， 表示不更新人，
                    .setFollowTime(followTime);
            if (!Objects.equals(existed.getOwnerDeptId(), request.getOwnerDeptId()) && existed.getOwnerUserId() > 0L) {
                // 把数据从原归属人剥离， 分配给新的部门时，必须指定新归属人 ID，当然， 新归属人也可以是0， 表示投入对应部门的公海
                VerifyUtils.notNull(request.getOwnerId(), "ownerId",
                        "对已存在跟进人的客户信息【" + existed.getName() + " - " + existed.getMobile()
                                + "】，更换部门时请指定分配的人员",
                        true);
            }
            if (Objects.equals(ownerUserId, existed.getOwnerUserId())
                    && Objects.equals(ownerDeptId, existed.getOwnerDeptId())) {
                // 分配信息没改变，踢掉
                continue;
            }
            list.add(Pair.of(toUpdate, existed));
        }
        return list;
    }

    /**
     * 检查用户与部门的对应关系
     *
     * @param userId
     * @param deptInfo
     */
    private void checkUserDept(Long userId, DeptInfo deptInfo) {
        VerifyUtils.notNull(deptInfo, "deptInfo", "请选择有效的部门信息", true);
        // 核对用户归属部门是否为指定的部门
        BriefUserInfoDTO user = userService.getUserInfo(userId);
        if (Objects.isNull(user)) {
            throw new BizException(ResultCode.USER_NOT_EXIST, "userId:" + userId);
        }
        VerifyUtils.verifyTrue(Objects.equals(user.getDepartmentId(), deptInfo.getId()),
                "指定的用户与部门不匹配，请确认后再操作", true);
        VerifyUtils.verifyTrue(Objects.equals(user.getOnlineStatus(), CommonConstant.YES),
                "指定的用户当前处于离线状态，不可分配客户数据", true);
    }

    /**
     * 检查 部门信息. 若部门 ID 为 null 或0视为不指定部门，直接返回 null
     *
     * @param deptId
     */
    private DeptInfo getValidDept(Long deptId) {
        if (Objects.isNull(deptId) || deptId == 0L) {
            return null;
        }
        DeptInfo info = deptService.getDeptInfoById(deptId, true);
        VerifyUtils.notNull(info, "ownerDeptId", "请选择有效的部门信息", true);
        return info;
    }

    /**
     * 检查业务字典 相关信息
     *
     * @param request
     */
    private void checkBizDict(AddCustomerInfoRequest request) {
        Map<Integer, BizDictItem> starGroup = bizConfigService
                .getBizDictItemMap(BizDictConfigTypeEnum.CUSTOMER_STAR_GROUP, false);
        Map<Integer, BizDictItem> channels = bizConfigService.getBizDictItemMap(BizDictConfigTypeEnum.DATA_CHANNEL,
                false);
        if (Objects.nonNull(request.getCustomerGroup())) {
            VerifyUtils.judge(starGroup.containsKey(request.getCustomerGroup()), "请选择正确的客户分组", true,
                    "customerGroup:" + request.getCustomerGroup());
        }
        if (Objects.nonNull(request.getChannel())) {
            VerifyUtils.judge(channels.containsKey(request.getChannel()), "请选择正确的数据来源渠道", true,
                    "channelId:" + request.getChannel());
        }

    }

    /**
     * 生成更新记录
     *
     * @param diffList  调用方保证 diffList 不为空
     * @param sourceId  客户信息 ID
     * @param optUserId
     * @return
     */
    private List<CustomerInfoItemTraceDO> makeupTraceDO(List<FieldDiffItemInfo> diffList, Long sourceId, Long optUserId,
            Date gmtCreate) {
        // 按更新类型分类
        Map<UpdTypeEnum, List<FieldDiffItemInfo>> updMap = diffList.stream()
                .collect(Collectors.groupingBy(
                        e -> CustomerUpdTraceStrategyContext.getFieldUpdTypeEnum(e.getFieldName())));
        List<CustomerInfoItemTraceDO> traceList = updMap.entrySet().stream()
                .map(entry -> {
                    List<FieldDiffItemInfo> innerItems = entry.getValue();
                    CustomerInfoItemTraceDO traceDO = new CustomerInfoItemTraceDO()
                            .setSourceId(sourceId)
                            .setUpdType(entry.getKey().name())
                            .setUpdFieldJson(GsonHelper.toJson(diffList))
                            .setOptUserId(optUserId)
                            .setOptUserRealName(userService.getUserRealName(optUserId))
                            .setUpdDispContentJson(StringUtils.EMPTY)
                            .setGmtCreate(gmtCreate);
                    if (Objects.equals(entry.getKey(), UpdTypeEnum.OTHER)) {
                        return traceDO;
                    }
                    List<String> descList = innerItems.stream()
                            .map(e -> CustomerUpdTraceStrategyContext.describeFiledChange(e, bizConfigService))
                            .filter(StringUtils::isNotBlank)
                            .collect(Collectors.toList());
                    traceDO.setUpdDispContentJson(GsonHelper.toJson(descList, true));
                    return traceDO;
                }).collect(Collectors.toList());
        return traceList;
    }

    /**
     * 根据权限取所有可用的部门 ID。 取出的部门 ID 列表中包含 参数 userDeptId 的值
     *
     * @param userDeptId
     * @param roles
     * @return
     */
    private Set<Long> getAllowedDeptIds(Long userDeptId, Set<UserAuthRoleEnum> roles) {
        if (roles.contains(UserAuthRoleEnum.ROLE_SUPPER)) {
            // 放行，不做调整;
            return ALLOW_ALL_DEPT_IDS;
        }
        if (roles.contains(UserAuthRoleEnum.ROLE_DEPT_DATA_ADMIN)) {
            return Set.of(userDeptId);
        }
        return Set.of(userDeptId);
    }

    /**
     * 根据用户权限角色，调整里查询条件，控制数据可见范围
     *
     * @param qry
     * @param optUserId  操作人用户 ID（调用方保证本参数不为 null）
     * @param userDeptId 操作人归属的部门 ID
     * @param roles      操作人的角色列表
     */
    private void adaptQryByRoles(CustomerInfoQry qry, Long optUserId, Long userDeptId, Set<UserAuthRoleEnum> roles) {
        userDeptId = OtherUtils.defaultIfNull(userDeptId, BizConstance.NONE_DEPT_ID);
        roles = OtherUtils.defaultIfNull(roles, Set.of());
        Collection<Long> deptIds = qry.getOwnerDeptIds();
        if (roles.contains(UserAuthRoleEnum.ROLE_SUPPER)) {
            // 放行，不做调整;
            return;
        }
        if (Boolean.TRUE.equals(qry.getPublicPoolOnly())) {
            qry.setOwnerUserId(0L);
            qry.setOwnerDeptIds(null);
            qry.setUserIdList(null);
            qry.setOwnerFavorite(null);
            qry.setSelfOnly(false);
            return;
        }
        Set<Long> allowedDeptIds = this.getAllowedDeptIds(userDeptId, roles);

        if (CollectionUtils.isEmpty(deptIds)) {
            // 设置数据部门范围
            if (roles.contains(UserAuthRoleEnum.ROLE_DEPT_DATA_ADMIN)) {
                qry.setOwnerDeptIds(allowedDeptIds);
                return;
            }
            // 其它情况， 仅看自己的数据
            qry.setOwnerUserId(optUserId);
            return;
        }
        // 已设置部门查询条件的情况
        // 直接移除越权的部门信息
        Iterator<Long> itr = deptIds.iterator();
        Long deptId;
        while (itr.hasNext()) {
            deptId = itr.next();
            if (!allowedDeptIds.contains(deptId)) {
                itr.remove();
            }
        }
        if (CollectionUtils.isEmpty(deptIds)) {
            qry.setOwnerDeptIds(Set.of(BizConstance.NONE_DEPT_ID));
        } else {
            qry.setOwnerDeptIds(deptIds);
        }
        if (roles.contains(UserAuthRoleEnum.ROLE_DEPT_DATA_ADMIN)) {
            // 可任意搭配数据归属人进行查询
            return;
        }
        qry.setOwnerUserId(optUserId);
    }

}
