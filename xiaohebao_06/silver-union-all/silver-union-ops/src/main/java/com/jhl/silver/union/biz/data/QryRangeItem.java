package com.jhl.silver.union.biz.data;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Objects;

/**
 * 区间搜索条件项
 *
 * @param <T>
 */
@Data
@Accessors(chain = true)
public class QryRangeItem<T> {
    /**
     * 区间左值
     */
    private T rangeBegin;
    /**
     * 区间右值
     */
    private T rangeEnd;

    /**
     * 开闭区间标识 true: 前闭， false: 前开
     */
    private boolean rangeBeginInclusive;
    /**
     * 开闭区间标识 true: 后闭， false: 后开
     */
    private boolean rangeEndInclusive;

    /**
     * 构造闭区间查询条件
     *
     * @param rangeBegin
     * @param rangeEnd
     * @param <T>
     * @return
     */

    public static <T> QryRangeItem<T> of(T rangeBegin, T rangeEnd) {
        QryRangeItem<T> item = new QryRangeItem<T>()
                .setRangeBegin(rangeBegin)
                .setRangeEnd(rangeEnd)
                .setRangeBeginInclusive(true)
                .setRangeEndInclusive(true);
        return item;
    }

    public <E> void setupQryWrapper(LambdaQueryWrapper<E> wrapper, SFunction<E, ?> column) {
        if (Objects.isNull(wrapper) || Objects.isNull(column)) {
            return;
        }
        if (Objects.nonNull(this.rangeBegin)) {
            if (this.rangeBeginInclusive) {
                wrapper.ge(column, this.rangeBegin);
            } else {
                wrapper.gt(column, this.rangeBegin);
            }
        }
        if (Objects.nonNull(this.rangeEnd)) {
            if (this.rangeEndInclusive) {
                wrapper.le(column, this.rangeEnd);
            } else {
                wrapper.lt(column, this.rangeEnd);
            }
        }
    }
}
