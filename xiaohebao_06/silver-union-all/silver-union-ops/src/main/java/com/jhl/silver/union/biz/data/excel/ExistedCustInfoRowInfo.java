package com.jhl.silver.union.biz.data.excel;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * @author: qingren
 * @create_time: 2025/5/8
 */
@ExcelIgnoreUnannotated
@Data
public class ExistedCustInfoRowInfo {

    /**
     * 客户信息 ID
     */
    private Long custId;

    /**
     * 原始文件中的行号
     */
    @ExcelProperty(value = "原始文件行号")
    @ColumnWidth(20)
    private Integer oriLineNo;
    /**
     * 客户姓名
     */
    @ExcelProperty(value = "客户姓名")
    @ColumnWidth(20)
    private String name;
    /**
     * 手机号
     */
    @ExcelProperty(value = "手机号")
    @ColumnWidth(20)
    private String mobile;

    private String channelName;

    public static ExistedCustInfoRowInfo of(Long custId, Integer oriLineNo, String name, String mobile,String channelName) {
        ExistedCustInfoRowInfo info = new ExistedCustInfoRowInfo();
        info.setCustId(custId);
        info.setOriLineNo(oriLineNo);
        info.setName(name);
        info.setMobile(mobile);
        info.setChannelName(channelName);
        return info;
    }

    public String toCsvString() {
        return custId + "," + oriLineNo + "," + name + "," + mobile;
    }
}
