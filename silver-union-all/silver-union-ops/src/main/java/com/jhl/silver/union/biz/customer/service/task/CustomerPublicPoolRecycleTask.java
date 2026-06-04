package com.jhl.silver.union.biz.customer.service.task;

import com.jhl.silver.union.biz.customer.service.CustomerInfoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomerPublicPoolRecycleTask {
    private static final int BATCH_LIMIT = 500;

    @Resource
    private CustomerInfoService customerInfoService;

    @Scheduled(cron = "0 */10 * * * ?")
    public void recycleTimeoutCustomers() {
        int count = customerInfoService.recycleTimeoutCustomers(BATCH_LIMIT);
        if (count > 0) {
            log.info("recycled timeout customers to public pool, count={}", count);
        }
    }
}
