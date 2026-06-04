package com.jhl.silver.union.web.data.customer.hyy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "花易用八位撞库请求明文")
public class HyyAccessCheckRequest {
    @JsonProperty("phone_code")
    @SerializedName("phone_code")
    private String phoneCode;
    @JsonProperty("name_md5")
    @SerializedName("name_md5")
    private String nameMd5;
    @JsonProperty("idno_md5")
    @SerializedName("idno_md5")
    private String idnoMd5;
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
