package com.jhl.silver.union.biz.customer.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jhl.silver.union.biz.customer.dal.entity.CustDispatchDailyStatDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.Date;

/**
 * 数据分配每日统计 Mapper
 */
public interface CustDispatchDailyStatMapper extends BaseMapper<CustDispatchDailyStatDO> {
    @Update("UPDATE cust_dispatch_daily_stat SET auto_count = auto_count + 1, gmt_modified = NOW() " +
            "WHERE stat_date = #{statDate} AND user_id = #{userId} AND auto_count < #{limit}")
    int incrementAutoCount(@Param("statDate") Date statDate,
                           @Param("userId") Long userId,
                           @Param("limit") Integer limit);

    @Update("UPDATE cust_dispatch_daily_stat SET manual_count = manual_count + 1, gmt_modified = NOW() " +
            "WHERE stat_date = #{statDate} AND user_id = #{userId}")
    int incrementManualCount(@Param("statDate") Date statDate,
                             @Param("userId") Long userId);

    @Update("UPDATE cust_dispatch_daily_stat SET manual_count = manual_count + #{delta}, gmt_modified = NOW() " +
            "WHERE stat_date = #{statDate} AND user_id = #{userId}")
    int incrementManualCountBy(@Param("statDate") Date statDate,
                               @Param("userId") Long userId,
                               @Param("delta") Integer delta);
}
