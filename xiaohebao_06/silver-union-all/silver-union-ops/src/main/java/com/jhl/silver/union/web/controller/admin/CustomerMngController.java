package com.jhl.silver.union.web.controller.admin;

import com.github.pagehelper.PageInfo;
import com.jhl.silver.union.biz.common.ResultCode;
import com.jhl.silver.union.biz.common.utils.AesUtils;
import com.jhl.silver.union.biz.config.BizProperty;
import com.jhl.silver.union.biz.customer.service.CustomerInfoService;
import com.jhl.silver.union.biz.customer.service.ImportCustDataService;
import com.jhl.silver.union.biz.data.ImportCustCmd;
import com.jhl.silver.union.biz.data.ImportFileProcInfo;
import com.jhl.silver.union.biz.data.UserContext;
import com.jhl.silver.union.commons.CommonResultCode;
import com.jhl.silver.union.commons.SuResult;
import com.jhl.silver.union.commons.exception.BizException;
import com.jhl.silver.union.commons.utils.SuResultUtils;
import com.jhl.silver.union.commons.utils.VerifyUtils;
import com.jhl.silver.union.web.data.ImportFileRecordDTO;
import com.jhl.silver.union.web.data.PagedListImportRecordRequest;
import com.jhl.silver.union.web.data.admin.AddCustomerInfoRequest;
import com.jhl.silver.union.web.data.admin.DispatchCustomerInfoRequest;
import com.jhl.silver.union.web.data.admin.PushCustInfoItem;
import com.jhl.silver.union.web.data.admin.PushCustInfoRequest;
import com.jhl.silver.union.web.data.admin.UpdCustomerInfoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

/**
 * 客户信息维护类接口
 *
 * @author: qingren
 * @create_time: 2025/4/2
 */
@RestController
@Tag(name = "客户信息维护类接口")
@RequestMapping("/sys/cust")
@Slf4j
public class CustomerMngController {
    @Resource
    private ImportCustDataService importCustDataService;
    @Resource
    private CustomerInfoService customerInfoService;
    @Autowired
    private BizProperty bizProperty;

    /**
     * 新增客户信息
     *
     * @param request
     * @return
     */
    @Operation(summary = "新增客户信息")
    @PostMapping("/add-cust")
    public SuResult<Void> addCustomerInfo(@RequestBody AddCustomerInfoRequest request) {
        customerInfoService.addCustomerFactInfo(request, UserContext.getUserId());
        return SuResultUtils.successResult();
    }

    /**
     * 更新客户信息
     *
     * @param request
     * @return
     */
    @Operation(summary = "更新客户信息")
    @PostMapping("/upd-cust")
    public SuResult<Void> updCustomerInfo(@RequestBody UpdCustomerInfoRequest request) {
        customerInfoService.updateCustomerFactInfo(request, UserContext.getUserId());
        return SuResultUtils.successResult();
    }

    /**
     * 分配客户信息
     *
     * @return
     */
    @Operation(summary = "分配客户信息")
    @PostMapping("/dispatch-cust")
    public SuResult<Void> dispatchCustomerInfo(@RequestBody DispatchCustomerInfoRequest request) {
        customerInfoService.dispatchCustomer(request, UserContext.getUserId(), UserContext.getDeptId(),
                UserContext.getRoles());
        return SuResultUtils.successResult();
    }

    /**
     * 上传文件导入用户信息
     *
     * @param file
     * @return
     */
    @Operation(summary = "上传文件导入用户信息")
    @PostMapping(value = "/import-user-info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuResult<List<String>> importUserInfoByFile(
            @Parameter(name = "file", description = "上传的文件", required = true)
            @RequestPart(name = "file") MultipartFile file,
            @Parameter(name = "targetDeptId", description = "分配数据的目标部门ID")
            @RequestParam(name = "targetDeptId", required = false) Long targetDeptId,
            @Parameter(name = "targetUserId", description = "分配数据的目标用户ID")
            @RequestParam(name = "targetUserId", required = false, defaultValue = "0") Long targetUserId
    ) {
        ImportCustCmd cmd = new ImportCustCmd()
                .setFile(file)
                .setOptUserId(UserContext.getUserId())
                .setOptDeptId(UserContext.getDeptId())
                .setTargetUserId(targetUserId)
                .setTargetDeptId(targetDeptId)
                .setRoles(UserContext.getRoles());
        ImportFileProcInfo info = importCustDataService.saveAndValidateFile(cmd);
        if (!CollectionUtils.isEmpty(info.getValidateErrorMsgs())) {
            return SuResultUtils.<List<String>>failedResult(ResultCode.IMPORT_CUST_FAILED)
                    .setData(info.getValidateErrorMsgs());
        }
        // 异步处理导入数据
        importCustDataService.asyncImportCustInfo(info);
        return SuResultUtils.successResult();
    }

    /**
     * 分页获取上传文件记录
     *
     * @return
     */
    @Operation(summary = "分页获取上传文件记录")
    @PostMapping(value = "/paged-list-imp-rec")
    public SuResult<PageInfo<ImportFileRecordDTO>> pagedListImportRecord(
            @RequestBody PagedListImportRecordRequest request) {

        PageInfo<ImportFileRecordDTO> pageInfo =
                importCustDataService.pagedListImportRecords(request, UserContext.getUserId(), UserContext.getDeptId(),
                        UserContext.getRoles());
        return SuResultUtils.successResult(pageInfo);
    }

    /**
     * 下载指定导入记录的已存在客户信息文件
     *
     * @param id
     * @return
     */
    @Operation(summary = "下载指定导入记录的已存在客户信息文件")
    @GetMapping("/download-existed-rec")
    public ResponseEntity<byte[]> downloadExistedCustRecFile(@RequestParam("id") Long id) {
        return importCustDataService.downloadExistedCustFile(id, UserContext.getUserId(), UserContext.getDeptId(),
                UserContext.getRoles());
    }

    /**
     * 下载导入模板文件
     *
     * @return
     */
    @Operation(summary = "下载导入模板文件")
    @GetMapping("/download-tmpl")
    public ResponseEntity<org.springframework.core.io.Resource> downloadImportTemplateFile() {
        String path = bizProperty.getImportCustTemplateFilePath();
        try {
            Path filePath = Path.of(path).toAbsolutePath().normalize();
            org.springframework.core.io.Resource resource = new FileSystemResource(filePath);
            if (!resource.exists() || !resource.isFile() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment",
                    URLEncoder.encode(filePath.getFileName().toString(), StandardCharsets.UTF_8));
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (Exception e) {
            log.error("下载客户信息模板失败.", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 推送客户信息。 客户信息若已存在，则更新渠道、来源文件以及申请时间
     *
     * @return
     */
    @Operation(summary = "推送客户信息。 客户信息若已存在，则更新渠道、来源文件以及申请时间")
    @PostMapping("/push-cust-info")
    public SuResult<Void> pushCustInfo(@RequestBody PushCustInfoRequest request) {
        request.validate();
        if (!StringUtils.equals(bizProperty.getExtraToken(), request.getToken())) {
            throw new BizException(CommonResultCode.INVALID_REQUEST, "invalid token: " + request.getToken());
        }
        if (request.isAesEncrypted()) {
            decryptPushItems(request);
        }
        importCustDataService.addCustInfo(request.getItemList(), request.getTargetDeptId(), request.getTargetUserId());
        return SuResultUtils.successResult();
    }

    private void decryptPushItems(PushCustInfoRequest request) {
        String aesKey = StringUtils.trimToEmpty(bizProperty.getPushCustAesKey());
        VerifyUtils.notBlank(aesKey, "pushCustAesKey", true);
        if (!isValidAesKeyLength(aesKey)) {
            throw new BizException(CommonResultCode.INVALID_PARAMS, "pushCustAesKey长度不合法");
        }
        for (PushCustInfoItem item : request.getItemList()) {
            if (item == null) {
                continue;
            }
            if (StringUtils.isNotBlank(item.getName())) {
                item.setName(AesUtils.decryptWithAesECB(item.getName(), aesKey));
            }
            if (StringUtils.isNotBlank(item.getMobile())) {
                item.setMobile(AesUtils.decryptWithAesECB(item.getMobile(), aesKey));
            }
            if (StringUtils.isNotBlank(item.getIdCardNo())) {
                item.setIdCardNo(AesUtils.decryptWithAesECB(item.getIdCardNo(), aesKey));
            }
        }
    }

    private boolean isValidAesKeyLength(String key) {
        int len = key.getBytes(StandardCharsets.UTF_8).length;
        return len == 16 || len == 24 || len == 32;
    }
}
