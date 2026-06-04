package com.jhl.silver.union.biz.customer.service;

import com.jhl.silver.union.web.data.admin.DispatchPlanDTO;
import com.jhl.silver.union.web.data.admin.SaveDispatchPlanRequest;

import java.util.Optional;

/**
 * 数据分配方案与分配服务
 */
public interface CustDispatchService {
    DispatchPlanDTO getActivePlan();

    boolean isAutoModeActive();

    void saveDispatchPlan(SaveDispatchPlanRequest request, Long optUserId);

    Optional<DispatchUserResult> pickAutoAssignee(Long targetDeptId);

    void recordManualAssignment(Long userId, Long deptId, int count);

    class DispatchUserResult {
        private final Long userId;
        private final Long deptId;

        public DispatchUserResult(Long userId, Long deptId) {
            this.userId = userId;
            this.deptId = deptId;
        }

        public Long getUserId() {
            return userId;
        }

        public Long getDeptId() {
            return deptId;
        }
    }
}
