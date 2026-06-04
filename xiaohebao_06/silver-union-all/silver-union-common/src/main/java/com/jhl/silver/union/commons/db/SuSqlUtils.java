package com.jhl.silver.union.commons.db;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jhl.silver.union.commons.CommonResultCode;
import com.jhl.silver.union.commons.exception.BizException;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * SQL语句工具类
 *
 * @author qingren
 * @date 2019/9/16 11:59 AM
 */
public class SuSqlUtils {
    /**
     * 通用分页查询代码块<br>
     * 当且仅当 pageNum=0, pageSize=Integer.MAX_VALUE 时，不进行分页查询，并返回全量匹配的数据
     *
     * @param pageNum       页码
     * @param pageSize      单页数据量
     * @param querySupplier 返回数据的提供者。即查询语句
     * @param <T>           分页查询返回的数据对象类型
     * @return
     */
    public static <T> PageInfo<T> pagedListQuery(int pageNum, int pageSize,
            Supplier<List<T>> querySupplier)            {
        return pagedListQuery(pageNum, pageSize, true, querySupplier);
    }

    /**
     * 通用分页查询代码块<br>
     * 当且仅当 pageNum=0, pageSize=Integer.MAX_VALUE 时，不进行分页查询，并返回全量匹配的数据
     *
     * @param pageNum       页码
     * @param pageSize      单页数据量
     * @param needTotalCnt  true: 返回总数据量
     * @param querySupplier 返回数据的提供者。即查询语句
     * @param <T>           分页查询返回的数据对象类型
     * @return
     * @
     */
    public static <T> PageInfo<T> pagedListQuery(int pageNum, int pageSize, boolean needTotalCnt,
            Supplier<List<T>> querySupplier)
             {
        if (querySupplier == null) {
            throw new BizException(CommonResultCode.SYSTEM_ERROR, "SuSqlUtils.pagedListQuery.querySupplier is null");
        }

        //修正页码
        if (pageNum < 1) {
            pageNum = 1;
        }

        PageInfo<T> pageInfo = null;
        if (pageSize < 1) {
            pageInfo = new PageInfo<>(Collections.emptyList());
            pageInfo.setPageNum(pageNum);
            pageInfo.setPageSize(0);
            pageInfo.setTotal(0);
            return pageInfo;
        }
        //当且仅当 pageNum=1, pageSize=Integer.MAX_VALUE 时，不进行分页查询，并返回全量匹配的数据
        //若未满足取全量数据的情况，由PageHelper修正，不再重复造轮子
        PageHelper.startPage(pageNum, pageSize, needTotalCnt);
        pageInfo = new PageInfo<>(querySupplier.get());
        return pageInfo;
    }

    /**
     * 在value前后增加百分号符号
     *
     * @param value
     * @return
     */
    public static final String addPercentAround(String value) {
        return "%" + value + "%";
    }

    /**
     * 构造空的分页返回信息
     *
     * @param pageNum
     * @param pageSize
     * @param <T>
     * @return
     */
    public static <T> PageInfo<T> blankPageInfo(int pageNum, int pageSize) {
        PageInfo<T> pi = new PageInfo<>();
        pi.setPageNum(pageNum);
        pi.setPageSize(pageSize);
        pi.setList(Collections.emptyList());
        return pi;
    }
}
