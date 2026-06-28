/*
 * Crv.com.cn Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.jhl.silver.union.biz.config.db;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.annotation.DbType;
import com.github.pagehelper.PageInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liwei
 * @Date 19/1/18 上午10:53
 */
@Configuration
@MapperScan(basePackages = { DBConfiguration.PRIMARY_MAPPER_BASE_PACKAGE })
public class DBConfiguration {
    public static final String PRIMARY_MAPPER_BASE_PACKAGE = "com.jhl.silver.union.biz.*.dal.mapper";

    @Bean
    public PageInterceptor pageInterceptor() {
        return new PageInterceptor();
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        // 添加分页插件，保证 MyBatis-Plus Page 查询生成 limit/total SQL。
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
