package com.jhl.silver.union.web.controller.customer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhl.silver.union.biz.common.enums.BizFlagEnum;
import com.jhl.silver.union.biz.common.enums.HouseFlagEnum;
import com.jhl.silver.union.biz.common.enums.PurchaseTypeEnum;
import com.jhl.silver.union.biz.common.utils.AesUtils;
import com.jhl.silver.union.biz.customer.dal.entity.CustPushRecordDO;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerInfoItemDO;
import com.jhl.silver.union.biz.customer.manager.CustomerInfoItemManager;
import com.jhl.silver.union.biz.customer.service.CustPushRecordService;
import com.jhl.silver.union.biz.customer.service.FeishuBotNotifyService;
import com.jhl.silver.union.biz.customer.service.HyyCollisionRemoteClient;
import com.jhl.silver.union.biz.customer.service.HyyValueTranslator;
import com.jhl.silver.union.biz.customer.service.ImportCustDataService;
import com.jhl.silver.union.biz.data.EnableRecvConfig;
import com.jhl.silver.union.biz.sys.service.SysConfigService;
import com.jhl.silver.union.commons.exception.BizException;
import com.jhl.silver.union.commons.gson.GsonHelper;
import com.jhl.silver.union.web.data.admin.PushCustInfoItem;
import com.jhl.silver.union.web.data.customer.hyy.HyyAccessCheckRequest;
import com.jhl.silver.union.web.data.customer.hyy.HyyAccessCheckResult;
import com.jhl.silver.union.web.data.customer.hyy.HyyCommonRequest;
import com.jhl.silver.union.web.data.customer.hyy.HyyCommonResponse;
import com.jhl.silver.union.web.data.customer.hyy.HyyPushRequest;
import io.swagger.v3.oas.annotations.Operation;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/hyy")
@RequiredArgsConstructor
@Slf4j
public class HyyDataController {
    private static final String HYY_AES_KEY = "6454A9AFCC693CAA";
    private static final String HYY_CHANNEL_NAME = "花易用";
    private static final int TYPE_ACCESS_CHECK = 0;
    private static final int TYPE_PUSH = 1;
    private static final int CODE_SUCCESS = 0;
    private static final int CODE_INVALID_PARAMS = 400;
    private static final int CODE_REFUSE = 700;
    private static final int CODE_DUPLICATED = 1001;
    private static final int CODE_DECRYPT_FAILED = 1501;
    private static final int CODE_SYSTEM_BUSY = 9999;

    private final SysConfigService sysConfigService;
    private final CustomerInfoItemManager customerInfoItemManager;
    private final CustPushRecordService custPushRecordService;
    private final ImportCustDataService importCustDataService;
    private final HyyCollisionRemoteClient hyyCollisionRemoteClient;
    private final FeishuBotNotifyService feishuBotNotifyService;

    @PostMapping("/access-check")
    @Operation(summary = "花易用八位撞库接口", description = "不校验用户登录信息")
    public HyyCommonResponse<HyyAccessCheckResult> accessCheck(@RequestBody HyyCommonRequest request) {
        if (!isRecvEnabled()) {
            return failed(CODE_REFUSE, "当前暂不接收数据");
        }
        CustPushRecordDO record = saveRecord(TYPE_ACCESS_CHECK, null, null, null, GsonHelper.toJson(request));
        String decrypted = null;
        Integer existedFlag = null;
        String phoneCode = null;
        String accessRequestId = null;
        try {
            decrypted = decrypt(request);
            HyyAccessCheckRequest bizReq = GsonHelper.fromJson(decrypted, HyyAccessCheckRequest.class);
            String errMsg = validateAccessCheck(bizReq);
            if (StringUtils.isNotBlank(errMsg)) {
                return failed(CODE_INVALID_PARAMS, errMsg);
            }
            phoneCode = bizReq.getPhoneCode();
            List<CustomerInfoItemDO> prefixMatches = listByPhoneCode(bizReq.getPhoneCode());
            Set<String> md5Set = new LinkedHashSet<>();
            prefixMatches.stream()
                    .map(CustomerInfoItemDO::getMobileMd5)
                    .filter(StringUtils::isNotBlank)
                    .forEach(md5Set::add);
            boolean localDuplicated = isDuplicatedCustomer(prefixMatches, bizReq.getNameMd5(), bizReq.getIdnoMd5());
            HyyCollisionRemoteClient.CollisionCheckResult remoteResult =
                    hyyCollisionRemoteClient.check(bizReq.getPhoneCode(), bizReq.getNameMd5(), bizReq.getIdnoMd5());
            if (CollectionUtils.isNotEmpty(remoteResult.getMd5List())) {
                remoteResult.getMd5List().stream()
                        .filter(StringUtils::isNotBlank)
                        .forEach(md5Set::add);
            }
            boolean duplicated = localDuplicated || remoteResult.isDuplicated();
            existedFlag = duplicated ? 1 : 0;
            if (duplicated) {
                feishuBotNotifyService.notifyHyyAccessCheckBlocked(bizReq.getPhoneCode(),
                        resolveCollisionCandidateCount(remoteResult, prefixMatches));
                return failed(CODE_DUPLICATED, "撞库失败，存在相同客户");
            }
            HyyAccessCheckResult result = buildAccessCheckResult(new ArrayList<>(md5Set));
            accessRequestId = result.getRequestId();
            return success(result, "撞库通过，可以进件");
        } catch (IllegalArgumentException | BizException e) {
            log.warn("hyy access-check decrypt/parse failed", e);
            return failed(CODE_DECRYPT_FAILED, "解密失败");
        } catch (Exception e) {
            log.error("hyy access-check failed", e);
            return failed(CODE_SYSTEM_BUSY, "系统繁忙，请稍后再试");
        } finally {
            if (record != null && record.getId() != null) {
                custPushRecordService.updateRecord(record.getId(), null, phoneCode, accessRequestId, decrypted,
                        existedFlag, null);
            }
        }
    }

    @PostMapping("/push")
    @Operation(summary = "花易用推送用户资料接口", description = "不校验用户登录信息")
    public HyyCommonResponse<Void> push(@RequestBody HyyCommonRequest request) {
        if (!isRecvEnabled()) {
            return failed(CODE_REFUSE, "当前暂不接收数据");
        }
        CustPushRecordDO record = saveRecord(TYPE_PUSH, null, null, null, GsonHelper.toJson(request));
        String decrypted = null;
        String custName = null;
        String mobile = null;
        String orderNo = null;
        HyyCommonResponse<Void> response = null;
        try {
            decrypted = decrypt(request);
            HyyPushRequest bizReq = GsonHelper.fromJson(decrypted, HyyPushRequest.class);
            String errMsg = validatePush(bizReq);
            if (StringUtils.isNotBlank(errMsg)) {
                response = failed(CODE_INVALID_PARAMS, errMsg);
                return response;
            }
            custName = bizReq.getName();
            mobile = bizReq.getPhone();
            orderNo = bizReq.getRequestId();
            if (!custPushRecordService.existsPassedAccessCheckOrder(HYY_CHANNEL_NAME, orderNo)) {
                response = failed(CODE_INVALID_PARAMS, "request_id未通过撞库或不存在");
                return response;
            }
            if (custPushRecordService.existsPushedOrder(HYY_CHANNEL_NAME, orderNo)) {
                response = failed(CODE_DUPLICATED, "request_id已推送，请勿重复提交");
                return response;
            }
            if (isMobileExisted(mobile)) {
                response = failed(CODE_DUPLICATED, "手机号已存在，请勿重复推送");
                return response;
            }
            PushCustInfoItem item = convertPushItem(bizReq);
            importCustDataService.addApiPushedCustInfo(List.of(item), 0L, 0L);
            updateHyyExtendedFields(bizReq);
            response = success(null, "success");
            return response;
        } catch (IllegalArgumentException | BizException e) {
            log.warn("hyy push decrypt/parse failed", e);
            response = failed(CODE_DECRYPT_FAILED, "解密失败");
            return response;
        } catch (Exception e) {
            log.error("hyy push failed", e);
            response = failed(CODE_SYSTEM_BUSY, "系统繁忙，请稍后再试");
            return response;
        } finally {
            if (record != null && record.getId() != null) {
                custPushRecordService.updateRecord(record.getId(), custName, mobile, orderNo, decrypted, null,
                        GsonHelper.toJson(response));
            }
        }
    }

    private boolean isRecvEnabled() {
        EnableRecvConfig config = sysConfigService.getEnableRecvConfig();
        return config == null || !Boolean.FALSE.equals(config.getEnable());
    }

    private String decrypt(HyyCommonRequest request) {
        if (request == null || StringUtils.isBlank(request.getData())) {
            throw new IllegalArgumentException("data is blank");
        }
        String decrypted = AesUtils.decryptWithAesECB(request.getData(), HYY_AES_KEY);
        if (StringUtils.isBlank(decrypted)) {
            throw new IllegalArgumentException("decrypted data is blank");
        }
        log.info("hyy decrypted req: {}", decrypted);
        return decrypted;
    }

    private String validateAccessCheck(HyyAccessCheckRequest request) {
        if (request == null) {
            return "请求参数为空";
        }
        if (!StringUtils.isNumeric(request.getPhoneCode()) || request.getPhoneCode().length() != 8) {
            return "phone_code必须为手机号前八位";
        }
        if (StringUtils.length(request.getNameMd5()) != 32) {
            return "name_md5不能为空且必须为32位";
        }
        if (request.getAge() == null || request.getAge() < 22 || request.getAge() > 60) {
            return "age必须在22到60之间";
        }
        return null;
    }

    private String validatePush(HyyPushRequest request) {
        if (request == null) {
            return "请求参数为空";
        }
        if (StringUtils.isBlank(request.getRequestId())) {
            return "request_id不能为空";
        }
        if (StringUtils.isBlank(request.getName())) {
            return "name不能为空";
        }
        if (!StringUtils.isNumeric(request.getPhone()) || request.getPhone().length() != 11) {
            return "phone必须为11位手机号";
        }
        return null;
    }

    private List<CustomerInfoItemDO> listByPhoneCode(String phoneCode) {
        LambdaQueryWrapper<CustomerInfoItemDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(CustomerInfoItemDO::getMobile, phoneCode);
        return customerInfoItemManager.list(wrapper);
    }

    private boolean isMobileExisted(String mobile) {
        if (StringUtils.isBlank(mobile)) {
            return false;
        }
        LambdaQueryWrapper<CustomerInfoItemDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerInfoItemDO::getMobile, StringUtils.trim(mobile));
        return customerInfoItemManager.count(wrapper) > 0;
    }

    private boolean isDuplicatedCustomer(List<CustomerInfoItemDO> matches, String nameMd5, String idnoMd5) {
        if (CollectionUtils.isEmpty(matches)) {
            return false;
        }
        String normalizedNameMd5 = StringUtils.lowerCase(StringUtils.trimToEmpty(nameMd5));
        String normalizedIdnoMd5 = StringUtils.lowerCase(StringUtils.trimToEmpty(idnoMd5));
        for (CustomerInfoItemDO item : matches) {
            if (StringUtils.isNotBlank(normalizedNameMd5)
                    && StringUtils.equals(normalizedNameMd5, md5Hex(item.getName()))) {
                return true;
            }
            if (StringUtils.isNotBlank(normalizedIdnoMd5)
                    && StringUtils.equals(normalizedIdnoMd5, md5Hex(item.getIdCardNo()))) {
                return true;
            }
        }
        return false;
    }

    private int resolveCollisionCandidateCount(HyyCollisionRemoteClient.CollisionCheckResult remoteResult,
            List<CustomerInfoItemDO> localMatches) {
        if (remoteResult != null && remoteResult.getMd5Count() > 0) {
            return remoteResult.getMd5Count();
        }
        if (remoteResult != null && CollectionUtils.isNotEmpty(remoteResult.getMd5List())) {
            return remoteResult.getMd5List().size();
        }
        return CollectionUtils.isEmpty(localMatches) ? 0 : localMatches.size();
    }

    private HyyAccessCheckResult buildAccessCheckResult(List<String> md5List) {
        List<HyyAccessCheckResult.ProductAgreement> agreements = new ArrayList<>();
        agreements.add(new HyyAccessCheckResult.ProductAgreement()
                .setName("客户授权协议")
                .setUrl(""));
        return new HyyAccessCheckResult()
                .setRequestId(UUID.randomUUID().toString().replace("-", ""))
                .setCompanyName("小荷包")
                .setProductName("小荷包授信服务")
                .setProductLogo("")
                .setProductLoan("1-10万")
                .setProductTerm("3-36期")
                .setProductRatio("以审批结果为准")
                .setProductAgreement(agreements)
                .setMd5List(md5List)
                .setPrice(2)
                .setPreUrl("");
    }

    private PushCustInfoItem convertPushItem(HyyPushRequest request) {
        return new PushCustInfoItem()
                .setName(request.getName())
                .setMobile(request.getPhone())
                .setIdCardNo(request.getIdno())
                .setChannelName(HYY_CHANNEL_NAME)
                .setReqLoanAmount(HyyValueTranslator.loanAmountUpperBound(request.getLoanAmount()))
                .setHouseFlagDescription(Objects.equals(request.getHouse(), 1) ? HouseFlagEnum.YES.desc
                        : Objects.equals(request.getHouse(), 2) ? HouseFlagEnum.NO.desc : HouseFlagEnum.UNKNOWN.desc)
                .setCarFlagDescription(toBizFlagDesc(request.getCar()))
                .setCarPurchaseTypeDescription(toCarPurchaseTypeDesc(request.getCarStatus()))
                .setCityName(request.getWorkingCity())
                .setSex(Objects.equals(request.getSex(), 1) ? 1 : Objects.equals(request.getSex(), 2) ? 2 : 0)
                .setAge(request.getAge())
                .setProvidentFlagDescription(toBizFlagDesc(request.getGjj()))
                .setSocialInsuranceFlagDescription(toBizFlagDesc(request.getShebao()))
                .setInsuranceFlagDescription(toBizFlagDesc(request.getInsurance()))
                .setUpstreamZhimaCode(request.getZhima())
                .setUserSource(StringUtils.trimToEmpty(request.getChannelId()));
    }

    private void updateHyyExtendedFields(HyyPushRequest request) {
        if (request == null || StringUtils.isBlank(request.getPhone())) {
            return;
        }
        LambdaQueryWrapper<CustomerInfoItemDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerInfoItemDO::getMobile, StringUtils.trim(request.getPhone()))
                .last("limit 1");
        CustomerInfoItemDO customer = customerInfoItemManager.getOne(wrapper);
        if (customer == null || customer.getId() == null) {
            return;
        }
        CustomerInfoItemDO toUpdate = new CustomerInfoItemDO()
                .setId(customer.getId())
                .setCustomerRemark("")
                .setZhimaScore(null)
                .setHyyHouseDesc(HyyValueTranslator.house(request.getHouse()))
                .setHyyCarDesc(HyyValueTranslator.car(request.getCar(), request.getCarPrice(), request.getCarStatus()))
                .setHyyProvidentDesc(HyyValueTranslator.socialSecurity(request.getGjj()))
                .setHyySocialInsuranceDesc(HyyValueTranslator.socialSecurity(request.getShebao()))
                .setHyyInsuranceDesc(HyyValueTranslator.insurance(request.getInsurance()))
                .setHyyOccupationDesc(HyyValueTranslator.occupation(request.getOccupation()))
                .setHyyOverdueDesc(HyyValueTranslator.overdue(request.getOverdue()))
                .setHyyZhimaDesc(HyyValueTranslator.zhima(request.getZhima()))
                .setHyyLoanAmountDesc(HyyValueTranslator.loanAmount(request.getLoanAmount()))
                .setHyyIp(StringUtils.trimToEmpty(request.getIp()))
                .setUserSource(StringUtils.trimToEmpty(request.getChannelId()));
        customerInfoItemManager.updateById(toUpdate);
    }

    private String toBizFlagDesc(Integer code) {
        if (Objects.equals(code, 1) || Objects.equals(code, 2)) {
            return BizFlagEnum.YES.desc;
        }
        if (Objects.equals(code, 3)) {
            return BizFlagEnum.NO.desc;
        }
        return BizFlagEnum.UNKNOWN.desc;
    }

    private String toCarPurchaseTypeDesc(Integer code) {
        if (Objects.equals(code, 1)) {
            return PurchaseTypeEnum.FULL_PAYMENT.desc;
        }
        if (Objects.equals(code, 2)) {
            return PurchaseTypeEnum.LOAN.desc;
        }
        return PurchaseTypeEnum.UNKNOWN.desc;
    }

    private CustPushRecordDO saveRecord(int type, String custName, String mobile, String orderNo, String receivedReq) {
        CustPushRecordDO record = new CustPushRecordDO()
                .setType(type)
                .setCustName(StringUtils.trimToEmpty(custName))
                .setMobile(StringUtils.trimToEmpty(mobile))
                .setOrderNo(StringUtils.trimToEmpty(orderNo))
                .setChannelName(HYY_CHANNEL_NAME)
                .setReceivedReq(receivedReq)
                .setRemark("");
        return custPushRecordService.saveRecord(record);
    }

    private <T> HyyCommonResponse<T> success(T data, String message) {
        return new HyyCommonResponse<T>()
                .setCode(CODE_SUCCESS)
                .setMessage(message)
                .setData(data);
    }

    private <T> HyyCommonResponse<T> failed(int code, String message) {
        return new HyyCommonResponse<T>()
                .setCode(code)
                .setMessage(message)
                .setData(null);
    }

    private String md5Hex(String input) {
        if (StringUtils.isBlank(input)) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
