package com.jhl.silver.union.biz.user.service;

import com.github.pagehelper.PageInfo;
import com.jhl.silver.union.web.data.UserInfoDTO;
import com.jhl.silver.union.web.data.admin.AddUserInfoRequest;
import com.jhl.silver.union.web.data.admin.PagedListUserInfoRequest;
import com.jhl.silver.union.web.data.admin.UpdateUserInfoRequest;
import com.jhl.silver.union.web.data.user.BriefUserInfoDTO;
import com.jhl.silver.union.web.data.user.UserItemInfo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 用户信息服务
 *
 * @author: qingren
 * @create_time: 2025/3/17
 */
public interface UserService {

    /**
     * 根据用户 ID 取用户简要信息
     *
     * @param userId
     * @return
     */
    BriefUserInfoDTO getUserInfo(Long userId);

    /**
     * 分页查询用户信息
     *
     * @param request
     * @return
     */
    PageInfo<UserInfoDTO> pageListUserInfo(PagedListUserInfoRequest request, Long optUserId);

    /**
     * 统计当前操作人可管理范围内的在线用户数
     *
     * @param optUserId 操作人用户 ID
     * @return 在线用户数
     */
    long countManageableOnlineUsers(Long optUserId);

    /**
     * 新增用户信息
     *
     * @param request
     * @param optUserId
     */
    void addUserInfo(AddUserInfoRequest request, Long optUserId);

    /**
     * 更新用户信息
     *
     * @param request
     */
    void updateUserInfo(UpdateUserInfoRequest request, Long optUserId);

    /**
     * 根据用户 ID 删除用户信息
     *
     * @param id
     * @param optUserId
     */
    void deleteUserInfo(Long id, Long optUserId);

    /**
     * 重置密码
     *
     * @param userId
     * @param password
     * @param optUserId
     */
    void resetPassword(Long userId, String password, Long optUserId);

    /**
     * 修改密码，仅能修改操作人自身的密码
     *
     * @param oldPassword
     * @param newPassword
     * @param optUserId
     */
    void updatePassword(String oldPassword, String newPassword, Long optUserId);

    /**
     * 根据 ID 里查询用户姓名
     *
     * @param userIds
     * @return userId - realName
     */
    Map<Long, String> getUserRealNames(Collection<Long> userIds);

    /**
     * 根据 ID 里查询用户姓名
     * @param userId
     * @return
     */
    String getUserRealName(Long userId);

    /**
     * 更新用户在线状态
     *
     * @param userId
     * @param online true:在线, false: 离线
     */
    void updateOnlineStatus(Long userId, boolean online);

    /**
     * 根据部门ID列表取用户信息
     *
     * @param deptIds
     * @param userId
     * @param onlineOnly true: 仅要求获取在线的人员
     * @return
     */
    List<UserItemInfo> listUserItemByDeptIds(List<Long> deptIds,Long userId,boolean onlineOnly);
}
