package com.jhl.silver.union.biz.customer.service.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.read.listener.ReadListener;
import com.google.common.collect.Maps;
import com.jhl.silver.union.biz.common.ResultCode;
import com.jhl.silver.union.biz.data.excel.CustomerInfoExcelRowInfo;
import com.jhl.silver.union.commons.exception.BizException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 读取 Excel内容，并进行数据合法性校验
 *
 * @author: qingren
 * @create_time: 2025/5/7
 */
public class CustomerExcelReadListener implements ReadListener<CustomerInfoExcelRowInfo> {
    private final List<CustomerInfoExcelRowInfo> list;
    private boolean validVersion = false;
    private Map<String, CustomerInfoExcelRowInfo> repo;
    private List<String> errorMsgList;
    private long count = 0;

    public CustomerExcelReadListener(List<CustomerInfoExcelRowInfo> list) {
        this.list = list;
        this.repo = Maps.newHashMap();
        this.errorMsgList = new ArrayList<>();

    }

    @Override
    public void invoke(CustomerInfoExcelRowInfo info, AnalysisContext analysisContext) {
        if (!validVersion) {
            throw new BizException(ResultCode.IMPORT_CUST_FAILED_4_BAD_TMPL_VERSION, "Excel 导入模板版本校验不通过");
        }
        if (info.isEmpty()) {
            return;
        }
        count++;
        if (count > CustomerInfoExcelRowInfo.MAX_RECORD_CNT) {
            throw BizException.makeupBy(ResultCode.IMPORT_CUST_FAILED_4_OVER_MAX_CNT,
                    List.of(CustomerInfoExcelRowInfo.MAX_RECORD_CNT + ""),
                    "单次导入数据量超过最大行数: " + CustomerInfoExcelRowInfo.MAX_RECORD_CNT);
        }
        info.setLineNo(analysisContext.readRowHolder().getRowIndex() + 1);
        String errorMsg = info.validateAndReturnErrMsg();
        if (StringUtils.isNotBlank(errorMsg)) {
            this.errorMsgList.add(errorMsg);
        }
        String key = info.getName() + info.getMobile();
        CustomerInfoExcelRowInfo exsited = repo.get(key);
        if (exsited != null) {
            this.errorMsgList.add(
                    "第" + info.getLineNo() + "行：客户信息与第" + exsited.getLineNo() + "行重复，请清除重复数据后再上传");
            return;
        }
        repo.put(key, info);
        list.add(info);
    }

    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        ReadCellData<?> data = headMap.get(CustomerInfoExcelRowInfo.VERSION_KEY_COLUMN);
        if (!StringUtils.contains(data.getStringValue(), CustomerInfoExcelRowInfo.TEMPLATE_VERSION_KEYWORD)) {
            return;
        }
        data = headMap.get(CustomerInfoExcelRowInfo.VERSION_VALUE_COLUMN);
        if (StringUtils.equals(data.getStringValue(), CustomerInfoExcelRowInfo.VERSION)) {
            this.validVersion = true;
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        this.repo.clear();
    }

    public List<String> getErrorMsgList() {
        return errorMsgList;
    }

    public long getCount() {
        return count;
    }
}
