package com.jhl.silver.union.biz.data;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerInfoItemDO;
import com.jhl.silver.union.commons.utils.OtherUtils;
import com.jhl.silver.union.commons.utils.SuDateUtils;
import com.jhl.silver.union.web.data.customer.PagedListCustomerRequest;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.util.*;

/**
 * 客户信息列表查询类
 *
 * @author: qingren
 * @create_time: 2025/3/29
 */
@Data
@Accessors(chain = true)
@Slf4j
public class CustomerInfoQry {
    /**
     * 客户ID
     */
    private Collection<Long> ids;

    /**
     * 姓名
     */
    private String name;
    /**
     * 姓名前缀
     */
    private String namePrefix;

    /**
     * 手机号
     */
    private String mobile;
    /**
     * 身份证号
     */
    private String idCardNo;

    /**
     * 跟进状态。 见业务字典定义
     */
    private List<Integer> progressList;
    /**
     * 最后一次跟进时间 区间
     */
    private QryRangeItem<Date> followTimeRange;

    /**
     * 申请时间（进本平台件的时间）区间
     */
    private QryRangeItem<Date> applyDateRange;

    /**
     * 客户分组
     */
    private Integer customerGroup;
    /**
     * 当前数据归属部门ID
     */
    private Collection<Long> ownerDeptIds;

    /**
     * 当前数据归属人用户ID
     */
    private Long ownerUserId;

    /**
     * 当前数据归属人用户ID列表
     */
    private List<Long> userIdList;

    /**
     * 收藏标识。 1:收藏; 其它:未收藏
     */
    private Integer ownerFavorite;
    /**
     * 电话结果。
     */
    private Integer callTips;
    /**
     * 未跟进天数， 按大于等于的方式查询
     */
    private Integer ignoreDays;

    /**
     * 芝麻分最小值（大于等于）
     */
    private Integer zhimaScoreMin;
    /**
     * 数据版本信息.用于乐观锁
     */
    private Long version;

    /**
     * true:用户归属数据与部门数据条件取【或】
     */
    private boolean orDept = false;

    /**
     * 是否按申请时间倒序排列
     */
    private Boolean orderByApplyDateDesc;
    /**
     * 是否按跟进时间倒序排列
     */
    private Boolean orderByFollowTimeDesc;

    /**
     * 手机号集合
     */
    private Collection<String> mobiles;
    /**
     * 推广渠道ID。见业务字典定义
     */
    private Integer channel;
    /**
     * 推广渠道ID列表
     */
    private List<Integer> channelList;

    private Boolean selfOnly;
    private Boolean publicPoolOnly;
    private Integer publicPoolStarFlag;
    private String regionProvince;
    private String regionCity;

    /**
     * 增加客户 ID
     *
     * @param id
     * @return
     */
    public CustomerInfoQry addId(Long id) {
        if (Objects.isNull(id)) {
            return this;
        }
        if (CollectionUtils.isEmpty(this.ids)) {
            this.ids = Lists.newArrayList();
        }
        this.ids.add(id);
        return this;
    }

    /**
     * 增加跟进状态条件值
     *
     * @param progress
     * @return
     */
    public CustomerInfoQry addProgress(Integer progress) {
        if (Objects.isNull(progress)) {
            return this;
        }
        if (CollectionUtils.isEmpty(this.progressList)) {
            this.progressList = Lists.newArrayList();
        }
        this.progressList.add(progress);
        return this;
    }

    /**
     * 增加归属部门条件值
     *
     * @param deptId
     * @return
     */
    public CustomerInfoQry addOwnerDeptId(Long deptId) {
        if (Objects.isNull(deptId)) {
            return this;
        }
        if (CollectionUtils.isEmpty(this.ownerDeptIds)) {
            this.ownerDeptIds = Lists.newArrayList();
        }
        this.ownerDeptIds.add(deptId);
        return this;
    }

    /**
     * @return
     */
    public LambdaQueryWrapper<CustomerInfoItemDO> toQueryWrapper() {
        LambdaQueryWrapper<CustomerInfoItemDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(this.name), CustomerInfoItemDO::getName, this.name)
                .likeRight(StringUtils.isNotBlank(this.namePrefix), CustomerInfoItemDO::getName, this.namePrefix)
                .eq(StringUtils.isNotBlank(this.mobile), CustomerInfoItemDO::getMobile, this.mobile)
                .eq(StringUtils.isNotBlank(this.idCardNo), CustomerInfoItemDO::getIdCardNo, this.idCardNo)
                .in(!CollectionUtils.isEmpty(this.progressList), CustomerInfoItemDO::getProgress, this.progressList)
                .in(!CollectionUtils.isEmpty(this.userIdList), CustomerInfoItemDO::getOwnerUserId, this.userIdList)
                .in(!CollectionUtils.isEmpty(this.mobiles), CustomerInfoItemDO::getMobile, this.mobiles)
                .eq(Objects.nonNull(this.customerGroup), CustomerInfoItemDO::getCustomerGroup, this.customerGroup)
                .eq(Objects.nonNull(this.callTips), CustomerInfoItemDO::getCallTips, this.callTips)
                .eq(Objects.nonNull(this.version), CustomerInfoItemDO::getOwnerFavorite, this.version)
                .in(Objects.equals(this.publicPoolStarFlag, 1), CustomerInfoItemDO::getPublicPoolStarFlag, List.of(1, 2))
                .eq(Objects.equals(this.publicPoolStarFlag, 2), CustomerInfoItemDO::getPublicPoolStarFlag, 2)
                .ge(Objects.nonNull(this.zhimaScoreMin), CustomerInfoItemDO::getZhimaScore, this.zhimaScoreMin)
                .in(!CollectionUtils.isEmpty(this.channelList), CustomerInfoItemDO::getChannel, this.channelList)
                .eq(CollectionUtils.isEmpty(this.channelList) && Objects.nonNull(this.channel),
                        CustomerInfoItemDO::getChannel, this.channel);
        if (StringUtils.isNotBlank(this.regionProvince)) {
            String province = this.regionProvince.trim();
            String city = StringUtils.trimToEmpty(this.regionCity);
            List<String> provinceValues = areaVariants(province);
            List<String> cityValues = areaVariants(city);
            queryWrapper.and(qw -> {
                qw.and(mobileQw -> {
                            mobileQw.and(areaQw -> {
                                for (int i = 0; i < provinceValues.size(); i++) {
                                    if (i > 0) {
                                        areaQw.or();
                                    }
                                    areaQw.like(CustomerInfoItemDO::getMobileArea, provinceValues.get(i));
                                }
                            });
                            if (!CollectionUtils.isEmpty(cityValues)) {
                                mobileQw.and(areaQw -> {
                                    for (int i = 0; i < cityValues.size(); i++) {
                                        if (i > 0) {
                                            areaQw.or();
                                        }
                                        areaQw.like(CustomerInfoItemDO::getMobileArea, cityValues.get(i));
                                    }
                                });
                            }
                        })
                        .or()
                        .and(idCardQw -> {
                            idCardQw.in(CustomerInfoItemDO::getHukouProvince, provinceValues);
                            if (!CollectionUtils.isEmpty(cityValues)) {
                                idCardQw.in(CustomerInfoItemDO::getHukouCity, cityValues);
                            }
                        });
            });
        }
        if (Objects.equals(this.publicPoolStarFlag, 0)) {
            queryWrapper.and(qw -> qw.isNull(CustomerInfoItemDO::getPublicPoolStarFlag)
                    .or()
                    .eq(CustomerInfoItemDO::getPublicPoolStarFlag, 0));
        }
        if (Objects.nonNull(this.ownerFavorite)) {
            // 查询收藏类型时， 要求带上归属人
            queryWrapper.eq(CustomerInfoItemDO::getOwnerFavorite, this.ownerFavorite)
                    .gt(CustomerInfoItemDO::getOwnerUserId, 0L);
        }
        OtherUtils.processIfNotNull(this.followTimeRange,
                () -> this.followTimeRange.setupQryWrapper(queryWrapper, CustomerInfoItemDO::getFollowTime));
        OtherUtils.processIfNotNull(this.applyDateRange,
                () -> this.applyDateRange.setupQryWrapper(queryWrapper, CustomerInfoItemDO::getApplyDate));

        if (Objects.nonNull(this.ignoreDays) && ignoreDays > 0) {
            Date baseDate = SuDateUtils.add(new Date(), Calendar.DAY_OF_MONTH, -this.ignoreDays);
            queryWrapper.isNotNull(CustomerInfoItemDO::getFollowTime)
                    .le(CustomerInfoItemDO::getFollowTime, baseDate);
        }
        if (Boolean.TRUE.equals(this.publicPoolOnly)) {
            queryWrapper.eq(CustomerInfoItemDO::getOwnerUserId, 0L)
                    .in(!CollectionUtils.isEmpty(this.ids), CustomerInfoItemDO::getId, this.ids);
        } else if (Boolean.TRUE.equals(this.selfOnly)) {
            queryWrapper.eq(Objects.nonNull(this.ownerUserId), CustomerInfoItemDO::getOwnerUserId, this.ownerUserId);
        } else {
            if (orDept) {
                queryWrapper.and(
                        qw -> qw.in(!CollectionUtils.isEmpty(this.ownerDeptIds), CustomerInfoItemDO::getOwnerDeptId,
                                this.ownerDeptIds)
                                .in(!CollectionUtils.isEmpty(this.ids), CustomerInfoItemDO::getId, this.ids)
                                .or()
                                .eq(Objects.nonNull(this.ownerUserId), CustomerInfoItemDO::getOwnerUserId,
                                        this.ownerUserId)
                                .in(!CollectionUtils.isEmpty(this.ids), CustomerInfoItemDO::getId, this.ids));
            } else {
                queryWrapper
                        .in(!CollectionUtils.isEmpty(this.ownerDeptIds), CustomerInfoItemDO::getOwnerDeptId,
                                this.ownerDeptIds)
                        .in(!CollectionUtils.isEmpty(this.ids), CustomerInfoItemDO::getId, this.ids)
                        .eq(Objects.nonNull(this.ownerUserId), CustomerInfoItemDO::getOwnerUserId, this.ownerUserId);
            }
        }
        queryWrapper.orderByDesc(Boolean.TRUE.equals(this.orderByFollowTimeDesc), CustomerInfoItemDO::getFollowTime);
        queryWrapper.orderByDesc(Boolean.TRUE.equals(this.orderByApplyDateDesc), CustomerInfoItemDO::getApplyDate);
        return queryWrapper;
    }

    public static CustomerInfoQry makeupFrom(PagedListCustomerRequest request) {
        CustomerInfoQry qry = new CustomerInfoQry();
        qry.addId(request.getId())
                .setNamePrefix(request.getNamePrefix())
                .setMobile(request.getMobile())
                .setIdCardNo(request.getIdCardNo())
                .addProgress(request.getProgress())
                .setCallTips(request.getCallTips())
                .setCustomerGroup(request.getCustomerGroup())
                .setFollowTimeRange(
                        QryRangeItem.of(parseDate(request.getFollowTimeStart()), parseDate(request.getFollowTimeEnd())))
                .setApplyDateRange(
                        QryRangeItem.of(parseDate(request.getApplyDateStart()), parseDate(request.getApplyDateEnd())))
                .setOwnerUserId(request.getOwnerUserId())
                .setOwnerDeptIds(request.getOwnerDeptIds())
                .setOwnerFavorite(request.getOwnerFavorite())
                .setOrderByApplyDateDesc(request.getOrderByApplyDateDesc())
                .setOrderByFollowTimeDesc(request.getOrderByFollowTimeDesc())
                .setUserIdList(request.getUserIdList())
                .setIgnoreDays(request.getIgnoreDays())
                .setZhimaScoreMin(request.getZhimaScoreMin())
                .setChannel(request.getChannel())
                .setSelfOnly(request.getSelfOnly())
                .setPublicPoolOnly(request.getPublicPoolOnly())
                .setPublicPoolStarFlag(request.getPublicPoolStarFlag())
                .setRegionProvince(request.getRegionProvince())
                .setRegionCity(request.getRegionCity());
        if (!CollectionUtils.isEmpty(request.getProgressList())) {
            qry.setProgressList(request.getProgressList());
        }
        if (!CollectionUtils.isEmpty(request.getChannelList())) {
            qry.setChannelList(request.getChannelList());
        }
        return qry;

    }

    private static Date parseDate(String dateStr) {
        if (StringUtils.isBlank(dateStr)) {
            return null;
        }
        try {
            return SuDateUtils.parse(dateStr, SuDateUtils.DF_YYYY_MM_DDHHMMSS);
        } catch (ParseException e) {
            log.error("Parsing date [{}] failed: {}", dateStr, e.getMessage());
            return null;
        }
    }

    private static List<String> areaVariants(String areaName) {
        if (StringUtils.isBlank(areaName)) {
            return List.of();
        }
        LinkedHashSet<String> values = new LinkedHashSet<>();
        String normalized = areaName.trim();
        values.add(normalized);
        String shortName = normalized
                .replace("维吾尔自治区", "")
                .replace("壮族自治区", "")
                .replace("回族自治区", "")
                .replace("特别行政区", "")
                .replace("自治区", "")
                .replace("自治州", "")
                .replace("地区", "")
                .replace("盟", "")
                .replace("省", "")
                .replace("市", "");
        if (StringUtils.isNotBlank(shortName)) {
            values.add(shortName);
        }
        return new ArrayList<>(values);
    }

}
