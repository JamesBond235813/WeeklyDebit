package com.jhl.silver.union.biz.region.service;

import com.jhl.silver.union.biz.data.IdCardAreaInfo;

public interface IdCardAreaService {
    IdCardAreaInfo getAreaInfoByIdCard(String idCardNo);

    IdCardAreaInfo getAreaInfoByCode(String areaCode);
}
