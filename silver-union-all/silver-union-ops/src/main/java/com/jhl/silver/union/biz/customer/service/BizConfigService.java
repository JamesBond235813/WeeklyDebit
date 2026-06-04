package com.jhl.silver.union.biz.customer.service;

import com.jhl.silver.union.biz.common.enums.BizDictConfigTypeEnum;
import com.jhl.silver.union.web.data.BizDictItem;
import com.jhl.silver.union.web.data.admin.AddBizConfigRequest;
import com.jhl.silver.union.web.data.admin.UpdBizConfigRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 业务字典配置类服务
 *
 * @author: qingren
 * @create_time: 2025/3/29
 */
public interface BizConfigService {
    /**
     * 获取业务字典列表
     *
     * @param configTypeEnum
     * @param validOnly      true: 仅获取有效状态的字典信息
     * @return
     */
    List<BizDictItem> getBizDictItems(BizDictConfigTypeEnum configTypeEnum, boolean validOnly);

    /**
     * 获取业务字典Map
     *
     * @param configTypeEnum
     * @param validOnly
     * @return Map&lt;BizDictItem.intValue,BizDictItem>
     */
    Map<Integer, BizDictItem> getBizDictItemMap(BizDictConfigTypeEnum configTypeEnum, boolean validOnly);

    /**
     * 新增业务字典数据
     *
     * @param request
     * @param optUserId
     * @return 返回新增成功的数据 ID
     */
    Long addBizConfigInfo(AddBizConfigRequest request, Long optUserId);

    /**
     * 更新业务字典。<br>
     * 仅可更新业务字典描述，以及业务字典项目
     *
     * @param request
     * @param optUserId
     */
    void updateBizConfigInfo(UpdBizConfigRequest request, Long optUserId);

    /**
     * 根据给定的渠道名称自动生成渠道信息的业务字典
     *
     * @param channelNames
     * @param optUserId
     */
    void generateChannelDictByNames(Set<String> channelNames, Long optUserId);

    /**
     * 根据类型以及名称获取 单条业务字典信息
     *
     * @param configTypeEnum
     * @param label
     * @return
     */
    BizDictItem getSingleBizDictItemByLabel(BizDictConfigTypeEnum configTypeEnum, String label);

    /**
     * 管理端：获取完整字典项（含id/status/sortNo等）
     * @param configTypeEnum 字典类型
     * @param validOnly 是否仅取有效
     */
    java.util.List<com.jhl.silver.union.web.data.admin.BizDictItemAdmin> listAdminDictItems(
            BizDictConfigTypeEnum configTypeEnum, boolean validOnly);
}
