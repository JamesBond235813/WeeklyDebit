package com.jhl.silver.union.biz.data;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class IdCardAreaInfo {
    private String province;
    private String city;
    private String district;
}
