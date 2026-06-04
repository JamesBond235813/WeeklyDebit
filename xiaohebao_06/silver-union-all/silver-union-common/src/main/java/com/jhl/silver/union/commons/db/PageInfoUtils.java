package com.jhl.silver.union.commons.db;

import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Objects;

/**
 * PageInfo 相关的工具类型
 *
 * @author: qingren
 * @create_time: 2023/8/22
 */
public class PageInfoUtils {

    public static <T> PageInfo<T> copyPageInfoWithoutListFrom(PageInfo<?> source) {
        if (Objects.isNull(source)) {
            return null;
        }
        PageInfo<T> pageInfo = new PageInfo<>();
        BeanUtils.copyProperties(source, pageInfo, "list");
        return pageInfo;
    }

    /**
     * 判断是否为空数据
     *
     * @param pageInfo
     * @return
     */
    public static boolean isEmpty(PageInfo<?> pageInfo) {
        return Objects.isNull(pageInfo) || Objects.isNull(pageInfo.getList()) || pageInfo.getList().isEmpty();
    }

    public static <T> PageInfo<T> blankPageInfo() {
        PageInfo<T> pageInfo = new PageInfo<>();
        pageInfo.setList(List.of());
        return pageInfo;
    }
}
