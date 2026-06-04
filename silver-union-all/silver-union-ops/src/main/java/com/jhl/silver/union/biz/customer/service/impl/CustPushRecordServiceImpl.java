package com.jhl.silver.union.biz.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhl.silver.union.biz.customer.dal.entity.CustPushRecordDO;
import com.jhl.silver.union.biz.customer.manager.CustPushRecordManager;
import com.jhl.silver.union.biz.customer.service.CustPushRecordService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 推送数据记录服务实现
 *
 * @author: qingren
 * @create_time: 2026/1/27
 */
@Service
public class CustPushRecordServiceImpl implements CustPushRecordService {

    @Resource
    private CustPushRecordManager custPushRecordManager;

    @Override
    public CustPushRecordDO saveRecord(CustPushRecordDO record) {
        if (record == null) {
            return null;
        }
        custPushRecordManager.save(record);
        return record;
    }

    @Override
    public boolean existsThirdRequest(String appId, String requestId) {
        if (StringUtils.isAnyBlank(appId, requestId)) {
            return false;
        }
        LambdaQueryWrapper<CustPushRecordDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustPushRecordDO::getAppId, StringUtils.trim(appId))
                .eq(CustPushRecordDO::getRequestId, StringUtils.trim(requestId));
        return custPushRecordManager.count(wrapper) > 0;
    }

    @Override
    public void updateRecord(Long recordId, String custName, String mobile, String orderNo, String receivedReq,
            Integer existedFlag, String remarkJson) {
        if (recordId == null) {
            return;
        }
        CustPushRecordDO update = new CustPushRecordDO().setId(recordId)
                .setCustName(StringUtils.trimToEmpty(custName))
                .setMobile(StringUtils.trimToEmpty(mobile))
                .setOrderNo(StringUtils.trimToEmpty(orderNo))
                .setReceivedReq(receivedReq)
                .setRemark(StringUtils.defaultString(remarkJson));
        if (existedFlag != null) {
            update.setExistedFlag(existedFlag);
        }
        custPushRecordManager.updateById(update);
    }
}
