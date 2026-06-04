package com.jhl.silver.union.web.data.customer.hyy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class HyyAccessCheckResult {
    @JsonProperty("request_id")
    @SerializedName("request_id")
    private String requestId;
    @JsonProperty("company_name")
    @SerializedName("company_name")
    private String companyName;
    @JsonProperty("product_name")
    @SerializedName("product_name")
    private String productName;
    @JsonProperty("product_logo")
    @SerializedName("product_logo")
    private String productLogo;
    @JsonProperty("product_loan")
    @SerializedName("product_loan")
    private String productLoan;
    @JsonProperty("product_term")
    @SerializedName("product_term")
    private String productTerm;
    @JsonProperty("product_ratio")
    @SerializedName("product_ratio")
    private String productRatio;
    @JsonProperty("product_agreement")
    @SerializedName("product_agreement")
    private List<ProductAgreement> productAgreement;
    @JsonProperty("md5_list")
    @SerializedName("md5_list")
    private List<String> md5List;
    private Integer price;
    @JsonProperty("pre_url")
    @SerializedName("pre_url")
    private String preUrl;

    @Data
    @Accessors(chain = true)
    public static class ProductAgreement {
        private String name;
        private String url;
    }
}
