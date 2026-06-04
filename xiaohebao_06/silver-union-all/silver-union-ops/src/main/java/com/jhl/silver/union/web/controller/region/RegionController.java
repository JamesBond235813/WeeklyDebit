package com.jhl.silver.union.web.controller.region;

import com.jhl.silver.union.commons.SuResult;
import com.jhl.silver.union.commons.utils.SuResultUtils;
import com.jhl.silver.union.biz.data.IdCardAreaInfo;
import com.jhl.silver.union.biz.region.service.IdCardAreaService;
import com.jhl.silver.union.biz.region.service.RegionAreaService;
import com.jhl.silver.union.web.data.region.RegionItemDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Region data")
@RequestMapping("/region")
public class RegionController {

    @Resource
    private RegionAreaService regionAreaService;
    @Resource
    private IdCardAreaService idCardAreaService;

    @GetMapping("/children")
    @Operation(summary = "List region children by parent id")
    public SuResult<List<RegionItemDTO>> listChildren(
            @Parameter(name = "parentId", description = "Parent id, default 0")
            @RequestParam(name = "parentId", required = false) Long parentId) {
        return SuResultUtils.successResult(regionAreaService.listChildren(parentId));
    }

    @GetMapping("/id-card-area")
    @Operation(summary = "Resolve ID card area by id card number")
    public SuResult<IdCardAreaInfo> getIdCardArea(
            @Parameter(name = "idCardNo", description = "ID card number", required = true)
            @RequestParam(name = "idCardNo") String idCardNo) {
        return SuResultUtils.successResult(idCardAreaService.getAreaInfoByIdCard(idCardNo));
    }
}
