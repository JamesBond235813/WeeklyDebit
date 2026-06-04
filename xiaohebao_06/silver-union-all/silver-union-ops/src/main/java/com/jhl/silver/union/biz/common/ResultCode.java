package com.jhl.silver.union.biz.common;

import com.jhl.silver.union.commons.IResultCode;

/**
 * @author: qingren
 * @create_time: 2025/3/16
 */
public enum ResultCode implements IResultCode {
    /**
     * 用户名或密码输入不正确
     */
    USER_OR_PASSWORD_INVALID(400, "您输入的用户名或密码不正确"),
    UNAUTHENTICATED(401, "请登录后再操作"),
    UNAUTHENTICATED_BY_ANTHER_AUTH(401,
            "您已在别处登录，若非您本人操作，则有可能您的密码已被泄露，请登录后及时修改密码，或联系管理员重置密码。"),
    SMS_AUTHENTICATION_FAILED(402, "手机验证码不正确，请确认后再操作"),
    SMS_TOKEN_NOT_EXIST(403, "手机验证码不正确或验证码已过期，请重新获取短信验证码"),
    LOGIN_FAIL_LIMIT(404, "登录失败达到最大次数！不可操作"),

    CHANGE_PASSWORD_FAILED(410, "修改密码失败"),
    SAME_PASSWORD(412, "新密码不可与旧密码相同"),
    OLD_PASSWORD_INVALID(413, "原密码不正确"),
    CHANGE_PASSWORD_FAILED_4_NONE_ORI_PSWD(414, "您未设置过密码，请先设置初始密码，或重置密码"),
    RESET_PASSWORD_FAILED_4_USER_NOT_EXIST(415, "用户信息不存在，不可重置密码"),
    RESET_PASSWORD_FAILED_4_USER_NOT_OK(416, "用户被冻结，不可重置密码"),
    USER_REGISTER_DUPLICATED_USERNAME(422, "该用户名已被注册,请更换用户名"),
    USER_BIRTHDAY_LATER_THAN_TODAY(423, "生日不可晚于当前日期"),
    LOGIN_USER_NOT_EXISTS(424, "您输入的用户名或密码不正确"),
    /**
     * 客户端ID不存在
     */
    INVALID_GRANT_NOT_EXISTED_CLIENT_ID(496, "非法请求"),
    /**
     * 客户端ID不正确
     */
    INVALID_GRANT_WRONG_CLIENT_ID(497, "非法请求"),
    UNSUPPORTED_GRANT_TYPE(498, "不支持此认证类型"),
    AUTHENTICATION_FAILED(499, "用户认证失败"),

    // 业务配置相关信息
    ADD_FAILED_FOR_DUPLICATED_BIZ_TYPE_VALUE(500,
            "业务字典类型: {0}, 字典值: {1}, 已存在，不可重复新增。请确认后重新操作"),

    //用户相关错误信息
    USER_NOT_EXIST(701, "用户信息不存在或已被注销"),
    USER_STATUS_NOT_OK(702, "用户已被冻结，无法操作"),
    USERNAME_ALREADY_EXISTED(703, "用户名已存在，请确认后再操作"),
    USER_ALREADY_EXISTED(704, "用户名({0})或手机号({1})已存在，请确认后再操作"),
    // 部门相关信息
    SAVE_DEPT_FAILED(800, "保存部门信息失败： {0}"),
    NOT_ALLOWED_DELETE_FOR_EXISTING_CHILDREN(801, "【{0}】包含下级部门，不可删除"),
    // 客户相关信息
    CUST_NOT_FOUND(900, "未找到指定的客户信息，请确认后再操作"),
    CUST_NOT_CHANGED(901, "客户信息字段未修改，无需保存"),
    CUST_UPDATE_FAILED_4_VERSION(902, "更新客户信息失败，数据可能已被他人更新，请刷新数据确认后再操作"),
    CUST_INFO_ALREADY_EXISTS(903, "客户信息已存在，不可重复保存。姓名:{0} , 手机号: {1}"),
    CUST_DISPATCH_FAILED_OUT_AUTH(904, "无权分配客户信息"),
    CUST_ADD_COMMENT_FAILED_OUT_AUTH(905, "无权追加评价信息"),
    CUST_CANT_NOT_EDIT_4_OCEAN(906, "不可编辑公海客户的业务信息，请确认后再操作"),
    CUST_CANT_NOT_EDIT_4_NO_AUTH(907, "该客户不在您的同级或下级部门，拒绝编辑操作"),
    CUST_BATCH_DISPATCH_FAILED_4_NOT_AUTH(908, "包含无权分配的客户信息，请确认后再操作"),
    IMPORT_CUST_FAILED_4_NOT_AUTH(909, "没有权限导入客户信息，请确认后再操作"),
    IMPORT_CUST_FAILED_4_BEYOND_DEPT(910, "无权向目标部门分配数据，请确认后再操作"),
    IMPORT_CUST_FAILED_4_TARGET_USER_DEPT_MISMATCH(911, "分配数据的目标部门与用户不匹配，请确认后再操作"),
    IMPORT_CUST_FAILED_4_DUPLICATED_FILE_NAME(912, "该文件已处理，不可重复导入，请确认后再操作"),
    IMPORT_CUST_FAILED_4_BAD_TMPL_VERSION(913, "导入文件的模板信息不正确，请下载最新模板后再操作"),
    IMPORT_CUST_FAILED_4_OVER_MAX_CNT(914, "单次导入不可超过{0}条数据"),
    IMPORT_CUST_FAILED(915, "导入客户信息失败。"),
    SYS_NO_AUTH(916, "无权执行该操作"),
    RISK_REPORT_NO_AUTH(917, "无权查询该客户风控报告"),
    RISK_REPORT_SEARCH_UNABLED(918, "暂未开放风控报告查询功能"),

    //下载文件相关
    DOWNLOAD_FAILED(940, "下载文件失败"),
    DOWNLOAD_FAILED_4_FILE_NO_AUTH(941, "无权下载该文件"),
    DOWNLOAD_FAILED_4_FILE_NOT_FOUND(942, "未找到对应文件"),

    //允许操作相关
    DURATION_OPTS_FORBIDDEN(1001, "当前时间不可操作"),
    // 导入数据
    WRITE_FILE_FAILED(1100, "保存文件失败"),
    // 接收推送数据相关
    DECRYPT_FAILED(1200, "解密数据失败"),
    ENCRYPT_FAILED(1201, "加密数据失败"),
    ;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 错误码
     */
    public final int code;
    /**
     * 错误信息
     */
    public final String msg;

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }
}
