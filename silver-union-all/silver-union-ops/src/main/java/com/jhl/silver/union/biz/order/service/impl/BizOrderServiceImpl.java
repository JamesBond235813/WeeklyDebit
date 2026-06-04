package com.jhl.silver.union.biz.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jhl.silver.union.biz.common.enums.BizDictConfigTypeEnum;
import com.jhl.silver.union.biz.common.enums.OrderStatusEnum;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerInfoItemDO;
import com.jhl.silver.union.biz.customer.manager.CustomerInfoItemManager;
import com.jhl.silver.union.biz.customer.service.BizConfigService;
import com.jhl.silver.union.biz.order.dal.entity.BizOrderDO;
import com.jhl.silver.union.biz.order.dal.entity.BizOrderTraceDO;
import com.jhl.silver.union.biz.order.dal.mapper.BizOrderMapper;
import com.jhl.silver.union.biz.order.dal.mapper.BizOrderTraceMapper;
import com.jhl.silver.union.biz.order.service.BizOrderService;
import com.jhl.silver.union.web.data.BizDictItem;
import com.jhl.silver.union.web.data.order.OrderCreateRequest;
import com.jhl.silver.union.web.data.order.OrderStatusUpdateRequest;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;

@lombok.extern.slf4j.Slf4j
@Service
public class BizOrderServiceImpl extends ServiceImpl<BizOrderMapper, BizOrderDO> implements BizOrderService {

    @Resource
    private BizOrderTraceMapper bizOrderTraceMapper;
    @Resource
    private CustomerInfoItemManager customerInfoItemManager;
    @Resource
    private BizConfigService bizConfigService;

    private static final Set<OrderStatusEnum> DEAL_STATUS_SET = EnumSet.of(
            OrderStatusEnum.FORWARDED,
            OrderStatusEnum.RECEIVED,
            OrderStatusEnum.PAID,
            OrderStatusEnum.RESOLD
    );

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(OrderCreateRequest request, Long userId, Long deptId) {
        BizOrderDO order = new BizOrderDO();
        BeanUtils.copyProperties(request, order);

        // Initial state
        order.setStatus(OrderStatusEnum.APPLIED.name());
        order.setOwnerUserId(userId);
        order.setOwnerDeptId(deptId);
        order.setGmtCreate(new Date());
        order.setGmtModified(new Date());

        // Default values
        if (order.getIsDownPaymentAdvanced() == null) {
            order.setIsDownPaymentAdvanced(false);
        }
        if (order.getDownPaymentAmount() == null) {
            order.setDownPaymentAmount(BigDecimal.ZERO);
        }
        if (order.getDeviceQuantity() == null) {
            order.setDeviceQuantity(1);
        }
        if (order.getAgreedRecyclePrice() == null) {
            order.setAgreedRecyclePrice(BigDecimal.ZERO);
        }
        if (order.getChannelCommission() == null) {
            order.setChannelCommission(BigDecimal.ZERO);
        }

        this.save(order);

        // Trace
        saveTrace(order.getId(), null, OrderStatusEnum.APPLIED.name(), userId, "系统自动记录", "创建订单");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(OrderStatusUpdateRequest request, Long userId, String userName) {
        BizOrderDO order = this.getById(request.getId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        String preStatus = order.getStatus();
        String currStatus = request.getTargetStatus();

        // Update fields based on status or request
        if (StringUtils.isNotBlank(request.getDeviceModel())) {
            order.setDeviceModel(request.getDeviceModel());
        }
        if (request.getDeviceQuantity() != null) {
            order.setDeviceQuantity(request.getDeviceQuantity());
        }
        if (request.getDownPaymentAmount() != null) {
            order.setDownPaymentAmount(request.getDownPaymentAmount());
        }
        if (request.getIsDownPaymentAdvanced() != null) {
            order.setIsDownPaymentAdvanced(request.getIsDownPaymentAdvanced());
        }
        if (request.getAgreedRecyclePrice() != null) {
            order.setAgreedRecyclePrice(request.getAgreedRecyclePrice());
        }
        if (request.getChannelCommission() != null) {
            order.setChannelCommission(request.getChannelCommission());
        }
        if (StringUtils.isNotBlank(request.getTrackingNoPlatform())) {
            order.setTrackingNoPlatform(request.getTrackingNoPlatform());
        }
        if (StringUtils.isNotBlank(request.getTrackingNoCustomer())) {
            order.setTrackingNoCustomer(request.getTrackingNoCustomer());
        }
        if (request.getPlatformRecvProvince() != null) {
            order.setPlatformRecvProvince(StringUtils.trim(request.getPlatformRecvProvince()));
        }
        if (request.getPlatformRecvCity() != null) {
            order.setPlatformRecvCity(StringUtils.trim(request.getPlatformRecvCity()));
        }
        if (request.getPlatformRecvDistrict() != null) {
            order.setPlatformRecvDistrict(StringUtils.trim(request.getPlatformRecvDistrict()));
        }
        if (request.getPlatformRecvStreet() != null) {
            order.setPlatformRecvStreet(StringUtils.trim(request.getPlatformRecvStreet()));
        }
        if (request.getPlatformRecvDetail() != null) {
            order.setPlatformRecvDetail(StringUtils.trim(request.getPlatformRecvDetail()));
        }
        if (request.getSelfRecvProvince() != null) {
            order.setSelfRecvProvince(StringUtils.trim(request.getSelfRecvProvince()));
        }
        if (request.getSelfRecvCity() != null) {
            order.setSelfRecvCity(StringUtils.trim(request.getSelfRecvCity()));
        }
        if (request.getSelfRecvDistrict() != null) {
            order.setSelfRecvDistrict(StringUtils.trim(request.getSelfRecvDistrict()));
        }
        if (request.getSelfRecvStreet() != null) {
            order.setSelfRecvStreet(StringUtils.trim(request.getSelfRecvStreet()));
        }
        if (request.getSelfRecvDetail() != null) {
            order.setSelfRecvDetail(StringUtils.trim(request.getSelfRecvDetail()));
        }
        if (request.getAgreedRecyclePrice() != null && request.getDownPaymentAmount() != null) {
            BigDecimal computed = request.getAgreedRecyclePrice().subtract(request.getDownPaymentAmount());
            if (computed.compareTo(BigDecimal.ZERO) < 0) {
                computed = BigDecimal.ZERO;
            }
            order.setRecyclePrice(computed);
        } else if (request.getRecyclePrice() != null) {
            order.setRecyclePrice(request.getRecyclePrice());
        }
        if (request.getResalePrice() != null) {
            order.setResalePrice(request.getResalePrice());
        }
        if (StringUtils.isNotBlank(request.getRemark())) {
            order.setRemark(StringUtils.trim(request.getRemark()));
        }

        // Handle specific status logic
        OrderStatusEnum targetEnum = null;
        try {
            targetEnum = OrderStatusEnum.valueOf(currStatus);
        } catch (IllegalArgumentException e) {
            // ignore
        }

        if (targetEnum == OrderStatusEnum.RESOLD && request.getResalePrice() != null) {
            // Calculate Profit
            // Profit = Resale - Recycle - (isAdvanced ? DownPayment : 0)
            BigDecimal cost = BigDecimal.ZERO;
            if (order.getRecyclePrice() != null) {
                cost = cost.add(order.getRecyclePrice());
            }
            if (Boolean.TRUE.equals(order.getIsDownPaymentAdvanced()) && order.getDownPaymentAmount() != null) {
                cost = cost.add(order.getDownPaymentAmount());
            }
            BigDecimal profit = request.getResalePrice().subtract(cost);
            order.setGrossProfit(profit);
        }

        order.setStatus(currStatus);
        order.setGmtModified(new Date());
        this.updateById(order);

        // Trace
        saveTrace(order.getId(), preStatus, currStatus, userId, userName, request.getRemark());
        syncCustomerProgressIfNeeded(order, targetEnum);
    }

    private void saveTrace(Long orderId, String preStatus, String currStatus, Long userId, String userName,
            String remark) {
        BizOrderTraceDO trace = new BizOrderTraceDO();
        trace.setOrderId(orderId);
        trace.setPreStatus(preStatus);
        trace.setCurrStatus(currStatus);
        trace.setOptUserId(userId);
        trace.setOptUserName(userName);
        trace.setRemark(remark);
        trace.setGmtCreate(new Date());
        bizOrderTraceMapper.insert(trace);
    }

    private void syncCustomerProgressIfNeeded(BizOrderDO order, OrderStatusEnum targetEnum) {
        if (order == null || order.getCustomerId() == null || targetEnum == null) {
            return;
        }
        if (!DEAL_STATUS_SET.contains(targetEnum)) {
            return;
        }
        BizDictItem progressItem = bizConfigService.getSingleBizDictItemByLabel(
                BizDictConfigTypeEnum.PROGRESS, "成交");
        if (progressItem == null) {
            log.warn("Progress config not found for label=成交");
            return;
        }
        CustomerInfoItemDO update = new CustomerInfoItemDO()
                .setId(order.getCustomerId())
                .setProgress(progressItem.getIntValue())
                .setFollowTime(new Date());
        customerInfoItemManager.updateById(update);
    }
}
