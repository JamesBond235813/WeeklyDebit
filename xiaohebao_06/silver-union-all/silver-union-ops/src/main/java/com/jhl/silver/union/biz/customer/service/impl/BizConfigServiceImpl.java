package com.jhl.silver.union.biz.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhl.silver.union.biz.common.ResultCode;
import com.jhl.silver.union.biz.common.enums.BizDictConfigTypeEnum;
import com.jhl.silver.union.biz.common.enums.CommonStatusEnum;
import com.jhl.silver.union.biz.common.enums.CustomerListFieldEnum;
import com.jhl.silver.union.biz.customer.dal.entity.BizDictConfigDO;
import com.jhl.silver.union.biz.customer.manager.BizDictConfigManager;
import com.jhl.silver.union.biz.customer.service.BizConfigService;
import com.jhl.silver.union.biz.data.convert.BizDictConfigConvert;
import com.jhl.silver.union.commons.exception.BizException;
import com.jhl.silver.union.commons.gson.GsonHelper;
import com.jhl.silver.union.web.data.BizDictItem;
import com.jhl.silver.union.web.data.admin.AddBizConfigRequest;
import com.jhl.silver.union.web.data.admin.UpdBizConfigRequest;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author: qingren
 * @create_time: 2025/3/29
 */
@Service
public class BizConfigServiceImpl implements BizConfigService {

    @Resource
    private BizDictConfigConvert convert;
    @Resource
    private BizDictConfigManager bizDictConfigManager;

    @Override
    public List<BizDictItem> getBizDictItems(BizDictConfigTypeEnum configTypeEnum, boolean validOnly) {
        if (Objects.isNull(configTypeEnum)) {
            return List.of();
        }
        ensureCustomerListFieldConfigsIfNeeded(configTypeEnum);
        List<BizDictConfigDO> list = bizDictConfigManager.listByDictType(configTypeEnum, validOnly);
        return convert.convert2BizDictItemList(list);
    }

    @Override
    public Map<Integer, BizDictItem> getBizDictItemMap(BizDictConfigTypeEnum configTypeEnum, boolean validOnly) {
        List<BizDictItem> list = this.getBizDictItems(configTypeEnum, validOnly);
        if (CollectionUtils.isEmpty(list)) {
            return Map.of();
        }
        return list.stream()
                .collect(Collectors.toMap(BizDictItem::getIntValue, e -> e, (v1, v2) -> v2));
    }

    @Override
    public java.util.List<com.jhl.silver.union.web.data.admin.BizDictItemAdmin> listAdminDictItems(
            BizDictConfigTypeEnum configTypeEnum, boolean validOnly) {
        ensureCustomerListFieldConfigsIfNeeded(configTypeEnum);
        List<BizDictConfigDO> list = bizDictConfigManager.listByDictType(configTypeEnum, validOnly);
        if (CollectionUtils.isEmpty(list)) {
            return java.util.List.of();
        }
        return list.stream().map(e -> new com.jhl.silver.union.web.data.admin.BizDictItemAdmin()
                .setId(e.getId())
                .setBizType(e.getBizType())
                .setTypeDesc(e.getTypeDesc())
                .setIntValue(e.getIntValue())
                .setLabel(e.getLabel())
                .setDescription(e.getDescription())
                .setStatus(e.getStatus())).collect(Collectors.toList());
    }

    @Override
    public Long addBizConfigInfo(AddBizConfigRequest request, Long optUserId) {
        request.validate();
        BizDictConfigDO configDO = new BizDictConfigDO()
                .setBizType(request.getBizDictConfigTypeEnum().name())
                .setStatus(CommonStatusEnum.OK.status)
                .setTypeDesc(request.getBizDictConfigTypeEnum().desc)
                .setIntValue(request.getIntValue())
                .setLabel(request.getLabel())
                .setDescription(request.getDescription())
                .setStatus(request.getStatus())
                .setOptUserId(optUserId);
        try {
            bizDictConfigManager.save(configDO);
        } catch (DuplicateKeyException e) {
            throw BizException.makeupBy(ResultCode.ADD_FAILED_FOR_DUPLICATED_BIZ_TYPE_VALUE,
                    List.of(request.getBizDictConfigTypeEnum().desc, request.getIntValue() + ""),
                    GsonHelper.toJson(request));
        }
        bizDictConfigManager.clearCache(request.getBizDictConfigTypeEnum());
        return configDO.getId();
    }

    @Override
    public void updateBizConfigInfo(UpdBizConfigRequest request, Long optUserId) {
        request.validate();
        BizDictConfigDO upd = new BizDictConfigDO()
                .setStatus(request.getStatus())
                .setDescription(request.getDescription())
                .setLabel(request.getLabel())
                .setIntValue(request.getIntValue())
                .setOptUserId(optUserId);
        LambdaQueryWrapper<BizDictConfigDO> qw = new LambdaQueryWrapper<>();
        qw.eq(BizDictConfigDO::getId, request.getId())
                .eq(BizDictConfigDO::getBizType, request.getBizDictConfigTypeEnum().name());
        boolean succ = bizDictConfigManager.update(upd, qw);
        if (succ) {
            bizDictConfigManager.clearCache(request.getBizDictConfigTypeEnum());
        }
    }

    @Override
    public synchronized void generateChannelDictByNames(Set<String> channelNames, Long optUserId) {
        if (CollectionUtils.isEmpty(channelNames)) {
            return;
        }
        List<BizDictItem> channelList = this.getBizDictItems(BizDictConfigTypeEnum.DATA_CHANNEL, true);
        List<String> absentChannelNames;
        if (!CollectionUtils.isEmpty(channelList)) {
            Map<String, Integer> existedChannelNameMap = channelList.stream()
                    .collect(Collectors.toMap(e -> e.getLabel(), e -> e.getIntValue(), (v1, v2) -> v2));
            absentChannelNames = channelNames.stream()
                    .filter(e -> !existedChannelNameMap.containsKey(e))
                    .sorted().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(absentChannelNames)) {
                return;
            }
        } else {
            absentChannelNames = channelNames.stream()
                    .sorted().collect(Collectors.toList());
        }
        // 到这里，absentChannelNames 一定非空
        Integer maxIntValue = CollectionUtils.isEmpty(channelList)
                ? 0
                : channelList.stream().map(BizDictItem::getIntValue).max(Integer::compareTo).orElse(0);
        AtomicInteger currentMaxIntValue = new AtomicInteger(maxIntValue);
        absentChannelNames.stream()
                .map(e -> {
                    AddBizConfigRequest r = new AddBizConfigRequest()
                            .setLabel(e)
                            .setIntValue(currentMaxIntValue.incrementAndGet())
                            .setBizDictConfigTypeEnum(BizDictConfigTypeEnum.DATA_CHANNEL)
                            .setBizType(BizDictConfigTypeEnum.DATA_CHANNEL.name());
                    return r;
                }).forEachOrdered(req -> {
                    this.addBizConfigInfo(req, optUserId);
                });
    }

    @Override
    public BizDictItem getSingleBizDictItemByLabel(BizDictConfigTypeEnum configTypeEnum, String label) {
        if (StringUtils.isBlank(label)) {
            return null;
        }
        List<BizDictItem> list = this.getBizDictItems(configTypeEnum, true);
        if (CollectionUtils.isEmpty(list)) {

            return null;
        }
        return list.parallelStream()
                .filter(e -> StringUtils.equals(e.getLabel(), label))
                .findAny().orElse(null);
    }

    private void ensureCustomerListFieldConfigsIfNeeded(BizDictConfigTypeEnum configTypeEnum) {
        if (configTypeEnum != BizDictConfigTypeEnum.CUSTOMER_LIST_FIELD) {
            return;
        }
        LambdaQueryWrapper<BizDictConfigDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizDictConfigDO::getBizType, configTypeEnum.name());
        List<BizDictConfigDO> existing = bizDictConfigManager.list(wrapper);
        Set<String> existingKeys = existing.stream()
                .map(BizDictConfigDO::getDescription)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        Set<Integer> usedValues = existing.stream()
                .map(BizDictConfigDO::getIntValue)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        int nextValue = usedValues.stream().max(Integer::compareTo).orElse(0) + 1;
        List<BizDictConfigDO> toInsert = new ArrayList<>();
        for (CustomerListFieldEnum field : CustomerListFieldEnum.values()) {
            if (existingKeys.contains(field.getFieldKey())) {
                continue;
            }
            int value = field.getSortNo();
            if (usedValues.contains(value)) {
                value = nextValue++;
            }
            usedValues.add(value);
            BizDictConfigDO configDO = new BizDictConfigDO()
                    .setBizType(configTypeEnum.name())
                    .setTypeDesc(configTypeEnum.desc)
                    .setIntValue(value)
                    .setLabel(field.getLabel())
                    .setDescription(field.getFieldKey())
                    .setStatus(CommonStatusEnum.OK.status)
                    .setOptUserId(0L);
            toInsert.add(configDO);
        }
        if (!toInsert.isEmpty()) {
            bizDictConfigManager.saveBatch(toInsert);
            bizDictConfigManager.clearCache(configTypeEnum);
        }
    }
}
