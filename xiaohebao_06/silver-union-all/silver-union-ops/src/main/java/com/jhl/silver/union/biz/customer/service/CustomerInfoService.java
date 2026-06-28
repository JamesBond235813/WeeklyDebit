package com.jhl.silver.union.biz.customer.service;

import com.github.pagehelper.PageInfo;
import com.jhl.silver.union.biz.common.enums.FavoriteTypeEnum;
import com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum;
import com.jhl.silver.union.web.data.CustomerItemDTO;
import com.jhl.silver.union.web.data.admin.AddCustomerInfoRequest;
import com.jhl.silver.union.web.data.admin.AddLeaderRemarkRequest;
import com.jhl.silver.union.web.data.admin.DispatchCustomerInfoRequest;
import com.jhl.silver.union.web.data.admin.UpdCustomerInfoRequest;
import com.jhl.silver.union.web.data.customer.CustomerItemDetailDTO;
import com.jhl.silver.union.web.data.customer.PagedListCustomerRequest;
import com.jhl.silver.union.web.data.customer.UpdateBizCustomerInfoRequest;

import java.util.List;
import java.util.Set;

/**
 * @author: qingren
 * @create_time: 2025/3/30
 */
public interface CustomerInfoService {

    /**
     * 分页查询客户信息列表
     *
     * @param request       查询请求
     * @param optUserId     操作人用户ID
     * @param optUserDeptId 操作人归属的部门ID
     * @param roles         当前查询的用户拥有的权限
     * @param needExtendQry true: 扩展查询标标记信息内容， 如人名，部门名称
     * @return
     */
    PageInfo<CustomerItemDTO> pageListCustomerInfo(PagedListCustomerRequest request, Long optUserId, Long optUserDeptId,
            Set<UserAuthRoleEnum> roles, boolean needExtendQry);

    /**
     * 更新客户业务信息(不可更新客户姓名，手机号)
     *
     * @param request
     * @param roles
     */
    void updateCustomerBizInfo(UpdateBizCustomerInfoRequest request, Long optUserId,Set<UserAuthRoleEnum> roles);

    /**
     * 根据客户 ID 取客户信息详情
     *
     * @param custId        客户信息 ID
     * @param optUserId     操作人用户ID
     * @param optUserDeptId 操作人归属的部门ID
     * @param roles         当前查询的用户拥有的权限
     * @return
     */
    CustomerItemDetailDTO getCustomerItemDetail(Long custId, Long optUserId, Long optUserDeptId,
            Set<UserAuthRoleEnum> roles);

    /**
     * 新增客户信息
     *
     * @param request
     * @param optUserId
     * @return 返回入库后的 ID
     */
    Long addCustomerFactInfo(AddCustomerInfoRequest request, Long optUserId, Long optUserDeptId,
            Set<UserAuthRoleEnum> roles);

    /**
     * 更新客户信息(可更新客户姓名，手机号, 并且无视 version 信息)
     *
     * @param request
     */
    void updateCustomerFactInfo(UpdCustomerInfoRequest request, Long optUserId);

    /**
     * 指派客户。 要求超管或部门数据管理员权限
     *
     * @param request
     * @param optUserId
     * @param optUserDeptId
     * @param roles
     */
    void dispatchCustomer(DispatchCustomerInfoRequest request, Long optUserId, Long optUserDeptId,
            Set<UserAuthRoleEnum> roles);

    /**
     * 将用户自己的客户信息退回至原部门公海。
     *
     * @param id
     * @param optUserId
     */
    void backCustomer(Long id, Long optUserId);

    /**
     * 将用户自己的客户信息退回至原部门公海。
     *
     * @param idList
     * @param optUserId
     * @param optUserDeptId
     */
    void backCustomers(List<Long> idList, Long optUserId,Long optUserDeptId);

    /**
     * 从公海领取客户。
     *
     * @param id 客户ID
     * @param optUserId 操作人用户ID
     * @param optUserDeptId 操作人部门ID
     */
    void claimCustomer(Long id, Long optUserId, Long optUserDeptId);

    /**
     * 将超时未跟进客户自动回收到公海。
     *
     * @param limit 单次处理上限
     * @return 处理数量
     */
    int recycleTimeoutCustomers(int limit);

    /**
     * 将用户自己的客户信息批量标记为指定的收藏类型。
     * @param idList
     * @param favType
     * @param optUserId
     * @param optUserDeptId
     */
    void switchFav4Customers(List<Long> idList, FavoriteTypeEnum favType, Long optUserId,Long optUserDeptId);

    /**
     * 追加上级评价
     *
     * @param request
     * @param optUserId
     * @param roles
     */
    void addLeaderRemark(AddLeaderRemarkRequest request, Long optUserId,Set<UserAuthRoleEnum> roles);
}
