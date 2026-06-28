package com.jhl.silver.union.biz.customer.service;

import com.jhl.silver.union.biz.customer.dal.entity.CustomerInfoItemDO;
import java.util.List;

public interface FeishuBotNotifyService {

    void notifyApiInsertedCustomers(List<CustomerInfoItemDO> insertedCustomers);

    void notifyHyyAccessCheckBlocked(String phoneCode, int candidateCount);
}
