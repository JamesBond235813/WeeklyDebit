package com.jhl.silver.union.biz.common.enums;

import com.jhl.silver.union.commons.utils.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * 文件处理状态
 *
 * @author: qingren
 * @create_time: 2025/5/5
 */
public enum ImportProcStatusEnum {
    INIT("等待处理"),
    VALIDATING("正在校验文件"),
    IMPORTING("正在导入文件内容"),
    SUCC("导入完成"),
    FAILED("导入失败");

    private static final Map<String, ImportProcStatusEnum> repo =
            EnumUtils.enum2Map(ImportProcStatusEnum.values(), e -> e.name());

    public final String desc;

    ImportProcStatusEnum(String desc) {
        this.desc = desc;
    }

    public static ImportProcStatusEnum findByName(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        return repo.get(name.toUpperCase());
    }

}
