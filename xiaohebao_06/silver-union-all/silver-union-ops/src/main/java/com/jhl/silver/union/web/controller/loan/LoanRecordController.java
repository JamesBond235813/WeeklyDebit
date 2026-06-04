package com.jhl.silver.union.web.controller.loan;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhl.silver.union.biz.common.ResultCode;
import com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerInfoItemDO;
import com.jhl.silver.union.biz.customer.manager.CustomerInfoItemManager;
import com.jhl.silver.union.biz.data.UserContext;
import com.jhl.silver.union.biz.loan.dal.entity.LoanRecordDO;
import com.jhl.silver.union.biz.loan.service.LoanRecordService;
import com.jhl.silver.union.commons.CommonResultCode;
import com.jhl.silver.union.commons.SuResult;
import com.jhl.silver.union.commons.exception.BizException;
import com.jhl.silver.union.commons.utils.SuResultUtils;
import com.jhl.silver.union.web.data.loan.LoanRecordPageRequest;
import com.jhl.silver.union.web.data.loan.LoanRecordSaveRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/biz/loan-record")
@Tag(name = "授信放款记录")
public class LoanRecordController {
    @Resource
    private LoanRecordService loanRecordService;
    @Resource
    private CustomerInfoItemManager customerInfoItemManager;

    @PostMapping("/page")
    @Operation(summary = "分页查询授信放款记录")
    public SuResult<IPage<LoanRecordDO>> pageList(@RequestBody LoanRecordPageRequest request) {
        request.autoFix();
        Page<LoanRecordDO> page = new Page<>(request.getPage(), request.getPageSize());
        LambdaQueryWrapper<LoanRecordDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(request.getCustomerId() != null, LoanRecordDO::getCustomerId, request.getCustomerId())
                .eq(request.getOwnerUserId() != null, LoanRecordDO::getOwnerUserId, request.getOwnerUserId())
                .eq(request.getOwnerDeptId() != null, LoanRecordDO::getOwnerDeptId, request.getOwnerDeptId())
                .eq(StringUtils.isNotBlank(request.getLoanStatus()), LoanRecordDO::getLoanStatus,
                        request.getLoanStatus())
                .orderByDesc(LoanRecordDO::getId);
        applyDataScope(wrapper);
        return SuResultUtils.successResult(loanRecordService.page(page, wrapper));
    }

    @PostMapping("/save")
    @Operation(summary = "保存授信放款记录")
    public SuResult<Boolean> save(@RequestBody LoanRecordSaveRequest request) {
        validateSaveRequest(request);
        CustomerInfoItemDO customer = customerInfoItemManager.getById(request.getCustomerId());
        if (customer == null) {
            throw new BizException(ResultCode.CUST_NOT_FOUND, "customerId:" + request.getCustomerId());
        }
        assertCustomerLoanAccess(customer);
        if (request.getId() != null) {
            LoanRecordDO existed = loanRecordService.getById(request.getId());
            if (existed == null) {
                throw new BizException(CommonResultCode.INVALID_PARAMS, "放款记录不存在");
            }
            assertLoanRecordAccess(existed);
        }
        LoanRecordDO record = new LoanRecordDO()
                .setId(request.getId())
                .setCustomerId(request.getCustomerId())
                .setOwnerUserId(customer.getOwnerUserId())
                .setOwnerDeptId(customer.getOwnerDeptId())
                .setLoanAmount(request.getLoanAmount())
                .setLoanTerm(request.getLoanTerm())
                .setServiceFeeRate(request.getServiceFeeRate())
                .setReceivableAmount(request.getReceivableAmount())
                .setRepaymentAmount(request.getRepaymentAmount() == null ? BigDecimal.ZERO
                        : request.getRepaymentAmount())
                .setLoanStatus(StringUtils.defaultIfBlank(request.getLoanStatus(), "DISBURSED"))
                .setRemark(request.getRemark());
        loanRecordService.saveOrUpdate(record);
        return SuResultUtils.successResult(true);
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "查询授信放款记录详情")
    public SuResult<LoanRecordDO> detail(@PathVariable Long id) {
        LoanRecordDO record = loanRecordService.getById(id);
        if (record != null) {
            assertLoanRecordAccess(record);
        }
        return SuResultUtils.successResult(record);
    }

    @GetMapping("/status-options")
    @Operation(summary = "放款状态选项")
    public SuResult<List<Map<String, String>>> statusOptions() {
        return SuResultUtils.successResult(List.of(
                Map.of("label", "已放款", "value", "DISBURSED"),
                Map.of("label", "回款中", "value", "REPAYING"),
                Map.of("label", "已部分回款", "value", "PARTIAL_REPAID"),
                Map.of("label", "回款完毕", "value", "REPAID"),
                Map.of("label", "逾期", "value", "OVERDUE")));
    }

    private void validateSaveRequest(LoanRecordSaveRequest request) {
        if (request == null || request.getCustomerId() == null) {
            throw new BizException(CommonResultCode.INVALID_PARAMS, "请选择客户");
        }
        if (request.getLoanAmount() == null || request.getLoanAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(CommonResultCode.INVALID_PARAMS, "请填写有效的放款金额");
        }
        if (request.getLoanTerm() == null || request.getLoanTerm() <= 0) {
            throw new BizException(CommonResultCode.INVALID_PARAMS, "请填写有效的期限");
        }
        if (request.getServiceFeeRate() == null || request.getServiceFeeRate().compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException(CommonResultCode.INVALID_PARAMS, "请填写有效的服务费费率");
        }
        if (request.getReceivableAmount() == null || request.getReceivableAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException(CommonResultCode.INVALID_PARAMS, "请填写有效的应回款金额");
        }
        if (request.getRepaymentAmount() != null && request.getRepaymentAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException(CommonResultCode.INVALID_PARAMS, "请填写有效的已回款金额");
        }
    }

    private void applyDataScope(LambdaQueryWrapper<LoanRecordDO> wrapper) {
        Set<UserAuthRoleEnum> roles = UserContext.getRoles();
        if (roles.contains(UserAuthRoleEnum.ROLE_SUPPER)) {
            return;
        }
        if (roles.contains(UserAuthRoleEnum.ROLE_DEPT_DATA_ADMIN)) {
            wrapper.eq(LoanRecordDO::getOwnerDeptId, UserContext.getDeptId());
            return;
        }
        wrapper.eq(LoanRecordDO::getOwnerUserId, UserContext.getUserId());
    }

    private void assertLoanRecordAccess(LoanRecordDO record) {
        Set<UserAuthRoleEnum> roles = UserContext.getRoles();
        if (roles.contains(UserAuthRoleEnum.ROLE_SUPPER)) {
            return;
        }
        if (roles.contains(UserAuthRoleEnum.ROLE_DEPT_DATA_ADMIN)
                && Objects.equals(record.getOwnerDeptId(), UserContext.getDeptId())) {
            return;
        }
        if (Objects.equals(record.getOwnerUserId(), UserContext.getUserId())) {
            return;
        }
        throw new BizException(ResultCode.SYS_NO_AUTH, "loanRecordId:" + record.getId());
    }

    private void assertCustomerLoanAccess(CustomerInfoItemDO customer) {
        Set<UserAuthRoleEnum> roles = UserContext.getRoles();
        if (roles.contains(UserAuthRoleEnum.ROLE_SUPPER)) {
            return;
        }
        if (roles.contains(UserAuthRoleEnum.ROLE_DEPT_DATA_ADMIN)
                && Objects.equals(customer.getOwnerDeptId(), UserContext.getDeptId())) {
            return;
        }
        if (Objects.equals(customer.getOwnerUserId(), UserContext.getUserId())) {
            return;
        }
        throw new BizException(ResultCode.SYS_NO_AUTH, "customerId:" + customer.getId());
    }
}
