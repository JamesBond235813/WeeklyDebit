package com.jhl.silver.union.biz.service;

import com.jhl.silver.union.SilverUnionOpsApplicationTests;
import com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum;
import com.jhl.silver.union.biz.risk.dal.entity.RiskControlReportDO;
import com.jhl.silver.union.biz.risk.service.RiskReportService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author: qingren
 * @create_time: 2026/3/31
 */
public class RiskReportServiceTest extends SilverUnionOpsApplicationTests {
    private static int IDX_NAME = 0;
    private static int IDX_PHONE = 1;
    private static int IDX_ID_NO = 2;
    @Resource
    private RiskReportService riskReportService;
    @Test
    void getReport() throws IOException {
        String path = "/Users/liwei/shared/projects/jhl/silver-union-all/silver-union-ops/test_input.txt";
        String output="/Users/liwei/shared/projects/jhl/silver-union-all/silver-union-ops/test_output.txt";
        List<String> lines = Files.readAllLines(Paths.get(path));
        int cnt = 0;
        Set<UserAuthRoleEnum> roles = new HashSet<>();
        roles.add(UserAuthRoleEnum.ROLE_SUPPER);
        List<RiskControlReportDO> rptList = new ArrayList<>();
        for (String line : lines) {
            if (StringUtils.isBlank(line)) {
                continue;
            }
            String [] arr = line.split(",");
            if (arr.length != 3) {
                continue;
            }
            RiskControlReportDO rptDO=riskReportService.getReport(arr[IDX_NAME],arr[IDX_ID_NO],arr[IDX_PHONE],2L,0L,roles );
            rptList.add(rptDO);
            cnt++;
        }
        System.out.println("===========> done: "+cnt);
    }
}
