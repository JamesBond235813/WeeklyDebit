package com.jhl.silver.union.biz.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jhl.silver.union.biz.order.dal.entity.BizOrderDO;
import com.jhl.silver.union.web.data.order.OrderCreateRequest;
import com.jhl.silver.union.web.data.order.OrderStatusUpdateRequest;

public interface BizOrderService extends IService<BizOrderDO> {

    /**
     * 创建订单
     * 
     * @param request
     * @param userId
     * @param deptId
     */
    void createOrder(OrderCreateRequest request, Long userId, Long deptId);

    /**
     * 更新订单状态
     * 
     * @param request
     * @param userId
     * @param userName
     */
    void updateOrderStatus(OrderStatusUpdateRequest request, Long userId, String userName);
}
