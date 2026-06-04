package com.jhl.silver.union.biz.data;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhl.silver.union.biz.common.enums.ImportProcStatusEnum;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerImportRecordDO;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 导入客户信息记录的查询条件
 *
 * @author: qingren
 * @create_time: 2025/5/7
 */
@Data
@Accessors(chain = true)
public class CustImpRecQry {

    /**
     * 原始文件名
     */
    private String oriFileName;

    /**
     * 处理状态。参数业务枚举
     */
    private List<ImportProcStatusEnum> procStatusEnumList;

    /**
     * 操作人用户ID
     */
    private Long optUserId;

    /**
     * 操作人部门ID列表
     */
    private List<Long> optDeptIds;

    /**
     * true: 按 ID 倒序排列， false: 按ID 升序排列, null: 不按 ID 排序
     */
    private Boolean orderByIdDesc;

    public CustImpRecQry addDeptId(Long deptId) {
        if (Objects.isNull(deptId)) {
            return this;
        }
        if (Objects.isNull(this.optDeptIds)) {
            this.optDeptIds = new ArrayList<>();
        }
        this.optDeptIds.add(deptId);
        return this;
    }

    public CustImpRecQry addProcStatusEnum(ImportProcStatusEnum procStatusEnum) {
        if (Objects.isNull(procStatusEnum)) {
            return this;
        }
        if (Objects.isNull(this.procStatusEnumList)) {
            this.procStatusEnumList = new ArrayList<>();
        }
        this.procStatusEnumList.add(procStatusEnum);
        return this;
    }

    public LambdaQueryWrapper<CustomerImportRecordDO> toQueryWrapper() {
        LambdaQueryWrapper<CustomerImportRecordDO> wrapper = new LambdaQueryWrapper<>();
        if (!CollectionUtils.isEmpty(procStatusEnumList)) {
            List<String> list = procStatusEnumList.stream()
                    .map(ImportProcStatusEnum::name)
                    .collect(Collectors.toList());
            wrapper.in(CustomerImportRecordDO::getProcStatus, list);
        }
        wrapper.eq(StringUtils.isNotBlank(oriFileName), CustomerImportRecordDO::getOriFileName, oriFileName)
                .eq(Objects.nonNull(optUserId), CustomerImportRecordDO::getOptUserId, optUserId)
                .in(!CollectionUtils.isEmpty(optDeptIds), CustomerImportRecordDO::getOptDeptId, optDeptIds);
        if (Objects.nonNull(this.orderByIdDesc)) {
            if (this.orderByIdDesc) {
                wrapper.orderByDesc(CustomerImportRecordDO::getId);
            } else {
                wrapper.orderByAsc(CustomerImportRecordDO::getId);
            }
        }
        return wrapper;
    }

}
