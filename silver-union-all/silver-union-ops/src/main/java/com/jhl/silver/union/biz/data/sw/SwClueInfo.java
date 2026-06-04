package com.jhl.silver.union.biz.data.sw;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 邵文数据平台客户线索信息
 *
 * @author: qingren
 * @create_time: 2025/12/6
 */
@Data
@Accessors(chain = true)
@Schema(description = "邵文数据平台客户线索信息")
public class SwClueInfo {
    /**
     * 必填：是	推送线索的单号，全局唯一，且不超过32位
     */
    @Schema(description = "推送线索的单号，全局唯一，且不超过32位")
    private String orderNo;
    /**
     * 必填：是	申请人所在城市ID。 国标编码 6 位
     */
    @Schema(description = "申请人所在城市ID。 国标编码 6 位")
    private String cityId;
    /**
     * 必填：是	申请人所在城市名称。
     */
    @Schema(description = "申请人所在城市名称。")
    private String cityName;
    /**
     * 必填：是	用户姓名
     */
    @Schema(description = "用户姓名")
    private String name;
    /**
     * 必填：是	用户手机号
     */
    @Schema(description = "用户手机号")
    private String phone;
    /**
     * 必填：是	贷款类型。 0 车抵贷；1 房抵贷；
     */
    @Schema(description = "贷款类型。 0 车抵贷；1 房抵贷；")
    private String loanType;
    /**
     * 必填：是	是否有房产。 0 无；1 有； 当loanType=0时传0；当loanType=1时传1
     */
    @Schema(description = "是否有房产。 0 无；1 有； 当loanType=0时传0；当loanType=1时传1")
    private String isHouse;
    /**
     * 必填：否	房产所在城市ID。 国标编码 6 位（国家标准GB/T 2260-2007） 当isHouse=1时传值
     */
    @Schema(description = "房产所在城市ID。 国标编码 6 位（国家标准GB/T 2260-2007） 当isHouse=1时传值")
    private String houseCity;
    /**
     * 必填：是	是否有车产。0 无；1 有； 当loanType=0时传1；当loanType=1时传0
     */
    @Schema(description = "是否有车产。0 无；1 有； 当loanType=0时传1；当loanType=1时传0")
    private String isCar;
    /**
     * 必填：否	车牌号 （当isCar=1时传值）
     */
    @Schema(description = "车牌号 （当isCar=1时传值）")
    private String carNo;
    /**
     * 必填：否	购买方式 0 全款；1 贷款-已结清；2 贷款-未结清； 无值表示未知
     */
    @Schema(description = "购买方式 0 全款；1 贷款-已结清；2 贷款-未结清； 无值表示未知")
    private String purchaseType;
}
