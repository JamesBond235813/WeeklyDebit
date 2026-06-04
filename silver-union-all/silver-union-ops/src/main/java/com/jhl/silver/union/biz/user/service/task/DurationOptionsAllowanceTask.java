package com.jhl.silver.union.biz.user.service.task;

import com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum;
import com.jhl.silver.union.biz.common.utils.RoleUtils;
import com.jhl.silver.union.biz.config.BizProperty;
import com.jhl.silver.union.commons.utils.SuDateUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Set;

/**
 * 计算可操作时间的定时任务
 *
 * @author: qingren
 * @create_time: 2025/4/16
 */
@Component
@Slf4j
public class DurationOptionsAllowanceTask {
    private static boolean OPTIONS_ALLOW = true;
    private static Set<String> ALLOW_URIS = Set.of("/api/user/info","/api/auth/revoke");

    @Resource
    private BizProperty bizProperty;

    @PostConstruct
    public void executeNow() {
        this.execute();
    }

    /**
     * 每分钟计算一次，当前是否可操作
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void execute() {
        if (!bizProperty.isEnableLoginTimeLimit()) {
            return;
        }
        String time = SuDateUtils.format(new Date(), SuDateUtils.DF_HH_MM_SS);
        if (time.compareTo(bizProperty.getAllowLoginTimeBegin()) < 0
                || time.compareTo(bizProperty.getAllowLoginTimeEnd()) > 0) {
            OPTIONS_ALLOW = false;
            return;
        }
        OPTIONS_ALLOW = true;
    }

    /**
     * 判断当前时间段是否可进行操作
     *
     * @return
     */
    public static boolean isOptionsAllowed(Set<UserAuthRoleEnum> roles, String uri) {
        if (StringUtils.isNotBlank(uri) && ALLOW_URIS.contains(uri)) {
            return true;
        }
        return OPTIONS_ALLOW || RoleUtils.isSupper(roles);
    }

    /**
     * 判断当前时间段是否可进行操作
     *
     * @return
     */
    public static boolean isOptionsAllowed(String rolesStr) {
        return OPTIONS_ALLOW || RoleUtils.isSupper(rolesStr);
    }

}
