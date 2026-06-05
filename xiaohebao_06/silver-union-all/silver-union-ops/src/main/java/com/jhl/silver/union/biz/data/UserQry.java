package com.jhl.silver.union.biz.data;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhl.silver.union.biz.common.BizConstance;
import com.jhl.silver.union.biz.common.enums.CommonStatusEnum;
import com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum;
import com.jhl.silver.union.biz.user.dal.entity.SuUserInfoDO;
import com.jhl.silver.union.web.data.admin.PagedListUserInfoRequest;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 用户信息查询类
 *
 * @author: qingren
 * @create_time: 2025/3/17
 */
@Data
@Accessors(chain = true)
public class UserQry {
    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户ID集合
     */
    private Collection<Long> ids;

    /**
     * 用户真实姓名前缀
     */
    private String realNamePrefix;

    /**
     * 用户名,即用户登录账号，不可重复
     */
    private String userName;

    /**
     * 用户手机号
     */
    private String phone;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户状态。 1：正常。 0：禁用
     */
    private Integer status;

    /**
     * 工号
     */
    private String employeeNo;

    /**
     * 部门id
     */
    private Long departmentId;

    /**
     * 归属部门 ID 集合
     */
    private Collection<Long> deptIds;

    /**
     * 账户状态 0-正常; 其它-已注销
     */
    private Long deleteFlag;
    private Integer onlineStatus;
    private Boolean excludeSuper;

    public LambdaQueryWrapper<SuUserInfoDO> toQryWrapper(boolean forceValid) {
        LambdaQueryWrapper<SuUserInfoDO> qw = new LambdaQueryWrapper<>();
        if (forceValid) {
            qw.eq(SuUserInfoDO::getDeleteFlag, 0)
                    .eq(SuUserInfoDO::getStatus, CommonStatusEnum.OK.status);
        } else {
            qw.eq(Objects.nonNull(this.deleteFlag), SuUserInfoDO::getDeleteFlag, this.deleteFlag)
                    .eq(Objects.nonNull(this.status), SuUserInfoDO::getStatus, this.status);
        }
        if (Objects.nonNull(this.departmentId)) {
            if (CollectionUtils.isEmpty(this.deptIds)) {
                this.deptIds = List.of(this.departmentId);
            } else {
                this.deptIds.add(this.departmentId);
            }
        }
        qw.eq(StringUtils.isNotBlank(this.userName), SuUserInfoDO::getUserName, this.userName)
                .eq(Objects.nonNull(this.id), SuUserInfoDO::getId, this.id)
                .eq(StringUtils.isNotBlank(this.phone), SuUserInfoDO::getPhone, this.phone)
                .eq(StringUtils.isNotBlank(this.email), SuUserInfoDO::getEmail, this.email)
                .eq(StringUtils.isNotBlank(this.employeeNo), SuUserInfoDO::getEmployeeNo, this.employeeNo)
                .in(!CollectionUtils.isEmpty(ids), SuUserInfoDO::getId, this.ids)
                .in(!CollectionUtils.isEmpty(this.deptIds), SuUserInfoDO::getDepartmentId, this.deptIds)
                .eq(Objects.nonNull(this.onlineStatus), SuUserInfoDO::getOnlineStatus, this.onlineStatus)
                .notLike(Boolean.TRUE.equals(this.excludeSuper), SuUserInfoDO::getRoles,
                        UserAuthRoleEnum.ROLE_SUPPER.name())
                .likeRight(StringUtils.isNotBlank(this.realNamePrefix), SuUserInfoDO::getRealName, this.realNamePrefix)
        ;
        return qw;
    }

    public static UserQry makeupFrom(PagedListUserInfoRequest request) {
        UserQry qry = new UserQry()
                .setId(request.getUserId())
                .setUserName(request.getUserName())
                .setRealNamePrefix(request.getUserRealNamePrefix())
                .setPhone(request.getPhone())
                .setDepartmentId(request.getDepartmentId())
                .setDeleteFlag(BizConstance.NOT_DELETED);
        return qry;
    }
}
