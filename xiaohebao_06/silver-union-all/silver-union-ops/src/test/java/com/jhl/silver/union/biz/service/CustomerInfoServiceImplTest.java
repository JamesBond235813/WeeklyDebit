package com.jhl.silver.union.biz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jhl.silver.union.SilverUnionOpsApplicationTests;
import com.jhl.silver.union.biz.common.ResultCode;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerInfoItemDO;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerInfoItemTraceDO;
import com.jhl.silver.union.biz.customer.manager.CustomerInfoItemManager;
import com.jhl.silver.union.biz.customer.manager.CustomerInfoItemTraceManager;
import com.jhl.silver.union.biz.customer.service.impl.CustomerInfoServiceImpl;
import com.jhl.silver.union.biz.data.UserContext;
import com.jhl.silver.union.biz.user.dal.entity.SuUserInfoDO;
import com.jhl.silver.union.biz.user.manager.SuUserInfoManager;
import com.jhl.silver.union.commons.CommonConstant;
import com.jhl.silver.union.commons.exception.BizException;
import com.jhl.silver.union.commons.exception.ExceptionLogPrinter;
import com.jhl.silver.union.commons.utils.SuDateUtils;
import com.jhl.silver.union.web.data.admin.AddCustomerInfoRequest;
import com.jhl.silver.union.web.data.admin.DispatchCustomerInfoRequest;
import com.jhl.silver.union.web.data.admin.UpdCustomerInfoRequest;
import com.jhl.silver.union.web.data.customer.UpdateBizCustomerInfoRequest;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author: qingren
 * @create_time: 2025/4/1
 */
@Slf4j
class CustomerInfoServiceImplTest extends SilverUnionOpsApplicationTests {

    @Resource
    private CustomerInfoServiceImpl customerInfoService;
    @Resource
    private CustomerInfoItemTraceManager traceManager;
    @Resource
    private SuUserInfoManager suUserInfoManager;
    @Resource
    private CustomerInfoItemManager itemManager;

    @Test
    void updateCustomerBizInfo() {
        Long id = 4L;
        Long userId = 8L;
        UpdateBizCustomerInfoRequest request = new UpdateBizCustomerInfoRequest()
                .setId(id)
                // .setHouseFlag(1)
                .setHouseVal(1300)
                .setSex(1)
                .setProvidentFlag(1)
                .setProvidentAmountYuan(3000)
                // .setProgress(5)
                // .setFollowRemark("这是跟进备注3")
                .setVersion(3L);
        try {
            customerInfoService.updateCustomerBizInfo(request, userId, Set.of());
        } catch (Exception e) {
            ExceptionLogPrinter.printExceptionLog(e, log);
            throw e;
        }
    }

    @Test
    @NewSpan
    void dispatchCustomer() throws InterruptedException {
        // 超管
        Long superUserId = 1L;
        // 部门数据管理员
        Long deptDataAdminUserId = 6L;
        Long basicUserId = 5L;
        Long basicUserId2 = 7L;

        Long deptId1 = 6L;
        Long deptId2 = 7L;
        Long deptId3 = 23L;

        DispatchCustomerInfoRequest request = new DispatchCustomerInfoRequest();
        // 1. 超管可以随意指派部门+部门员工
        Long customerId = 12L;
        List<Long> customerIds=List.of(customerId,11L);
        request.setCids(customerIds)
                .setOwnerDeptId(deptId2)
                .setOwnerId(basicUserId);
        SuUserInfoDO superUser = suUserInfoManager.queryUserById(superUserId);
        UserContext.setUserInfo(superUser);
        CustomerInfoItemDO item = itemManager.getById(customerId);
        CustomerInfoItemDO toUpdate = new CustomerInfoItemDO()
                .setId(item.getId())
                .setVersion(item.getVersion())
                .setOwnerUserId(0L)
                .setOwnerDeptId(0L)
                .setOwnerFavorite(CommonConstant.YES);
        itemManager.updateById(toUpdate);
        Date now = new Date();
        TimeUnit.SECONDS.sleep(1);
        customerInfoService.dispatchCustomer(request, UserContext.getUserId(), UserContext.getDeptId(),
                UserContext.getRoles());
        item = itemManager.getById(customerId);
        Assertions.assertNotNull(item);
        Assertions.assertEquals(item.getOwnerDeptId(), request.getOwnerDeptId());
        Assertions.assertEquals(item.getOwnerUserId(), request.getOwnerId());
        Assertions.assertEquals(item.getOwnerFavorite(), CommonConstant.NO);
        Assertions.assertNotNull(item.getFollowTime());
        Assertions.assertTrue(item.getFollowTime().getTime() > now.getTime());
        Date followTime = item.getFollowTime();
        // 2.超管可以仅指派到其它部门，员工清零,跟进时间不变
        request = new DispatchCustomerInfoRequest()
                .setCids(customerIds)
                .setOwnerDeptId(deptId1)
                .setOwnerId(0L)
        ;
        TimeUnit.SECONDS.sleep(1);
        customerInfoService.dispatchCustomer(request, UserContext.getUserId(), UserContext.getDeptId(),
                UserContext.getRoles());
        item = itemManager.getById(customerId);
        Assertions.assertNotNull(item);
        Assertions.assertEquals(item.getOwnerDeptId(), request.getOwnerDeptId());
        Assertions.assertEquals(item.getOwnerUserId(), 0L);
        Assertions.assertEquals(item.getOwnerFavorite(), CommonConstant.NO);
        Assertions.assertNotNull(item.getFollowTime());
        // 跟进时间不变
        Assertions.assertEquals(item.getFollowTime().getTime(), followTime.getTime());

        // 3.部门管理员可以指派给部门以及下属部门
        request = new DispatchCustomerInfoRequest()
                .setCids(customerIds)
                .setOwnerDeptId(deptId2)
                .setOwnerId(basicUserId);
        customerInfoService.dispatchCustomer(request, UserContext.getUserId(), UserContext.getDeptId(),
                UserContext.getRoles());
        superUser = suUserInfoManager.queryUserById(deptDataAdminUserId);
        UserContext.setUserInfo(superUser);
        request = new DispatchCustomerInfoRequest()
                .setCids(customerIds)
                .setOwnerDeptId(deptId3)
                .setOwnerId(basicUserId2);
        now = new Date();
        TimeUnit.SECONDS.sleep(1);
        customerInfoService.dispatchCustomer(request, UserContext.getUserId(), UserContext.getDeptId(),
                UserContext.getRoles());
        item = itemManager.getById(customerId);
        Assertions.assertNotNull(item);
        Assertions.assertEquals(item.getOwnerDeptId(), request.getOwnerDeptId());
        Assertions.assertEquals(item.getOwnerUserId(), request.getOwnerId());
        Assertions.assertEquals(item.getOwnerFavorite(), CommonConstant.NO);
        Assertions.assertNotNull(item.getFollowTime());
        Assertions.assertTrue(item.getFollowTime().getTime() > now.getTime());
        followTime = item.getFollowTime();
        // 4. 部门管理员可以把数据投海
        request = new DispatchCustomerInfoRequest()
                .setCids(customerIds)
                .setOwnerDeptId(deptId1)
                .setOwnerId(0L)
        ;
        TimeUnit.SECONDS.sleep(1);
        customerInfoService.dispatchCustomer(request, UserContext.getUserId(), UserContext.getDeptId(),
                UserContext.getRoles());
        item = itemManager.getById(customerId);
        Assertions.assertNotNull(item);
        Assertions.assertEquals(item.getOwnerDeptId(), request.getOwnerDeptId());
        Assertions.assertEquals(item.getOwnerUserId(), 0L);
        Assertions.assertEquals(item.getOwnerFavorite(), CommonConstant.NO);
        Assertions.assertNotNull(item.getFollowTime());
        // 跟进时间不变
        Assertions.assertEquals(item.getFollowTime().getTime(), followTime.getTime());
    }

    @Test
    void updateCustomerFactInfo() {
        Long optUserId = 1L;
        AddCustomerInfoRequest request = this.buildFullAddCustomerInfoRequest();
        request.setName("asdf");
        UpdCustomerInfoRequest updateRequest = this.buildFullUpdateCustomerInfoRequest2(null);
        LambdaQueryWrapper<CustomerInfoItemDO> qw = Wrappers.lambdaQuery();
        qw.eq(CustomerInfoItemDO::getName, updateRequest.getName())
                .eq(CustomerInfoItemDO::getMobile, updateRequest.getMobile());
        CustomerInfoItemDO existed = itemManager.getOne(qw);
        if (Objects.nonNull(existed)) {
            itemManager.removeById(existed.getId());
            LambdaQueryWrapper<CustomerInfoItemTraceDO> dqw = Wrappers.lambdaQuery();
            dqw.eq(CustomerInfoItemTraceDO::getSourceId, existed.getId());
            traceManager.remove(dqw);
        }
        Long id = customerInfoService.addCustomerFactInfo(request, optUserId);
        updateRequest.setId(id);
        customerInfoService.updateCustomerFactInfo(updateRequest, optUserId);
        CustomerInfoItemDO itemDO = itemManager.getById(id);
        Assertions.assertNotNull(itemDO);
        Assertions.assertNotNull(itemDO.getName(), updateRequest.getName());
        Assertions.assertNotNull(itemDO.getMobile(), updateRequest.getMobile());
        Assertions.assertEquals(itemDO.getName(), updateRequest.getName());
        Assertions.assertEquals(itemDO.getMobile(), updateRequest.getMobile());
        Assertions.assertEquals(itemDO.getMobileArea(), updateRequest.getMobileArea());
        Assertions.assertEquals(itemDO.getIdCardNo(), updateRequest.getIdCardNo());
        Assertions.assertEquals(itemDO.getSourceFileName(), updateRequest.getSourceFileName());
        Assertions.assertEquals(itemDO.getChannel(), updateRequest.getChannel());
        Assertions.assertEquals(itemDO.getOwnerDeptId(), updateRequest.getOwnerDeptId());
        Assertions.assertEquals(itemDO.getSex(), updateRequest.getSex());
        Assertions.assertEquals(itemDO.getAge(), updateRequest.getAge());
        Assertions.assertEquals(itemDO.getBirthday(), updateRequest.getBirthday());
        Assertions.assertEquals(itemDO.getReqLoanAmount(), updateRequest.getReqLoanAmount());
        Assertions.assertEquals(itemDO.getHouseFlag(), updateRequest.getHouseFlag());
        Assertions.assertEquals(itemDO.getInsuranceFlag(), updateRequest.getInsuranceFlag());
        Assertions.assertEquals(itemDO.getSocialInsuranceFlag(), updateRequest.getSocialInsuranceFlag());
        Assertions.assertEquals(itemDO.getCarFlag(), updateRequest.getCarFlag());
        Assertions.assertEquals(itemDO.getProvidentFlag(), updateRequest.getProvidentFlag());
        Assertions.assertEquals(itemDO.getCreditCardFlag(), updateRequest.getCreditCardFlag());
        Assertions.assertEquals(itemDO.getEnterpriseFlag(), updateRequest.getEnterpriseFlag());
        Assertions.assertEquals(itemDO.getMarriageStatus(), updateRequest.getMarriageStatus());
        Assertions.assertEquals(itemDO.getProvidentAmountYuan(), updateRequest.getProvidentAmountYuan());
        Assertions.assertEquals(itemDO.getHouseVal(), updateRequest.getHouseVal());
        Assertions.assertEquals(itemDO.getCustomerGroup(), updateRequest.getCustomerGroup());
        Assertions.assertEquals(SuDateUtils.format(itemDO.getApplyDate(), SuDateUtils.DF_YYYY_MM_DDHHMMSS),
                updateRequest.getApplyDateStr());
    }

    @Test
    void addCustomerFactInfo() {
        Long optUserId = 1L;
        // 仅姓名手机号
        AddCustomerInfoRequest request = new AddCustomerInfoRequest()
                .setName("测客1")
                .setMobile("13800010001");
        Long id;
        try {
            id = customerInfoService.addCustomerFactInfo(request, optUserId);
            Assertions.assertNotNull(id);
            CustomerInfoItemDO itemDO = itemManager.getById(id);
            Assertions.assertNotNull(itemDO);
            Assertions.assertNotNull(itemDO.getName(), request.getName());
            Assertions.assertNotNull(itemDO.getMobile(), request.getMobile());
        } catch (BizException e) {
            Assertions.assertEquals(e.getCode(), ResultCode.CUST_INFO_ALREADY_EXISTS.code);
            ExceptionLogPrinter.printExceptionLog(e, log);
        }
        request = buildFullAddCustomerInfoRequest();
        try {
            id = customerInfoService.addCustomerFactInfo(request, optUserId);
            Assertions.assertNotNull(id);
            CustomerInfoItemDO itemDO = itemManager.getById(id);
            Assertions.assertNotNull(itemDO);
            Assertions.assertNotNull(itemDO.getName(), request.getName());
            Assertions.assertNotNull(itemDO.getMobile(), request.getMobile());
            Assertions.assertEquals(itemDO.getName(), request.getName());
            Assertions.assertEquals(itemDO.getMobile(), request.getMobile());
            Assertions.assertEquals(itemDO.getMobileArea(), request.getMobileArea());
            Assertions.assertEquals(itemDO.getIdCardNo(), request.getIdCardNo());
            Assertions.assertEquals(itemDO.getSourceFileName(), request.getSourceFileName());
            Assertions.assertEquals(itemDO.getChannel(), request.getChannel());
            Assertions.assertEquals(itemDO.getOwnerDeptId(), request.getOwnerDeptId());
            Assertions.assertEquals(itemDO.getSex(), request.getSex());
            Assertions.assertEquals(itemDO.getAge(), request.getAge());
            Assertions.assertEquals(itemDO.getBirthday(), request.getBirthday());
            Assertions.assertEquals(itemDO.getReqLoanAmount(), request.getReqLoanAmount());
            Assertions.assertEquals(itemDO.getHouseFlag(), request.getHouseFlag());
            Assertions.assertEquals(itemDO.getInsuranceFlag(), request.getInsuranceFlag());
            Assertions.assertEquals(itemDO.getSocialInsuranceFlag(), request.getSocialInsuranceFlag());
            Assertions.assertEquals(itemDO.getCarFlag(), request.getCarFlag());
            Assertions.assertEquals(itemDO.getProvidentFlag(), request.getProvidentFlag());
            Assertions.assertEquals(itemDO.getCreditCardFlag(), request.getCreditCardFlag());
            Assertions.assertEquals(itemDO.getEnterpriseFlag(), request.getEnterpriseFlag());
            Assertions.assertEquals(itemDO.getMarriageStatus(), request.getMarriageStatus());
            Assertions.assertEquals(itemDO.getProvidentAmountYuan(), request.getProvidentAmountYuan());
            Assertions.assertEquals(itemDO.getHouseVal(), request.getHouseVal());
            Assertions.assertEquals(itemDO.getCustomerGroup(), request.getCustomerGroup());
            Assertions.assertEquals(SuDateUtils.format(itemDO.getApplyDate(), SuDateUtils.DF_YYYY_MM_DDHHMMSS),
                    request.getApplyDateStr());
        } catch (BizException e) {
            Assertions.assertEquals(e.getCode(), ResultCode.CUST_INFO_ALREADY_EXISTS.code);
            ExceptionLogPrinter.printExceptionLog(e, log);
        }
    }

    private AddCustomerInfoRequest buildFullAddCustomerInfoRequest() {
        AddCustomerInfoRequest request = new AddCustomerInfoRequest()
                .setName("测客2")
                .setMobile("  13800010002")
                // 手机号归属地
                .setMobileArea("手机号归属地  ")
                // 身份证号
                .setIdCardNo("110102199012011234   ")
                // 数据来源文件名称
                .setSourceFileName("单元测试")
                // 推广渠道ID
                .setChannel(1)
                // 当前数据归属部门ID
                .setOwnerDeptId(6L)
                // 性别。 0:保密, 1:男, 2:女
                .setSex(1)
                // 年龄
                .setAge(23)
                // 出生日期。 yyyy-MM-dd 格式
                .setBirthday("1990-01-12")
                // 目标贷款金额，单位元
                .setReqLoanAmount(10)
                // 房产标识。0:未知; 1:有京房; 2:有房; 3:无
                .setHouseFlag(2)
                // 保单标识。0:未知; 1:有; 2:无
                .setInsuranceFlag(1)
                // 社保标识。0:未知; 1:有; 2:无
                .setSocialInsuranceFlag(1)
                // 车产标识。0:未知; 1:有; 2:无
                .setCarFlag(1)
                // 公积金标识。0:未知; 1:有; 2:无
                .setProvidentFlag(1)
                // 信用卡标识。0:未知; 1:有; 2:无
                .setCreditCardFlag(1)
                // 企业主标识。0:未知; 1:是; 2:否
                .setEnterpriseFlag(1)
                // 婚姻状态. 0:未知; 1:已婚; 2:未婚; 3:离异
                .setMarriageStatus(2)
                // 公积金金额， 单位元
                .setProvidentAmountYuan(1600)
                // 房产价值，单位万元
                .setHouseVal(300)
                // 客户分组，见业务字典定义
                .setCustomerGroup(2)
                // 申请时间（进本平台件的时间）
                .setApplyDateStr("2025-04-01 10:18:00");
        return request;
    }

    private UpdCustomerInfoRequest buildFullUpdateCustomerInfoRequest2(Long id) {
        UpdCustomerInfoRequest request = new UpdCustomerInfoRequest()
                .setId(id);
        request.setName("测客3")
                .setMobile("13800010004")
                // 手机号归属地
                .setMobileArea("手机号归属地2")
                // 身份证号
                .setIdCardNo("110102199012011235")
                // 数据来源文件名称
                .setSourceFileName("单元测试-更新")
                // 推广渠道ID
                .setChannel(2)
                // 当前数据归属部门ID
                .setOwnerDeptId(23L)
                // 性别。 0:保密, 1:男, 2:女
                .setSex(2)
                // 年龄
                .setAge(24)
                // 出生日期。 yyyy-MM-dd 格式
                .setBirthday("1990-01-13")
                // 目标贷款金额，单位元
                .setReqLoanAmount(10000)
                // 房产标识。0:未知; 1:有京房; 2:有房; 3:无
                .setHouseFlag(1)
                // 保单标识。0:未知; 1:有; 2:无
                .setInsuranceFlag(0)
                // 社保标识。0:未知; 1:有; 2:无
                .setSocialInsuranceFlag(0)
                // 车产标识。0:未知; 1:有; 2:无
                .setCarFlag(0)
                // 公积金标识。0:未知; 1:有; 2:无
                .setProvidentFlag(0)
                // 信用卡标识。0:未知; 1:有; 2:无
                .setCreditCardFlag(0)
                // 企业主标识。0:未知; 1:是; 2:否
                .setEnterpriseFlag(0)
                // 婚姻状态. 0:未知; 1:已婚; 2:未婚; 3:离异
                .setMarriageStatus(3)
                // 公积金金额， 单位元
                .setProvidentAmountYuan(1600)
                // 房产价值，单位万元
                .setHouseVal(1300)
                // 客户分组，见业务字典定义
                .setCustomerGroup(3)
                // 申请时间（进本平台件的时间）
                .setApplyDateStr("2025-04-02 10:18:00");
        return request;
    }
}