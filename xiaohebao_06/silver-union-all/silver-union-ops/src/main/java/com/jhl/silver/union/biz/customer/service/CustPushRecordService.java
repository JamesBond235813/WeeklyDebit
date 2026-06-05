package com.jhl.silver.union.biz.customer.service;

import com.jhl.silver.union.biz.customer.dal.entity.CustPushRecordDO;

/**
 * 推送数据记录服务
 *
 * @author: qingren
 * @create_time: 2026/1/27
 */
public interface CustPushRecordService {

    /**
     * 保存推送记录
     *
     * @param record 记录
     * @return 保存后的记录（含ID）
     */
    CustPushRecordDO saveRecord(CustPushRecordDO record);

    /**
     * 判断三方平台请求是否已处理过
     *
     * @param appId 应用ID
     * @param requestId 请求ID
     * @return true 表示已存在
     */
    boolean existsThirdRequest(String appId, String requestId);

    /**
     * 判断三方平台手机号是否已有通过撞库的记录
     *
     * @param appId 应用ID
     * @param mobileMd5 手机号MD5
     * @return true 表示存在已通过撞库记录
     */
    boolean existsPassedAccessCheck(String appId, String mobileMd5);

    /**
     * 判断指定渠道的业务请求ID是否已有通过撞库的记录
     *
     * @param channelName 渠道名称
     * @param orderNo 业务请求ID
     * @return true 表示存在已通过撞库记录
     */
    boolean existsPassedAccessCheckOrder(String channelName, String orderNo);

    /**
     * 判断指定渠道的业务请求ID是否已完成推送
     *
     * @param channelName 渠道名称
     * @param orderNo 业务请求ID
     * @return true 表示已推送
     */
    boolean existsPushedOrder(String channelName, String orderNo);

    /**
     * 更新推送记录（一次性更新）
     *
     * @param recordId 记录ID
     * @param custName 客户姓名
     * @param mobile 手机号/MD5
     * @param orderNo 订单号
     * @param receivedReq 接收请求明文
     * @param existedFlag 是否已存在标识
     * @param remarkJson 响应JSON
     */
    void updateRecord(Long recordId, String custName, String mobile, String orderNo, String receivedReq,
            Integer existedFlag, String remarkJson);
}
