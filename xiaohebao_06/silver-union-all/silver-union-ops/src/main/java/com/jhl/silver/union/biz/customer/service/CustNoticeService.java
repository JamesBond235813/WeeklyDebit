package com.jhl.silver.union.biz.customer.service;

import com.jhl.silver.union.biz.customer.dal.entity.CustomerInfoItemDO;
import com.jhl.silver.union.web.data.customer.CustomerNoticeItemDTO;

import java.util.List;

/**
 * 客户通知服务
 */
public interface CustNoticeService {
    /**
     * 查询未读通知
     */
    List<CustomerNoticeItemDTO> listUnread(Long userId, int limit);

    /**
     * 标记通知已读
     */
    void markRead(Long userId, List<Long> ids);

    /**
     * 按客户ID标记通知已读
     */
    void markReadByCustomer(Long userId, Long custId);

    /**
     * 新增/更新类通知
     */
    void notifyNewAssignment(List<CustomerInfoItemDO> inserted, List<CustomerInfoItemDO> updated, String source);

    /**
     * 分配类通知
     */
    void notifyAssign(List<CustomerInfoItemDO> assigned, String source);
}
