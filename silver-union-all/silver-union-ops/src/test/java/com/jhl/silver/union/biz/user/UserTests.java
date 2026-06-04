package com.jhl.silver.union.biz.user;

import com.jhl.silver.union.biz.common.enums.SexEnum;
import com.jhl.silver.union.biz.common.enums.CommonStatusEnum;
import com.jhl.silver.union.biz.user.dal.entity.SuUserInfoDO;
import com.jhl.silver.union.biz.user.manager.SuUserInfoManager;
import com.jhl.silver.union.SilverUnionOpsApplicationTests;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Objects;

/**
 * @author: qingren
 * @create_time: 2025/3/18
 */
@Slf4j
public class UserTests extends SilverUnionOpsApplicationTests {
    @Resource
    private BCryptPasswordEncoder passwordEncoder;
    @Resource
    private SuUserInfoManager userInfoManager;

    @Test
    public void createUserTest() {
        SuUserInfoDO user = new SuUserInfoDO()
                .setUserName("qr8")
                .setPassword(passwordEncoder.encode("123456abc"))
                // 用户昵称，可重复
                .setNickName("小布8")
                // 真实姓名
                .setRealName("李小布8")
                // 用户手机号
                .setPhone("15080010004")
                // 用户邮箱
                .setEmail("123@qq.com")
                // 用户状态。 1：正常。 0：禁用
                .setStatus(CommonStatusEnum.OK.status)
                // 性别。 0:保密, 1:男, 2:女
                .setSex(SexEnum.MALE.sex)
                // 头像图片地址
                .setHeadImg(StringUtils.EMPTY)
                // 生日 yyyy-MM-dd格式
                .setBirthday(StringUtils.EMPTY)
                // 工号
                .setEmployeeNo("1")
                // 部门id
                .setDepartmentId(0L)
                // 职位
                .setJobName("测试");
        SuUserInfoDO existUser = userInfoManager.findUserByUsername(user.getUserName());
        if (Objects.nonNull(existUser)) {
            return;
        }
        userInfoManager.save(user);
    }

    @Test
    public void generatePassword() {
        // String password = RandomStringUtils.randomAlphanumeric(8);
        String password = "tkY!Vg8a6hTv";
        String enc = passwordEncoder.encode(password);
        log.info("===========> \npassword:\n{} \nenc:\n{}", password, enc);
    }
}
