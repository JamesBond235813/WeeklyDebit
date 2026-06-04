package com.jhl.silver.union.biz.data.sw;

/**
 * 邵文数据平台客户线索状态枚举
 *
 * @author qingren
 * @create_time 2025/12/6
 */
public enum SwClueStatusEnum {
/**
     * 有效线索
     */
    VALID_CLUE("1001", "有效线索"),
    
    /**
     * 跟进中
     */
    FOLLOWING_UP("1002", "跟进中"),
    
    /**
     * 待拨打
     */
    TO_CALL("1003", "待拨打"),
    
    /**
     * 已上门
     */
    VISITED("1004", "已上门"),
    
    /**
     * 已签单
     */
    SIGNED("1005", "已签单"),
    
    /**
     * 客户有意愿
     */
    CUSTOMER_INTERESTED("1009", "客户有意愿"),
    
    /**
     * 预审通过
     */
    PRE_APPROVED("1010", "预审通过"),
    
    /**
     * 终审通过
     */
    FINAL_APPROVED("1011", "终审通过"),
    
    /**
     * 订单修改
     */
    ORDER_MODIFIED("1012", "订单修改"),
    
    /**
     * 订单删除
     */
    ORDER_DELETED("1007", "订单删除"),
    
    /**
     * 线索重复
     */
    DUPLICATE_CLUE("2001", "线索重复"),
    
    /**
     * 线索接收其他失败
     */
    RECEIVE_FAILED("2002", "线索接收其他失败"),
    
    /**
     * 预审拒绝
     */
    PRE_REJECTED("2003", "预审拒绝"),
    
    /**
     * 客户无意向
     */
    CUSTOMER_NOT_INTERESTED("2004", "客户无意向"),
    
    /**
     * 终审拒绝
     */
    FINAL_REJECTED("2005", "终审拒绝"),
    
    /**
     * 已放款
     */
    LOAN_DISBURSED("1006", "已放款"),
    
    /**
     * 放款订单修改（用于订单对账异常）
     */
    LOAN_ORDER_MODIFIED("1008", "放款订单修改（用于订单对账异常）");
    public final String status;
    public final String desc;

    SwClueStatusEnum(String status, String desc) {
        this.status = status;
        this.desc = desc;
    }
}
