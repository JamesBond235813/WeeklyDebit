package com.jhl.silver.union.web.controller.admin;

import com.jhl.silver.union.biz.common.ResultCode;
import com.jhl.silver.union.biz.common.enums.BizDictConfigTypeEnum;
import com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum;
import com.jhl.silver.union.biz.customer.service.BizConfigService;
import com.jhl.silver.union.biz.data.EnableRecvConfig;
import com.jhl.silver.union.biz.data.UserContext;
import com.jhl.silver.union.biz.sys.service.SysConfigService;
import com.jhl.silver.union.commons.SuResult;
import com.jhl.silver.union.commons.exception.BizException;
import com.jhl.silver.union.commons.utils.SuResultUtils;
import com.jhl.silver.union.web.data.admin.AddBizConfigRequest;
import com.jhl.silver.union.web.data.admin.UpdBizConfigRequest;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

/**
 * 业务字典维护类信息
 *
 * @author: qingren
 * @create_time: 2
 *               2025/3/29
 */
@RestController
@RequestMapping("/sys/biz-cnf")
@Tag(name = "系统管理类接口: 业务字典管理")
public class BizConfigController {
    @Resource
    private BizConfigService bizConfigService;
    @Resource
    private SysConfigService sysConfigService;

    /**
     * 新增业务字典信息
     */
    @PostMapping("/add-biz-config")
    @Operation(summary = "新增业务字典信息")
    public SuResult<Void> addBizConfig(@RequestBody AddBizConfigRequest request) {
        verifyCustomerFieldConfigAuth(request.getBizType());
        bizConfigService.addBizConfigInfo(request, UserContext.getUserId());
        return SuResultUtils.successResult();
    }

    /**
     * 更新业务字典信息
     */
    @PostMapping("/upd-biz-config")
    @Operation(summary = "更新业务字典信息。可更新状态、描述、显示文字、字典值")
    public SuResult<Void> updBizConfig(@RequestBody UpdBizConfigRequest request) {
        verifyCustomerFieldConfigAuth(request.getBizType());
        bizConfigService.updateBizConfigInfo(request, UserContext.getUserId());
        return SuResultUtils.successResult();
    }

    /**
     * 分页查询字典信息
     */
    @Operation(summary = "分页查询业务字典信息")
    @PostMapping("/page-list")
    public SuResult<List<com.jhl.silver.union.web.data.admin.BizDictItemAdmin>> pageList(
            @Parameter(description = "业务类型，见BizDictConfigTypeEnum") @RequestBody Map<String, Object> params) {
        String bizType = String.valueOf(params.getOrDefault("bizType", ""));
        boolean validOnly = Boolean.parseBoolean(String.valueOf(params.getOrDefault("validOnly", false)));
        verifyCustomerFieldConfigAuth(bizType);
        var typeEnum = BizDictConfigTypeEnum.findByName(bizType);
        var list = bizConfigService.listAdminDictItems(typeEnum, validOnly);
        return SuResultUtils.successResult(list);
    }

    /**
     * 批量排序调整
     */
    @Operation(summary = "批量调整字典项排序")
    @PostMapping("/reorder")
    public SuResult<Void> reorder(@RequestBody Map<String, Object> params) {
        // 为简单起见，前端可调用多次 upd 接口；如需高效可在 service 加批量更新
        @SuppressWarnings("unchecked")
        var items = (List<Map<String, Object>>) params.get("items");
        if (items != null) {
            for (Map<String, Object> item : items) {
                UpdBizConfigRequest req = new UpdBizConfigRequest();
                req.setId(Long.valueOf(String.valueOf(item.get("id"))));
                req.setBizType(String.valueOf(params.getOrDefault("bizType", "")));
                verifyCustomerFieldConfigAuth(req.getBizType());
                Object sortNo = item.get("sortNo");
                if (sortNo != null) {
                    try {
                        req.setSortNo(Integer.valueOf(String.valueOf(sortNo)));
                    } catch (Exception ignore) {
                        /* keep null if not a number */ }
                }
                bizConfigService.updateBizConfigInfo(req, UserContext.getUserId());
            }
        }
        return SuResultUtils.successResult();
    }

    /**
     * 获取接收上游流量开关配置
     */
    @PostMapping("/get-enable-recv")
    @Operation(summary = "获取接收上游流量开关配置")
    public SuResult<EnableRecvConfig> getEnableRecv() {
        return SuResultUtils.successResult(sysConfigService.getEnableRecvConfig());
    }

    /**
     * 更新接收上游流量开关配置
     */
    @PostMapping("/upd-enable-recv")
    @Operation(summary = "更新接收上游流量开关配置")
    public SuResult<Void> updateEnableRecv(@RequestBody EnableRecvConfig request) {
        if (!UserContext.hasAnyRole(UserAuthRoleEnum.ROLE_SUPPER)) {
            throw new BizException(ResultCode.SYS_NO_AUTH, "enable recv config");
        }
        sysConfigService.updateEnableRecvConfig(request, UserContext.getUserId());
        return SuResultUtils.successResult();
    }

    private void verifyCustomerFieldConfigAuth(String bizType) {
        if (BizDictConfigTypeEnum.CUSTOMER_LIST_FIELD.name().equalsIgnoreCase(String.valueOf(bizType))) {
            if (!UserContext.hasAnyRole(UserAuthRoleEnum.ROLE_SUPPER)) {
                throw new BizException(ResultCode.SYS_NO_AUTH, "customer list field config");
            }
        }
    }

}
