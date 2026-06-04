package com.jhl.silver.union.biz.data;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 接收上游流量开关配置
 */
@Data
@Accessors(chain = true)
public class EnableRecvConfig {
    /**
     * 是否开启接收上游流量。true: 开启
     */
    private Boolean enable;
}
