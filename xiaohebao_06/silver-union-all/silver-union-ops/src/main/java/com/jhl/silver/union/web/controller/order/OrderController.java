package com.jhl.silver.union.web.controller.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhl.silver.union.biz.common.enums.OrderStatusEnum;
import com.jhl.silver.union.biz.data.UserContext;
import com.jhl.silver.union.biz.express.service.ExpressTrackingService;
import com.jhl.silver.union.biz.order.dal.entity.BizOrderDO;
import com.jhl.silver.union.biz.order.dal.entity.BizOrderTraceDO;
import com.jhl.silver.union.biz.order.dal.mapper.BizOrderTraceMapper;
import com.jhl.silver.union.biz.order.service.BizOrderService;
import com.jhl.silver.union.biz.user.service.UserService;
import com.jhl.silver.union.commons.utils.SuDateUtils;
import com.jhl.silver.union.commons.SuResult;
import com.jhl.silver.union.web.data.order.OrderCreateRequest;
import com.jhl.silver.union.web.data.order.OrderPageRequest;
import com.jhl.silver.union.web.data.order.OrderStatusUpdateRequest;
import com.jhl.silver.union.web.data.order.ExpressTrackResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/biz/order")
@Tag(name = "业务订单管理")
public class OrderController {

    @Resource
    private BizOrderService bizOrderService;
    @Resource
    private BizOrderTraceMapper bizOrderTraceMapper;
    @Resource
    private com.jhl.silver.union.biz.customer.manager.CustomerInfoItemManager customerInfoItemManager;
    @Resource
    private com.jhl.silver.union.biz.platform.service.BizPlatformService bizPlatformService;
    @Resource
    private UserService userService;
    @Resource
    private ExpressTrackingService expressTrackingService;

    @PostMapping("/create")
    @Operation(summary = "创建订单")
    public SuResult<Boolean> createOrder(@RequestBody OrderCreateRequest request) {
        bizOrderService.createOrder(request, UserContext.getUserId(), UserContext.getDeptId());
        return new SuResult<Boolean>().setCode(0).setData(true);
    }

    @PostMapping("/update-status")
    @Operation(summary = "更新订单状态")
    public SuResult<Boolean> updateStatus(@RequestBody OrderStatusUpdateRequest request) {
        String userName = UserContext.getUserInfo() != null ? UserContext.getUserInfo().getRealName() : "Unknown";
        bizOrderService.updateOrderStatus(request, UserContext.getUserId(), userName);
        return new SuResult<Boolean>().setCode(0).setData(true);
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询订单")
    public SuResult<IPage<com.jhl.silver.union.web.data.order.OrderListVO>> pageList(
            @RequestBody OrderPageRequest request) {
        Page<BizOrderDO> page = new Page<>(request.getPage(), request.getPageSize());
        LambdaQueryWrapper<BizOrderDO> wrapper = new LambdaQueryWrapper<>();

        if (!CollectionUtils.isEmpty(request.getStatusList())) {
            wrapper.in(BizOrderDO::getStatus, request.getStatusList());
        } else if (StringUtils.isNotBlank(request.getStatus())) {
            wrapper.eq(BizOrderDO::getStatus, request.getStatus());
        }
        if (request.getOwnerUserId() != null) {
            wrapper.eq(BizOrderDO::getOwnerUserId, request.getOwnerUserId());
        }
        if (request.getOwnerDeptId() != null) {
            wrapper.eq(BizOrderDO::getOwnerDeptId, request.getOwnerDeptId());
        }
        Date orderTimeStart = parseDate(request.getOrderTimeStart());
        Date orderTimeEnd = parseDate(request.getOrderTimeEnd());
        if (orderTimeStart != null) {
            wrapper.ge(BizOrderDO::getGmtCreate, orderTimeStart);
        }
        if (orderTimeEnd != null) {
            wrapper.le(BizOrderDO::getGmtCreate, orderTimeEnd);
        }

        wrapper.orderByDesc(BizOrderDO::getId);
        IPage<BizOrderDO> orderPage = bizOrderService.page(page, wrapper);

        // Convert to VO with joined names
        List<com.jhl.silver.union.web.data.order.OrderListVO> voList = orderPage.getRecords().stream().map(order -> {
            com.jhl.silver.union.web.data.order.OrderListVO vo = new com.jhl.silver.union.web.data.order.OrderListVO();
            // Copy basic fields
            vo.setId(order.getId());
            vo.setCustomerId(order.getCustomerId());
            vo.setPlatformId(order.getPlatformId());
            vo.setDeviceModel(order.getDeviceModel());
            vo.setDeviceQuantity(order.getDeviceQuantity());
            vo.setStatus(order.getStatus());
            vo.setDownPaymentAmount(order.getDownPaymentAmount());
            vo.setIsDownPaymentAdvanced(order.getIsDownPaymentAdvanced());
            vo.setRecyclePrice(order.getRecyclePrice());
            vo.setAgreedRecyclePrice(order.getAgreedRecyclePrice());
            vo.setChannelCommission(order.getChannelCommission());
            vo.setResalePrice(order.getResalePrice());
            vo.setGrossProfit(order.getGrossProfit());
            vo.setTrackingNoPlatform(order.getTrackingNoPlatform());
            vo.setTrackingNoCustomer(order.getTrackingNoCustomer());
            vo.setPlatformRecvProvince(order.getPlatformRecvProvince());
            vo.setPlatformRecvCity(order.getPlatformRecvCity());
            vo.setPlatformRecvDistrict(order.getPlatformRecvDistrict());
            vo.setPlatformRecvStreet(order.getPlatformRecvStreet());
            vo.setPlatformRecvDetail(order.getPlatformRecvDetail());
            vo.setSelfRecvProvince(order.getSelfRecvProvince());
            vo.setSelfRecvCity(order.getSelfRecvCity());
            vo.setSelfRecvDistrict(order.getSelfRecvDistrict());
            vo.setSelfRecvStreet(order.getSelfRecvStreet());
            vo.setSelfRecvDetail(order.getSelfRecvDetail());
            vo.setOwnerUserId(order.getOwnerUserId());
            vo.setOwnerDeptId(order.getOwnerDeptId());
            vo.setRemark(order.getRemark());
            vo.setGmtCreate(order.getGmtCreate() != null ? order.getGmtCreate().toString() : null);
            vo.setGmtModified(order.getGmtModified() != null ? order.getGmtModified().toString() : null);

            // Query and set customer name/mobile
            try {
                var customer = customerInfoItemManager.getById(order.getCustomerId());
                if (customer != null) {
                    vo.setCustomerName(customer.getName());
                    vo.setCustomerMobile(customer.getMobile());
                }
            } catch (Exception e) {
                // Ignore if customer not found
            }

            // Query and set platform name
            try {
                var platform = bizPlatformService.getById(order.getPlatformId());
                if (platform != null) {
                    vo.setPlatformName(platform.getName());
                }
            } catch (Exception e) {
                // Ignore if platform not found
            }

            return vo;
        }).collect(Collectors.toList());

        // Batch populate owner names
        java.util.Set<Long> ownerUserIds = voList.stream()
                .map(com.jhl.silver.union.web.data.order.OrderListVO::getOwnerUserId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        if (!ownerUserIds.isEmpty()) {
            Map<Long, String> userNames = userService.getUserRealNames(ownerUserIds);
            for (com.jhl.silver.union.web.data.order.OrderListVO vo : voList) {
                if (vo.getOwnerUserId() != null) {
                    vo.setOwnerUserName(userNames.get(vo.getOwnerUserId()));
                }
            }
        }

        // Create result page with VOs
        Page<com.jhl.silver.union.web.data.order.OrderListVO> voPage = new Page<>(orderPage.getCurrent(),
                orderPage.getSize(), orderPage.getTotal());
        voPage.setRecords(voList);

        return new SuResult<IPage<com.jhl.silver.union.web.data.order.OrderListVO>>().setCode(0).setData(voPage);
    }

    private static Date parseDate(String dateStr) {
        if (StringUtils.isBlank(dateStr)) {
            return null;
        }
        try {
            return SuDateUtils.parse(dateStr, SuDateUtils.DF_YYYY_MM_DDHHMMSS);
        } catch (ParseException e) {
            return null;
        }
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取订单详情(含日志)")
    public SuResult<Map<String, Object>> getDetail(@PathVariable Long id) {
        BizOrderDO order = bizOrderService.getById(id);
        if (order == null) {
            return new SuResult<Map<String, Object>>().setCode(1).setMsg("订单不存在");
        }

        List<BizOrderTraceDO> traces = bizOrderTraceMapper.selectList(
                new LambdaQueryWrapper<BizOrderTraceDO>()
                        .eq(BizOrderTraceDO::getOrderId, id)
                        .orderByDesc(BizOrderTraceDO::getId));

        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("traces", traces);

        return new SuResult<Map<String, Object>>().setCode(0).setData(result);
    }

    @GetMapping("/tracking")
    @Operation(summary = "查询物流轨迹")
    public SuResult<ExpressTrackResult> queryTracking(@RequestParam("trackingNo") String trackingNo) {
        return new SuResult<ExpressTrackResult>().setCode(0).setData(expressTrackingService.query(trackingNo));
    }

    @GetMapping("/status-options")
    @Operation(summary = "获取状态枚举")
    public SuResult<List<Map<String, String>>> getStatusOptions() {
        return new SuResult<List<Map<String, String>>>().setCode(0).setData(
                Arrays.stream(OrderStatusEnum.values())
                        .map(e -> {
                            Map<String, String> map = new HashMap<>();
                            map.put("value", e.name());
                            map.put("label", e.getDesc());
                            return map;
                        })
                        .collect(Collectors.toList()));
    }
}
