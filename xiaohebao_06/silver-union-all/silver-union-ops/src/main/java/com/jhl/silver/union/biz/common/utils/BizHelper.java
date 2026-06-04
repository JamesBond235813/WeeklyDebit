package com.jhl.silver.union.biz.common.utils;

import com.jhl.silver.union.biz.data.DeptInfo;
import com.jhl.silver.union.biz.dept.dal.entity.SuOrgDepartmentInfoDO;
import com.jhl.silver.union.biz.user.dal.entity.SuUserInfoDO;
import com.jhl.silver.union.commons.utils.SuDateUtils;
import com.jhl.silver.union.commons.utils.VerifyUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 业务杂项辅助类
 *
 * @author: qingren
 * @create_time: 2025/3/27
 */
public abstract class BizHelper {
    private static java.util.regex.Pattern PATTERN_ALPHABET = java.util.regex.Pattern.compile(".*[a-zA-z]+.*");
    private static java.util.regex.Pattern PATTERN_DIGIT = java.util.regex.Pattern.compile(".*\\d+.*");

    /**
     * 校验密码有效性
     *
     * @param password
     * @param errMsg
     */
    public static void verifyPassword(String password, String errMsg) {
        //密码校验
        VerifyUtils.verifyTrue(
                PATTERN_DIGIT.matcher(password).matches() && PATTERN_ALPHABET.matcher(password).matches(),
                errMsg, true);
    }

    /**
     * 组装 用户ID - realName(userName) 的信息映射
     *
     * @return
     */
    public static final Map<Long/*userID*/, String/*realName+username*/> makeupUserNameMap(
            List<SuUserInfoDO> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return Collections.emptyMap();
        }
        Map<Long/*userID*/, String/*realName+username*/> userNameMap = userList.stream()
                .collect(Collectors.toMap(SuUserInfoDO::getId,
                        e -> e.getRealName() + "(" + e.getUserName() + ")",
                        (v1, v2) -> v2));
        return userNameMap;
    }

    /**
     * 组装 部门ID - 部门名称
     *
     * @param deptList
     * @return
     */
    public static Map<Long, String> makeupDeptIdName(List<SuOrgDepartmentInfoDO> deptList) {
        if (CollectionUtils.isEmpty(deptList)) {
            return Collections.emptyMap();
        }
        return deptList.stream()
                .collect(Collectors.toMap(SuOrgDepartmentInfoDO::getId, e -> e.getDeptName(), (v1, v2) -> v2));
    }

    /**
     * 组装 部门ID - 部门名称
     *
     * @param deptList
     * @return
     */
    public static Map<Long, String> makeupDeptInfoIdName(List<DeptInfo> deptList) {
        if (CollectionUtils.isEmpty(deptList)) {
            return Collections.emptyMap();
        }
        return deptList.stream()
                .collect(Collectors.toMap(DeptInfo::getId, e -> e.getDeptName(), (v1, v2) -> v2));
    }

    /**
     * 根据原文件名生成唯一文件名
     *
     * @param originalName
     * @param ext          扩展名。 格式为 .xxx
     * @return
     */
    public static String generateUniqueFileName(String originalName, String ext) {
        int index = originalName.lastIndexOf(".");
        String fileNameSuffix = RandomStringUtils.randomAlphanumeric(6).toLowerCase();
        ext = StringUtils.defaultIfBlank(ext, "");
        Long now = System.currentTimeMillis();
        String timestamp = SuDateUtils.format(new Date(now), "yyyyMMddHHmmssS");
        if (index == -1) {
            return originalName + "_" + timestamp + "_" + fileNameSuffix + ext;
        }
        if (StringUtils.isBlank(ext)) {
            ext = originalName.substring(index);
        }
        String oriFileNamePrefix = originalName.substring(0, index);
        return oriFileNamePrefix + "_" + timestamp + "_" + fileNameSuffix + ext;
    }
}
