package com.jhl.silver.union;

import com.jhl.silver.union.commons.gson.GsonHelper;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class SilverUnionOpsApplicationTests {

    @Test
    @NewSpan("test")
    void contextLoads() {

        log.debug("===============>debug ");
        log.info("===============> info");
        log.warn("===============> warn");
        log.error("===============> error");
        log.info("===============> mdc: {}", GsonHelper.toJson(MDC.getCopyOfContextMap()));
    }

}
