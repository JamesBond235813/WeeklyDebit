package com.jhl.silver.union.commons.db;

import com.jhl.silver.union.commons.exception.BizExceptionUtils;
import com.jhl.silver.union.commons.func.SimpleBlocker;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;

/**
 * @author: qingren
 * @create_time: 2021/10/26
 */
public class TransactionUtils {

    /**
     * 执行DB事务逻辑。<br>
     * 若transBlock中未抛异常（只允许抛运行时异常）,则事务正常提交。<br>
     * 否则事务回滚，并向外部抛异常
     *
     * @param transactionTemplate
     * @param transBlock
     */
    public static void executeTransaction(TransactionTemplate transactionTemplate,
            SimpleBlocker transBlock) {
        Exception exception = transactionTemplate.execute(status -> {
            try {
                transBlock.process();
            } catch (Exception e) {
                status.setRollbackOnly();
                return e;
            }
            return null;
        });
        if (Objects.nonNull(exception)) {
            throw BizExceptionUtils.wrapException(exception);
        }
    }
}
