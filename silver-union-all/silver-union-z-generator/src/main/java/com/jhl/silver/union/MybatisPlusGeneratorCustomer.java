package com.jhl.silver.union;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * mybatis-plus 代码生成器
 *
 * @author: qingren
 * @create_time: 2021/10/22
 */
public class MybatisPlusGeneratorCustomer {
    /**
     * hostName:port
     */

    private static final String TARGET_MODULE_NAME = "silver-union-ops";
    private static final String TARGET_MODULE_PACKAGE = "com.jhl.silver.union.biz.customer";

    /**
     * 需要生成代码的表名
     */
    private static final List<String> tableList = Lists.newArrayList("customer_info_item", "customer_info_item_trace",
            "biz_dict_config", "customer_import_record", "cust_push_record");

    public static void main(String[] args) {
        GeneratorHelper.generateDalInfo(tableList, TARGET_MODULE_NAME, TARGET_MODULE_PACKAGE);
    }
}
