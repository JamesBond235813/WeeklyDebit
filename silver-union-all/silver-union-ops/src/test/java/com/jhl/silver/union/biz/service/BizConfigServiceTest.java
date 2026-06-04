package com.jhl.silver.union.biz.service;

import com.google.gson.reflect.TypeToken;
import com.jhl.silver.union.biz.common.ResultCode;
import com.jhl.silver.union.biz.common.enums.BizDictConfigTypeEnum;
import com.jhl.silver.union.biz.common.enums.CommonStatusEnum;
import com.jhl.silver.union.biz.customer.dal.entity.BizDictConfigDO;
import com.jhl.silver.union.biz.customer.manager.BizDictConfigManager;
import com.jhl.silver.union.biz.customer.service.BizConfigService;
import com.jhl.silver.union.commons.exception.BizException;
import com.jhl.silver.union.commons.exception.ExceptionLogPrinter;
import com.jhl.silver.union.commons.gson.GsonHelper;
import com.jhl.silver.union.SilverUnionOpsApplicationTests;
import com.jhl.silver.union.web.data.BizDictItem;
import com.jhl.silver.union.web.data.admin.AddBizConfigRequest;
import com.jhl.silver.union.web.data.admin.UpdBizConfigRequest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author: qingren
 * @create_time: 2025/3/29
 */
@Slf4j
class BizConfigServiceTest extends SilverUnionOpsApplicationTests {
    private static Long userId = 1L;
    @Resource
    private BizDictConfigManager bizDictConfigManager;
    @Resource
    private BizConfigService bizConfigService;

    @Test
    void getBizDictItems() {
        List<BizDictItem> list = bizConfigService.getBizDictItems(BizDictConfigTypeEnum.DATA_CHANNEL, true);
        log.info("=====> list: {}", GsonHelper.toJson(list));
        List<BizDictItem> allList = bizConfigService.getBizDictItems(BizDictConfigTypeEnum.DATA_CHANNEL, false);
        log.info("=====> allList: {}", GsonHelper.toJson(allList));
        Assertions.assertTrue(list.size() < allList.size());
    }

    @Test
    void getBizDictItemMap() {
        Map<Integer, BizDictItem> map =
                bizConfigService.getBizDictItemMap(BizDictConfigTypeEnum.DATA_CHANNEL, true);
        log.info("=====> map: {}", GsonHelper.toJson(map));
        Map<Integer, BizDictItem> allMap =
                bizConfigService.getBizDictItemMap(BizDictConfigTypeEnum.DATA_CHANNEL, false);
        log.info("=====> allMap: {}", GsonHelper.toJson(allMap));
        Assertions.assertTrue(map.size() < allMap.size());
    }

    @Test
    void initBizConfigData() {
        // 沟通进度
        String initDataStr = """
                [
                    {"bizType": "PROGRESS", "label": "待跟进", "intValue": 1},
                    {"bizType": "PROGRESS", "label": "捣乱人员/客户在外地", "intValue": 2},
                    {"bizType": "PROGRESS", "label": "邀约中", "intValue": 3},
                    {"bizType": "PROGRESS", "label": "意向客户", "intValue": 4},
                    {"bizType": "PROGRESS", "label": "已到店 无意签约", "intValue": 5},
                    {"bizType": "PROGRESS", "label": "已到店  犹豫中", "intValue": 6},
                    {"bizType": "PROGRESS", "label": "已签约", "intValue": 7},
                    {"bizType": "PROGRESS", "label": "贷款已下款，待回款", "intValue": 8},
                    {"bizType": "PROGRESS", "label": "在途待回款", "intValue": 9},
                    {"bizType": "PROGRESS", "label": "已回款，未完全回款", "intValue": 14078},
                    {"bizType": "PROGRESS", "label": "回款完毕", "intValue": 14079},
                    {"bizType": "CALL_RESULT_TIPS", "intValue": 0, "label": "未致电"},
                    {"bizType": "CALL_RESULT_TIPS", "intValue": 1, "label": "有意向"},
                    {"bizType": "CALL_RESULT_TIPS", "intValue": 2, "label": "无意向"},
                    {"bizType": "CALL_RESULT_TIPS", "intValue": 3, "label": "资料错误"},
                    {"bizType": "CALL_RESULT_TIPS", "intValue": 4, "label": "二次呼叫"},
                    {"bizType": "CALL_RESULT_TIPS", "intValue": 6, "label": "未接通"},
                    {"bizType": "CALL_RESULT_TIPS", "intValue": 7, "label": "未明确"},
                    {"bizType": "CALL_RESULT_TIPS", "intValue": 8, "label": "空号"},
                    {"bizType": "CALL_RESULT_TIPS", "intValue": 9, "label": "停机"},
                    {"bizType": "CALL_RESULT_TIPS", "intValue": 10, "label": "关机"},
                    {"bizType": "CALL_RESULT_TIPS", "intValue": 11, "label": "长响不接"},
                    {"bizType": "CALL_RESULT_TIPS", "intValue": 12, "label": "拒绝挂断"},
                    {"bizType": "CALL_RESULT_TIPS", "intValue": 13, "label": "接通-听贷挂断"},
                    {"bizType": "CALL_RESULT_TIPS", "intValue": 14, "label": "接通-稍后联系"},
                    {"bizType": "CALL_RESULT_TIPS", "intValue": 15, "label": "接通-无意向"},
                    {"bizType": "CALL_RESULT_TIPS", "intValue": 16, "label": "有意向-补充资料"},
                    {"bizType": "CUSTOMER_STAR_GROUP", "intValue": 0, "label": "未分组", "description": ""},
                    {"bizType": "CUSTOMER_STAR_GROUP", "intValue": 1, "label": "【零】", "description": "未核出资质"},
                    {"bizType": "CUSTOMER_STAR_GROUP", "intValue": 10, "label": "【☆】", "description": "同行捣乱/京外借款人"},
                    {"bizType": "CUSTOMER_STAR_GROUP", "intValue": 20, "label": "【☆☆】", "description": "刚需 | 核出户籍 公积金 资产等主要信息，但资质较差"},
                    {"bizType": "CUSTOMER_STAR_GROUP", "intValue": 25, "label": "【☆☆+】", "description": "0-700 公积金个缴"},
                    {"bizType": "CUSTOMER_STAR_GROUP", "intValue": 30, "label": "【☆☆☆】", "description": "700-1500 公积金个缴"},
                    {"bizType": "CUSTOMER_STAR_GROUP", "intValue": 40, "label": "【☆☆☆☆】", "description": "1500+ 公积金个缴"},
                    {"bizType": "CUSTOMER_STAR_GROUP", "intValue": 50, "label": "【☆☆☆☆☆】", "description": "自己名下或者父母名下有京房"},
                    {"bizType": "CUSTOMER_STAR_GROUP", "intValue": 99, "label": "$$重要客户$$", "description": "重要客户说明"},
                    {"bizType": "DATA_CHANNEL", "intValue": 10, "label": "渠道1 有意向"},
                    {"bizType": "DATA_CHANNEL", "intValue": 11, "label": "渠道1 要用款"},
                    {"bizType": "DATA_CHANNEL", "intValue": 20, "label": "渠道2"},
                    {"bizType": "DATA_CHANNEL", "intValue": 30, "label": "渠道3 有京房"},
                    {"bizType": "DATA_CHANNEL", "intValue": 40, "label": "渠道4"}
                ]""";
        //增加数据

        List<AddBizConfigRequest> requestList =
                GsonHelper.fromJson(initDataStr, new TypeToken<List<AddBizConfigRequest>>() {
                }.getType());
        for (AddBizConfigRequest request : requestList) {
            try {
                bizConfigService.addBizConfigInfo(request, userId);
            } catch (BizException e) {
                if (Objects.equals(e.getCode(), ResultCode.ADD_FAILED_FOR_DUPLICATED_BIZ_TYPE_VALUE.code)) {
                    ExceptionLogPrinter.printExceptionLog(e, log);
                }
            }
        }

    }

    @Test
    void curdTest() {

        String forbiddenData = """
                {"bizType": \"""" + BizDictConfigTypeEnum.PROGRESS.name() + """
                \", "label": "测试禁用状态", "intValue": 9999,status: 0}
                """;
        AddBizConfigRequest addRequest = GsonHelper.fromJson(forbiddenData, AddBizConfigRequest.class);
        Long id = bizConfigService.addBizConfigInfo(addRequest, userId);
        BizDictConfigDO config = bizDictConfigManager.getById(id);
        log.info("========> saved config: {}", GsonHelper.toJson(config));
        Assertions.assertTrue(
                Objects.nonNull(config) && Objects.equals(config.getStatus(), CommonStatusEnum.FORBIDDEN.status));

        UpdBizConfigRequest request = new UpdBizConfigRequest()
                .setId(id)
                .setStatus(CommonStatusEnum.OK.status)
                .setBizType(BizDictConfigTypeEnum.PROGRESS.name())
                .setLabel("测试可用状态")
                .setDescription("测试说明信息");
        bizConfigService.updateBizConfigInfo(request, userId);
        config = bizDictConfigManager.getById(id);
        log.info("========> updated config: {}", GsonHelper.toJson(config));
        Assertions.assertTrue(Objects.nonNull(config)
                && Objects.equals(config.getStatus(), CommonStatusEnum.OK.status)
                && StringUtils.equals("测试可用状态", config.getLabel())
                && StringUtils.equals("测试说明信息", config.getDescription())
        );

        request.setLabel("测试禁用状态")
                .setStatus(CommonStatusEnum.FORBIDDEN.status)
                .setDescription("");
        bizConfigService.updateBizConfigInfo(request, userId);
        config = bizDictConfigManager.getById(id);
        log.info("========> updated config: {}", GsonHelper.toJson(config));
        Assertions.assertTrue(Objects.nonNull(config)
                && Objects.equals(config.getStatus(), CommonStatusEnum.FORBIDDEN.status)
                && StringUtils.equals("测试禁用状态", config.getLabel())
                && StringUtils.isBlank(config.getDescription())
        );

        bizDictConfigManager.removeById(id);
        config = bizDictConfigManager.getById(id);
        Assertions.assertNull(config);
    }
}