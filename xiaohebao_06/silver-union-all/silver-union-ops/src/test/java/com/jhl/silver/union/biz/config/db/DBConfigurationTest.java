package com.jhl.silver.union.biz.config.db;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DBConfigurationTest {

    @Test
    void mybatisPlusInterceptorIncludesPagination() {
        MybatisPlusInterceptor interceptor = new DBConfiguration().mybatisPlusInterceptor();

        boolean hasPagination = interceptor.getInterceptors().stream()
                .anyMatch(PaginationInnerInterceptor.class::isInstance);

        Assertions.assertTrue(hasPagination);
    }
}
