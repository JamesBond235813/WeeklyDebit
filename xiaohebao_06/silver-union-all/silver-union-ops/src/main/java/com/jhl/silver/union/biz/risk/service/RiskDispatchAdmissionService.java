package com.jhl.silver.union.biz.risk.service;

import com.jhl.silver.union.web.data.admin.PushCustInfoItem;

public interface RiskDispatchAdmissionService {

    boolean shouldAutoDispatch(PushCustInfoItem item);

    boolean shouldAutoDispatch(String name, String idCard, String phone, Integer upstreamZhimaCode);
}
