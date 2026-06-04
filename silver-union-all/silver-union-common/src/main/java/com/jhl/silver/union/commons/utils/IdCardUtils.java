package com.jhl.silver.union.commons.utils;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

/**
 * Simple ID card parser for China mainland 18-digit IDs.
 */
public final class IdCardUtils {
    private static final String ID_CARD_REGEX = "[0-9]{17}([0-9]|[xX])";
    private static final DateTimeFormatter BASIC_DATE = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private IdCardUtils() {
    }

    public static IdCardInfo parse(String idCardNo) {
        String normalized = StringUtils.upperCase(StringUtils.trim(idCardNo));
        if (StringUtils.isBlank(normalized) || !normalized.matches(ID_CARD_REGEX)) {
            return null;
        }
        try {
            String birthPart = normalized.substring(6, 14);
            LocalDate birthDate = LocalDate.parse(birthPart, BASIC_DATE);
            int seq = Character.getNumericValue(normalized.charAt(16));
            int sex = (seq % 2 == 0) ? 2 : 1;
            int age = Period.between(birthDate, LocalDate.now()).getYears();
            if (age <= 0 || age > 150) {
                return null;
            }
            return new IdCardInfo(birthDate.format(DATE_FORMAT), sex, age);
        } catch (Exception ex) {
            return null;
        }
    }

    public static final class IdCardInfo {
        private final String birthday;
        private final Integer sex;
        private final Integer age;

        public IdCardInfo(String birthday, Integer sex, Integer age) {
            this.birthday = birthday;
            this.sex = sex;
            this.age = age;
        }

        public String getBirthday() {
            return birthday;
        }

        public Integer getSex() {
            return sex;
        }

        public Integer getAge() {
            return age;
        }
    }
}
