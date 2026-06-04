package com.jhl.silver.union.biz.data;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 部门组织架构树节点信息
 *
 * @author: qingren
 * @create_time: 2025/3/21
 */
@Data
@Accessors(chain = true)
@lombok.EqualsAndHashCode(callSuper = true)
public class DeptNodeInfo extends DeptInfo {

    /**
     * 上级部门信息
     */
    private DeptNodeInfo parentDeptNodeInfo;

    /**
     * 下级部门列表
     */
    private List<DeptNodeInfo> children;
}
