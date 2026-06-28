package com.jhl.silver.union.biz.customer.service;

import com.jhl.silver.union.web.data.CustomerItemDTO;
import com.jhl.silver.union.web.data.admin.AddRiskRegionRequest;
import com.jhl.silver.union.web.data.admin.RiskRegionItemDTO;

import java.util.List;

public interface CustRiskRegionService {
    String TYPE_RISK = "RISK";
    String TYPE_BLACK = "BLACK";

    List<RiskRegionItemDTO> list(String regionType);

    void add(AddRiskRegionRequest request, Long optUserId);

    void delete(Long id);

    void setupRiskFlags(List<CustomerItemDTO> customers);
}
