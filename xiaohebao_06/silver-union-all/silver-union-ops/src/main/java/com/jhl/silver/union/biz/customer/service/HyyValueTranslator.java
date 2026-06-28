package com.jhl.silver.union.biz.customer.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class HyyValueTranslator {

    private HyyValueTranslator() {
    }

    public static String yesNo(Integer code) {
        if (Objects.equals(code, 1) || Objects.equals(code, 2)) {
            return "有";
        }
        if (Objects.equals(code, 3)) {
            return "无";
        }
        return "";
    }

    public static String house(Integer code) {
        if (Objects.equals(code, 1)) {
            return "有";
        }
        if (Objects.equals(code, 2)) {
            return "无";
        }
        return "";
    }

    public static String car(Integer car, Integer carPrice, Integer carStatus) {
        if (Objects.equals(car, 3)) {
            return "无";
        }
        if (!Objects.equals(car, 1) && !Objects.equals(car, 2)) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        parts.add("有");
        String price = carPrice(carPrice);
        if (!price.isBlank()) {
            parts.add(price);
        }
        String status = carStatus(carStatus);
        if (!status.isBlank()) {
            parts.add(status);
        }
        return String.join("|", parts);
    }

    public static String carStatus(Integer code) {
        if (Objects.equals(code, 1)) {
            return "全款车";
        }
        if (Objects.equals(code, 2)) {
            return "按揭车";
        }
        return "";
    }

    public static String carPrice(Integer code) {
        if (Objects.equals(code, 1)) {
            return "10万以下";
        }
        if (Objects.equals(code, 2)) {
            return "10-20万";
        }
        if (Objects.equals(code, 3)) {
            return "20-50万";
        }
        if (Objects.equals(code, 4)) {
            return "50万以上";
        }
        return "";
    }

    public static String loanAmount(Integer code) {
        if (Objects.equals(code, 1)) {
            return "1-3万";
        }
        if (Objects.equals(code, 2)) {
            return "3-5万";
        }
        if (Objects.equals(code, 3)) {
            return "5-10万";
        }
        if (Objects.equals(code, 4)) {
            return "10万以上";
        }
        return "";
    }

    public static Integer loanAmountUpperBound(Integer code) {
        if (Objects.equals(code, 1)) {
            return 30000;
        }
        if (Objects.equals(code, 2)) {
            return 50000;
        }
        if (Objects.equals(code, 3)) {
            return 100000;
        }
        if (Objects.equals(code, 4)) {
            return 150000;
        }
        return null;
    }

    public static String zhima(Integer code) {
        if (Objects.equals(code, 1)) {
            return "600以下";
        }
        if (Objects.equals(code, 2)) {
            return "600-650";
        }
        if (Objects.equals(code, 3)) {
            return "650-700";
        }
        if (Objects.equals(code, 4)) {
            return "700以上";
        }
        if (Objects.equals(code, 5)) {
            return "无芝麻分";
        }
        return "";
    }

    public static String overdue(Integer code) {
        if (Objects.equals(code, 1)) {
            return "有";
        }
        if (Objects.equals(code, 2)) {
            return "无";
        }
        return "";
    }

    public static String occupation(Integer code) {
        if (Objects.equals(code, 1)) {
            return "上班族";
        }
        if (Objects.equals(code, 2)) {
            return "个体户";
        }
        if (Objects.equals(code, 3)) {
            return "企业主";
        }
        if (Objects.equals(code, 4)) {
            return "自由职业";
        }
        return "";
    }

    public static String insurance(Integer code) {
        if (Objects.equals(code, 1)) {
            return "一年以上";
        }
        if (Objects.equals(code, 2)) {
            return "一年以下";
        }
        if (Objects.equals(code, 3)) {
            return "无";
        }
        return "";
    }

    public static String socialSecurity(Integer code) {
        if (Objects.equals(code, 1)) {
            return "6个月以上";
        }
        if (Objects.equals(code, 2)) {
            return "6个月以下";
        }
        if (Objects.equals(code, 3)) {
            return "无";
        }
        return "";
    }
}
