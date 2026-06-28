package com.jhl.silver.union.biz.customer.service;

import com.jhl.silver.union.web.data.customer.CustomerFlowQueryRequest;
import com.jhl.silver.union.web.data.customer.CustomerFlowQueryResult;

public interface CustomerFlowQueryService {

    CustomerFlowQueryResult query(CustomerFlowQueryRequest request);
}
