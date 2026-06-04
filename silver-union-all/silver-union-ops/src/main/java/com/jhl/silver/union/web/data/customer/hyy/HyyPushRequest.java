package com.jhl.silver.union.web.data.customer.hyy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class HyyPushRequest {
    @JsonProperty("request_id")
    @SerializedName("request_id")
    private String requestId;
    private String phone;
    private String name;
    private String idno;
    @JsonProperty("working_city")
    @SerializedName("working_city")
    private String workingCity;
    private Integer age;
    private Integer sex;
    private Integer gjj;
    private Integer shebao;
    @JsonProperty("loan_amount")
    @SerializedName("loan_amount")
    private Integer loanAmount;
    private Integer house;
    private Integer car;
    @JsonProperty("car_status")
    @SerializedName("car_status")
    private Integer carStatus;
    @JsonProperty("car_price")
    @SerializedName("car_price")
    private Integer carPrice;
    private Integer overdue;
    private Integer zhima;
    private Integer occupation;
    private Integer insurance;
    private String ip;
}
