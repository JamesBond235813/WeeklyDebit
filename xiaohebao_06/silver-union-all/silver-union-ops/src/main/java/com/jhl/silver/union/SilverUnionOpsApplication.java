package com.jhl.silver.union;

import com.jhl.silver.union.commons.exception.BizException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.jhl.silver.union")
@EnableScheduling
public class SilverUnionOpsApplication {

    public static void main(String[] args) {
        BizException.BIZ_CODE = "OP001";
        SpringApplication.run(SilverUnionOpsApplication.class, args);
    }
}
