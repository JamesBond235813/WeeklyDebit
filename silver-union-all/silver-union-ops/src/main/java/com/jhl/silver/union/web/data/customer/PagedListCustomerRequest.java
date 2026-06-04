package com.jhl.silver.union.web.data.customer;

import com.jhl.silver.union.web.data.BasePagedRequest;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 分页获取客户列表信息请求
 *
 * @author: qingren
 * @create_time: 2025/3/30
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "分页获取客户列表信息请求")
public class PagedListCustomerRequest extends BasePagedRequest {
    /**
     * 客户ID
     */
    @Schema(description = "客户ID")
    private Long id;
    /**
     * 姓名前缀
     */
    @Schema(description = "姓名前缀")
    private String namePrefix;
    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String mobile;
    /**
     * 身份证号
     */
    @Schema(description = "身份证号")
    private String idCardNo;
    /**
     * 跟进状态。 见业务字典定义
     */
    @Schema(description = "跟进状态。 见业务字典定义")
    private Integer progress;
    /**
     * 跟进状态列表。 见业务字典定义
     */
    @Schema(description = "跟进状态列表。 见业务字典定义")
    private List<Integer> progressList;
    /**
     * 电话结果。见枚举定义
     */
    @Schema(description = "电话结果。见枚举定义")
    private Integer callTips;
    /**
     * 客户分组
     */
    @Schema(description = "客户分组")
    private Integer customerGroup;
    /**
     * 最后一次跟进时间 开始时间（包含）yyyy-MM-dd HH:mm:ss 格式
     */
    @Schema(description = "最后一次跟进时间 开始时间（包含）yyyy-MM-dd HH:mm:ss 格式")
    private String followTimeStart;
    /**
     * 最后一次跟进时间 截止时间（包含）yyyy-MM-dd HH:mm:ss 格式
     */
    @Schema(description = "最后一次跟进时间 截止时间（包含）yyyy-MM-dd HH:mm:ss 格式")
    private String followTimeEnd;
    /**
     * 申请时间（进本平台件的时间）开始时间（包含）yyyy-MM-dd HH:mm:ss 格式
     */
    @Schema(description = "申请时间（进本平台件的时间）开始时间（包含）yyyy-MM-dd HH:mm:ss 格式")
    private String applyDateStart;
    /**
     * 申请时间（进本平台件的时间）截止时间（包含）yyyy-MM-dd HH:mm:ss 格式
     */
    @Schema(description = "申请时间（进本平台件的时间）截止时间（包含）yyyy-MM-dd HH:mm:ss 格式")
    private String applyDateEnd;
    /**
     * 当前数据归属人用户ID
     */
    @Schema(description = "当前数据归属人用户ID")
    private Long ownerUserId;

    /**
     * 当前数据归属人用户ID列表
     */
    @Schema(description = "当前数据归属人用户ID列表")
    private List<Long> userIdList;
    /**
     * 当前数据归属部门ID
     */
    @Schema(description = "当前数据归属部门ID")
    private List<Long> ownerDeptIds;
    /**
     * 收藏标识。 1:收藏; 其它:未收藏
     */
    @Schema(description = "收藏标识。 1:收藏; 其它:未收藏")
    private Integer ownerFavorite;

    /**
     * 是否按跟进时间倒序排列
     */
    @Schema(description = "是否按跟进时间倒序排列")
    private Boolean orderByFollowTimeDesc;
    /**
     * 是否申请进时间倒序排列
     */
    @Schema(description = "是否申请进时间倒序排列")
    private Boolean orderByApplyDateDesc;

    /**
     * 未跟进天数, 按大于等于的方式查询
     */
    @Schema(description = "未跟进天数, 按大于等于的方式查询")
    private Integer ignoreDays;

    /**
     * 芝麻分最小值（大于等于）
     */
    @JsonProperty("zhimaScoreMin")
    @JsonAlias({"zhima_score_min"})
    @Schema(description = "芝麻分最小值（大于等于）")
    private Integer zhimaScoreMin;

    /**
     * 推广渠道ID。见业务字典定义
     */
    @Schema(description = "推广渠道ID")
    private Integer channel;
    /**
     * 推广渠道ID列表。见业务字典定义
     */
    @Schema(description = "推广渠道ID列表")
    private List<Integer> channelList;

    @Schema(description = "是否只查自己的数据")
    private Boolean selfOnly;

    @Schema(description = "是否只查询公海数据")
    private Boolean publicPoolOnly;

    public void autoFix() {
        super.autoFix();
        if (orderByFollowTimeDesc == null && orderByApplyDateDesc == null) {
            orderByFollowTimeDesc = true;
            orderByApplyDateDesc = true;
        }
    }

}
