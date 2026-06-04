package com.jhl.silver.union.biz.data;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

@Data
@Accessors(chain = true)
public class MobileAreaInfo {
    private String province;
    private String city;

    public String toDisplay() {
        String prefix = StringUtils.defaultString(province);
        String suffix = StringUtils.defaultString(city);
        return StringUtils.trimToEmpty(prefix + suffix);
    }
}
