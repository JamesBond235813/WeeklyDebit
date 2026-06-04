package com.jhl.silver.union.web.data.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "客户通知条目")
public class CustomerNoticeItemDTO {
    private Long id;
    private Long userId;
    private Long deptId;
    private Long custId;
    private String custName;
    private String custMobile;
    private String custIdCard;
    private Long ownerUserId;
    private Long ownerDeptId;
    private Integer ownerFavorite;
    private String noticeType;
    private String source;
    private Integer status;
    private Date gmtCreate;
}
