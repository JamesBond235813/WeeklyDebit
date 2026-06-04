package com.jhl.silver.union.biz.customer.service.impl;

import com.alibaba.excel.EasyExcel;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jhl.silver.union.biz.common.ResultCode;
import com.jhl.silver.union.biz.common.enums.BizDictConfigTypeEnum;
import com.jhl.silver.union.biz.common.enums.ImportProcStatusEnum;
import com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum;
import com.jhl.silver.union.biz.common.utils.BizHelper;
import com.jhl.silver.union.biz.common.utils.RoleUtils;
import com.jhl.silver.union.biz.config.BizProperty;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerImportRecordDO;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerInfoItemDO;
import com.jhl.silver.union.biz.customer.manager.CustomerImportRecordManager;
import com.jhl.silver.union.biz.customer.manager.CustomerInfoItemManager;
import com.jhl.silver.union.biz.customer.service.BizConfigService;
import com.jhl.silver.union.biz.customer.service.CustDispatchService;
import com.jhl.silver.union.biz.customer.service.CustNoticeService;
import com.jhl.silver.union.biz.customer.service.ImportCustDataService;
import com.jhl.silver.union.biz.customer.service.MobileAreaService;
import com.jhl.silver.union.biz.customer.service.excel.CustomerExcelReadListener;
import com.jhl.silver.union.biz.data.CustImpRecQry;
import com.jhl.silver.union.biz.data.CustomerInfoQry;
import com.jhl.silver.union.biz.data.ImportCustCmd;
import com.jhl.silver.union.biz.data.ImportFileProcInfo;
import com.jhl.silver.union.biz.data.MobileAreaInfo;
import com.jhl.silver.union.biz.data.convert.CustImportRecordConvert;
import com.jhl.silver.union.biz.data.excel.CustomerInfoExcelRowInfo;
import com.jhl.silver.union.biz.data.excel.ExistedCustInfoRowInfo;
import com.jhl.silver.union.biz.dept.service.DeptService;
import com.jhl.silver.union.biz.user.service.UserService;
import com.jhl.silver.union.commons.CommonConstant;
import com.jhl.silver.union.commons.db.PageInfoUtils;
import com.jhl.silver.union.commons.db.SuSqlUtils;
import com.jhl.silver.union.commons.exception.BizException;
import com.jhl.silver.union.commons.exception.ExceptionLogPrinter;
import com.jhl.silver.union.commons.gson.GsonHelper;
import com.jhl.silver.union.commons.utils.OtherUtils;
import com.jhl.silver.union.commons.utils.VerifyUtils;
import com.jhl.silver.union.web.data.BizDictItem;
import com.jhl.silver.union.web.data.ImportFileRecordDTO;
import com.jhl.silver.union.web.data.PagedListImportRecordRequest;
import com.jhl.silver.union.web.data.admin.PushCustInfoItem;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: qingren
 * @create_time: 2025/4/28
 */
@Service
@Slf4j
public class ImportCustDataServiceImpl implements ImportCustDataService {

    @Resource
    private BizProperty bizProperty;
    @Resource
    private DeptService deptService;
    @Resource
    private UserService userService;
    @Resource
    private BizConfigService bizConfigService;
    @Resource
    private CustDispatchService custDispatchService;
    @Resource
    private CustNoticeService custNoticeService;
    @Resource
    private CustomerInfoItemManager customerInfoItemManager;
    @Resource
    private CustomerImportRecordManager customerImportRecordManager;
    @Resource
    private CustImportRecordConvert customerConvert;
    @Resource
    private MobileAreaService mobileAreaService;

    @Override
    public String saveFile(MultipartFile file) {
        VerifyUtils.verifyTrue(Objects.nonNull(file) && !file.isEmpty(), "文件为空，拒绝操作", true);
        String oriFileName = file.getOriginalFilename();
        String realFileName = BizHelper.generateUniqueFileName(oriFileName, null);
        String path = bizProperty.getUploadTmpDir() + File.separator + realFileName;
        try {
            File outputFile = new File(path);
            OutputStream outputStream = new FileOutputStream(outputFile);
            file.getInputStream().transferTo(outputStream);
        } catch (IOException e) {
            log.error("写入文件[{}]失败.", realFileName, e);
            throw new BizException(ResultCode.WRITE_FILE_FAILED, "oriFileName: " + oriFileName + ". " + e.getMessage());
        }
        return path;
    }

    @Override
    public synchronized ImportFileProcInfo saveAndValidateFile(ImportCustCmd cmd) {
        // 方法签名中设置synchronized， 先按单机的方式简易处理
        this.validate4Import(cmd);
        log.info("开始校验上传文件的信息=> {}", cmd.getOriFileName());
        // 判断文件是否已经导入成功或在导入中
        this.checkDuplicateImport(cmd);
        // 保存文件内容
        String path = this.saveFile(cmd.getFile());
        ImportFileProcInfo info = cmd.makeupImportFileProcInfo(path);
        // 校验文件内容
        List<CustomerInfoExcelRowInfo> rowInfoList = new LinkedList<>();
        CustomerExcelReadListener excelReadListener = new CustomerExcelReadListener(rowInfoList);
        EasyExcel.read(path, CustomerInfoExcelRowInfo.class, excelReadListener).sheet()
                .headRowNumber(CustomerInfoExcelRowInfo.HEADER_LINES).doRead();
        info.setValidateErrorMsgs(excelReadListener.getErrorMsgList())
                .setRowInfoList(rowInfoList)
                .setTotalCnt(excelReadListener.getCount());
        if (!info.hasError()) {
            customerImportRecordManager.saveImportFileProcInfo(info);
        }
        return info;
    }

    @Override
    @Async("tracedTaskExecutor")
    public void asyncImportCustInfo(ImportFileProcInfo info) {
        // 有错误信息时不处理
        if (info.hasError()) {
            return;
        }
        log.info("开始处理导入客户信息文件【{}】", info.getOriFileName());
        if (Objects.isNull(info.getId())) {
            ImportFileProcInfo existed =
                    customerImportRecordManager.findByOriFileName(info.getOriFileName(), info.getOptUserId());
            if (Objects.isNull(existed)) {
                customerImportRecordManager.saveImportFileProcInfo(info);
            } else {
                info.setId(existed.getId());
            }
        }
        Date applyDate = new Date();
        Long startTime = applyDate.getTime();
        List<CustomerInfoExcelRowInfo> rowInfoList = info.getRowInfoList();
        if (CollectionUtils.isEmpty(rowInfoList)) {
            info.setTotalCnt(0L)
                    .setCostMilSec(0L)
                    .setInsertedCnt(0L)
                    .setFinishTime(new Date())
                    .setProcStatusEnum(ImportProcStatusEnum.SUCC);
            customerImportRecordManager.updateById(info);
            log.info("结束处理导入客户信息文件【{}】， 文件无内容。", info.getOriFileName());
            return;
        }
        info.setProcStatusEnum(ImportProcStatusEnum.IMPORTING);
        customerImportRecordManager.updateById(info);
        List<List<CustomerInfoExcelRowInfo>> groups = Lists.partition(rowInfoList, 1000);
        List<ExistedCustInfoRowInfo> duplicated = new LinkedList<>();
        try {
            for (List<CustomerInfoExcelRowInfo> list : groups) {
                ImportCustResult result =
                        this.importCustInfoWithResult(info, list, applyDate, false, true);
                List<ExistedCustInfoRowInfo> duplicatedPart = result.getDuplicatedList();
                custNoticeService.notifyNewAssignment(result.getInsertedList(), result.getUpdatedList(), "IMPORT");
                if (!CollectionUtils.isEmpty(duplicatedPart)) {
                    duplicated.addAll(duplicatedPart);
                }
                // 用于更新处理进度
                customerImportRecordManager.updateById(info);
            }
            if (!CollectionUtils.isEmpty(duplicated)) {
                String realFileName = BizHelper.generateUniqueFileName(info.getOriFileName(), ".xlsx");
                String path = bizProperty.getDuplicatedCustFileDir() + File.separator + realFileName;
                EasyExcel.write(path, ExistedCustInfoRowInfo.class)
                        .sheet("已存在的客户信息")
                        .doWrite(duplicated);
                info.setExistedDataFileName(path);
                duplicated.clear();
            }
            // 写入 Excel
            info.setProcStatusEnum(ImportProcStatusEnum.SUCC);
        } catch (BizException e) {
            info.setErrorMsg(e.getMessage() + " " + e.getParamMsg());
        } catch (Exception e) {
            ExceptionLogPrinter.printExceptionLog(e, log);
            info.setErrorMsg("导入数据失败。 " + e.getMessage());
        } finally {
            if (!CollectionUtils.isEmpty(duplicated)) {

                String dupCsv = duplicated.stream()
                        .map(ExistedCustInfoRowInfo::toCsvString)
                        .collect(Collectors.joining("\n"));
                log.warn("库中已存在的客户信息：\n{}", dupCsv);
            }
            Date now = new Date();
            long costMilSec = now.getTime() - startTime;
            if (!Objects.equals(info.getProcStatusEnum(), ImportProcStatusEnum.SUCC)) {
                info.setProcStatusEnum(ImportProcStatusEnum.FAILED);
            } else {
                info.setFinishTime(now);
            }
            info.setCostMilSec(costMilSec);
            customerImportRecordManager.updateById(info);
            log.info("结束处理导入客户信息文件【{}】，处理结果: {} => {}/{}", info.getOriFileName(),
                    info.getProcStatusEnum(), info.getProcessedCnt(), info.getTotalCnt());
        }
    }

    @Override
    public void addCustInfo(List<PushCustInfoItem> itemList, Long targetDeptId, Long targetUserId) {
        if (CollectionUtils.isEmpty(itemList)) {
            return;
        }
        if (targetUserId != null && targetUserId > 0L) {
            var user = userService.getUserInfo(targetUserId);
            VerifyUtils.notNull(user, "targetUserId", ResultCode.USER_NOT_EXIST, true);
            if (targetDeptId != null && targetDeptId > 0L) {
                VerifyUtils.verifyTrue(Objects.equals(user.getDepartmentId(), targetDeptId),
                        "指定的用户与部门不匹配，请确认后再操作", true);
            }
            VerifyUtils.verifyTrue(Objects.equals(user.getOnlineStatus(), CommonConstant.YES),
                    "目标用户当前处于离线状态，不可分配客户数据", true);
        }

        List<CustomerInfoExcelRowInfo> rowList = itemList.stream()
                .map(e -> {
                    CustomerInfoExcelRowInfo info = customerConvert.convert2CustomerInfoExcelRowInfo(e);
                    info.ensureRemarkLengthSafe();
                    // 逐条校验数据
                    String errMsg = info.validateAndReturnErrMsg();
                    VerifyUtils.verifyTrue(StringUtils.isBlank(errMsg), errMsg, true);
                    return info;
                }).collect(Collectors.toList());
        Date applyDate = new Date();
        ImportFileProcInfo info = new ImportFileProcInfo()
                .setOptDeptId(0L)
                .setOptUserId(0L)
                .setTargetDeptId(targetDeptId)
                .setTargetUserId(targetUserId)
                .setRowInfoList(rowList);

        ImportCustResult result = this.importCustInfoWithResult(info, rowList, applyDate, true, true);
        custNoticeService.notifyNewAssignment(result.getInsertedList(), result.getUpdatedList(), "API");
    }

    @Override
    public PageInfo<ImportFileRecordDTO> pagedListImportRecords(PagedListImportRecordRequest request, Long optUserId,
            Long optUserDeptId, Set<UserAuthRoleEnum> roles) {
        request.autoFix();
        PageInfo<ImportFileRecordDTO> pageInfo = PageInfoUtils.blankPageInfo();
        pageInfo.setPageNum(request.getPage());
        pageInfo.setPageSize(request.getPageSize());
        pageInfo.setList(List.of());
        if (!RoleUtils.hasAny(roles, UserAuthRoleEnum.ROLE_SUPPER, UserAuthRoleEnum.ROLE_DEPT_DATA_ADMIN)) {
            return pageInfo;
        }
        CustImpRecQry qry = new CustImpRecQry()
                .setOrderByIdDesc(true);
        if (request.isSelfOnly()) {
            if (Objects.isNull(optUserId)) {
                return pageInfo;
            }
            qry.setOptUserId(optUserId);
        } else {
            // 部门数据管理员的情况
            if (!RoleUtils.isSupper(roles)) {
                List<Long> deptIds = deptService.getAllChildrenIdByParentDeptId(optUserDeptId, true);
                if (CollectionUtils.isEmpty(deptIds)) {
                    return pageInfo;
                }
                qry.setOptDeptIds(deptIds);
            }
        }
        PageInfo<CustomerImportRecordDO> oriPageInfo =
                SuSqlUtils.pagedListQuery(request.getPage(), request.getPageSize(),
                        () -> customerImportRecordManager.list(qry.toQueryWrapper()));
        if (PageInfoUtils.isEmpty(oriPageInfo)) {
            return pageInfo;
        }
        // 转换数据
        List<CustomerImportRecordDO> innerList = oriPageInfo.getList();
        Set<Long> userIds = Sets.newHashSet();
        Set<Long> deptIds = Sets.newHashSet();
        List<ImportFileRecordDTO> list = innerList.stream()
                .peek(e -> {
                    OtherUtils.processIfNotNull(e.getOptUserId(), () -> userIds.add(e.getOptUserId()));
                    OtherUtils.processIfNotNull(e.getTargetUserId(), () -> userIds.add(e.getTargetUserId()));
                    OtherUtils.processIfNotNull(e.getOptDeptId(), () -> deptIds.add(e.getOptDeptId()));
                    OtherUtils.processIfNotNull(e.getTargetDeptId(), () -> deptIds.add(e.getTargetDeptId()));
                }).map(e -> {
                    ImportFileRecordDTO dto = customerConvert.convert2ImportFileRecordDTO(e);
                    dto.setDownloadDupRecFlag(StringUtils.isNotBlank(e.getExistedDataFileName())
                            ? CommonConstant.YES
                            : CommonConstant.NO);
                    return dto;
                }).collect(Collectors.toList());
        Map<Long, String> deptIdNameMap = deptService.getDeptNameByIds(deptIds);
        Map<Long, String> userIdNameMap = userService.getUserRealNames(userIds);
        list.parallelStream().forEach(e -> {
            e.setOptUserName(userIdNameMap.getOrDefault(e.getOptUserId(), StringUtils.EMPTY))
                    .setTargetUserName(userIdNameMap.getOrDefault(e.getTargetUserId(), StringUtils.EMPTY))
                    .setOptDeptName(deptIdNameMap.getOrDefault(e.getOptDeptId(), StringUtils.EMPTY))
                    .setTargetDeptName(deptIdNameMap.getOrDefault(e.getTargetDeptId(), StringUtils.EMPTY));
            if (Objects.equals(e.getTargetDeptId(), 0L) && Objects.equals(e.getTargetUserId(), 0L)) {
                e.setTargetDeptName("企业公海");
            } else if (Objects.equals(e.getTargetUserId(), 0L)) {
                e.setTargetUserName("部门公海");
            }
            OtherUtils.processIf(Objects.equals(e.getOptDeptId(), 0L), () -> e.setOptDeptName(StringUtils.EMPTY));
        });
        pageInfo = PageInfoUtils.copyPageInfoWithoutListFrom(oriPageInfo);
        pageInfo.setList(list);
        return pageInfo;
    }

    @Override
    public CustomerImportRecordDO getById(Long id) {
        if (Objects.isNull(id)) {
            return null;
        }
        return customerImportRecordManager.getById(id);
    }

    @Override
    public ResponseEntity<byte[]> downloadExistedCustFile(Long custImportRecId, Long optUserId, Long optUserDeptId,
            Set<UserAuthRoleEnum> roles) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAcceptCharset(List.of(StandardCharsets.UTF_8));
        headers.setContentType(MediaType.TEXT_HTML);
        if (!RoleUtils.hasAny(roles, UserAuthRoleEnum.ROLE_SUPPER, UserAuthRoleEnum.ROLE_DEPT_DATA_ADMIN)) {
            // 无权限直接返回
            return ResponseEntity.ok().headers(headers)
                    .body(ResultCode.DOWNLOAD_FAILED_4_FILE_NO_AUTH.msg.getBytes(StandardCharsets.UTF_8));
        }

        CustomerImportRecordDO rec = customerImportRecordManager.getById(custImportRecId);
        if (Objects.isNull(rec) || com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(
                rec.getExistedDataFileName())) {
            return ResponseEntity.ok().headers(headers)
                    .body(ResultCode.DOWNLOAD_FAILED_4_FILE_NOT_FOUND.msg.getBytes(StandardCharsets.UTF_8));
        }
        File file = new File(rec.getExistedDataFileName());
        if (!file.exists() || !file.isFile()) {
            return ResponseEntity.ok().headers(headers)
                    .body(ResultCode.DOWNLOAD_FAILED_4_FILE_NOT_FOUND.msg.getBytes(StandardCharsets.UTF_8));
        }
        if (!RoleUtils.isSupper(roles)) {
            //判断是否有权限下载
            boolean hasAuth = Objects.equals(optUserId, rec.getOptUserId());
            hasAuth |= deptService.doesBelongParentDeptId(optUserDeptId, rec.getOptDeptId(), true);
            if (!hasAuth) {
                return ResponseEntity.ok().headers(headers)
                        .body(ResultCode.DOWNLOAD_FAILED_4_FILE_NO_AUTH.msg.getBytes(StandardCharsets.UTF_8));
            }
        }
        //读取文件内容并返回
        byte[] content = null;
        try {
            content = FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            log.error("读取文件失败: id: {} , existedDataFileName: {}", custImportRecId, rec.getExistedDataFileName());
            ExceptionLogPrinter.printExceptionLog(e, log);
            return ResponseEntity.ok().headers(headers)
                    .body(ResultCode.DOWNLOAD_FAILED.msg.getBytes(StandardCharsets.UTF_8));
        }
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", URLEncoder.encode(file.getName(), StandardCharsets.UTF_8));
        headers.setContentLength(content.length);
        return ResponseEntity.ok().headers(headers).body(content);
    }

    private ImportCustResult importCustInfoWithResult(ImportFileProcInfo info, List<CustomerInfoExcelRowInfo> list,
            Date applyDate, boolean enableAutoDispatch, boolean collectNotice) {
        if (CollectionUtils.isEmpty(list)) {
            return ImportCustResult.empty();
        }
        boolean autoActive = enableAutoDispatch && custDispatchService.isAutoModeActive();
        Map<Long, Integer> manualAssignCounts = new HashMap<>();
        Long baseDeptId = info.getTargetDeptId();
        if (baseDeptId == null) {
            baseDeptId = 0L;
        }
        final Long finalBaseDeptId = baseDeptId;
        Set<String> channelNames = Sets.newHashSet();
        //查询已存在的客户信息
        Set<String> mobiles = list.stream()
                .peek(e -> {
                    if (StringUtils.isNotBlank(e.getChannelName())) {
                        channelNames.add(e.getChannelName());
                    }
                })
                .map(CustomerInfoExcelRowInfo::getMobile)
                .collect(Collectors.toSet());
        Map<String, Integer> channelNameValueMap =
                this.addChannelAndFetchChannelNameMap(channelNames, info.getOptUserId());

        CustomerInfoQry qry = new CustomerInfoQry()
                .setMobiles(mobiles);
        List<CustomerInfoItemDO> existedCustList = customerInfoItemManager.list(qry.toQueryWrapper());
        List<CustomerInfoItemDO> toInsertList = new ArrayList<>(list.size());
        Map<String, CustomerInfoItemDO> map = CollectionUtils.isEmpty(existedCustList)
                ? Map.of()
                : existedCustList.stream().collect(Collectors.toMap(e -> e.getName() + e.getMobile(), e -> e));
        List<ExistedCustInfoRowInfo> duplicatedList = new ArrayList<>(existedCustList.size());
        Iterator<CustomerInfoExcelRowInfo> itr = list.iterator();

        while (itr.hasNext()) {
            CustomerInfoExcelRowInfo rowInfo = itr.next();
            itr.remove();
            CustomerInfoItemDO itemDO = map.get(rowInfo.getName() + rowInfo.getMobile());
            if (Objects.isNull(itemDO)) {
                Long targetDeptId = info.getTargetDeptId();
                Long targetUserId = info.getTargetUserId();
                if (targetDeptId == null) {
                    targetDeptId = 0L;
                }
                if (targetUserId == null) {
                    targetUserId = 0L;
                }
                if (autoActive) {
                    Optional<CustDispatchService.DispatchUserResult> autoResult =
                            custDispatchService.pickAutoAssignee(targetDeptId);
                    if (autoResult.isPresent()) {
                        targetUserId = autoResult.get().getUserId();
                        targetDeptId = autoResult.get().getDeptId();
                    } else {
                        targetUserId = 0L;
                    }
                } else if (targetUserId != null && targetUserId > 0L) {
                    manualAssignCounts.merge(targetUserId, 1, Integer::sum);
                }
                itemDO = makeupCustomerInfoItemDOFrom(rowInfo, targetDeptId, targetUserId, channelNameValueMap);
                itemDO.setApplyDate(applyDate)
                        .setSourceFileName(info.getOriFileName());
                if (targetUserId != null && targetUserId > 0L) {
                    itemDO.setFollowTime(applyDate);
                }
                toInsertList.add(itemDO);
            } else {
                duplicatedList.add(ExistedCustInfoRowInfo.of(itemDO.getId(), rowInfo.getLineNo(), rowInfo.getName(),
                        rowInfo.getMobile(), rowInfo.getChannelName()));
            }
        }
        //新增数据
        if (!CollectionUtils.isEmpty(toInsertList)) {
            try {
                customerInfoItemManager.saveBatch(toInsertList);
            } catch (DuplicateKeyException e) {
                throw new BizException(ResultCode.IMPORT_CUST_FAILED,
                        "已存在相同的客户信息，可能其他人正在执行导入操作，请稍后重新导入。");
            }
            info.addInsertedCnt(toInsertList.size());
            info.addProcessedCnt(toInsertList.size());
        }
        if (!autoActive && !manualAssignCounts.isEmpty()) {
            manualAssignCounts.forEach((userId, count) ->
                    custDispatchService.recordManualAssignment(userId, finalBaseDeptId, count));
        }
        // 更新数据
        if (!CollectionUtils.isEmpty(duplicatedList)) {
            List<CustomerInfoItemDO> updateList = duplicatedList.stream()
                    .map(e -> new CustomerInfoItemDO()
                            .setId(e.getCustId())
                            .setSourceFileName(info.getOriFileName())
                            .setChannel(channelNameValueMap.get(e.getChannelName()))
                            .setApplyDate(applyDate))
                    .collect(Collectors.toList());
            customerInfoItemManager.updateBatchById(updateList);
            info.addProcessedCnt(duplicatedList.size());
        }

        ImportCustResult result = new ImportCustResult()
                .setDuplicatedList(duplicatedList);
        if (collectNotice) {
            result.setInsertedList(new ArrayList<>(toInsertList));
            if (!CollectionUtils.isEmpty(duplicatedList)) {
                Set<Long> dupIds = duplicatedList.stream()
                        .map(ExistedCustInfoRowInfo::getCustId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                if (!dupIds.isEmpty()) {
                    result.setUpdatedList(customerInfoItemManager.listByIds(dupIds));
                }
            }
        }
        return result;
    }

    private static class ImportCustResult {
        private List<CustomerInfoItemDO> insertedList = List.of();
        private List<CustomerInfoItemDO> updatedList = List.of();
        private List<ExistedCustInfoRowInfo> duplicatedList = List.of();

        static ImportCustResult empty() {
            return new ImportCustResult();
        }

        public List<CustomerInfoItemDO> getInsertedList() {
            return insertedList;
        }

        public ImportCustResult setInsertedList(List<CustomerInfoItemDO> insertedList) {
            this.insertedList = insertedList == null ? List.of() : insertedList;
            return this;
        }

        public List<CustomerInfoItemDO> getUpdatedList() {
            return updatedList;
        }

        public ImportCustResult setUpdatedList(List<CustomerInfoItemDO> updatedList) {
            this.updatedList = updatedList == null ? List.of() : updatedList;
            return this;
        }

        public List<ExistedCustInfoRowInfo> getDuplicatedList() {
            return duplicatedList;
        }

        public ImportCustResult setDuplicatedList(List<ExistedCustInfoRowInfo> duplicatedList) {
            this.duplicatedList = duplicatedList == null ? List.of() : duplicatedList;
            return this;
        }
    }

    private CustomerInfoItemDO makeupCustomerInfoItemDOFrom(CustomerInfoExcelRowInfo rowInfo, Long targetDeptId,
            Long targetUserId, Map<String, Integer> channelNameValueMap) {
        CustomerInfoItemDO item = new CustomerInfoItemDO()
                .setName(rowInfo.getName())
                .setMobile(rowInfo.getMobile())
                .setIdCardNo(rowInfo.getIdCardNo())
                .setHouseFlag(rowInfo.getHouseFlagEnum().flag)
                .setHouseVal(rowInfo.getHouseVal())
                .setChannel(StringUtils.isBlank(rowInfo.getChannelName())
                        ? null
                        : channelNameValueMap.get(rowInfo.getChannelName()))
                .setCustomerRemark(rowInfo.getCustomerRemark())
                .setOwnerDeptId(targetDeptId)
                .setOwnerUserId(targetUserId)
                .setReqLoanAmount(rowInfo.getReqLoanAmount())
                .setSex(rowInfo.getSex())
                .setAge(rowInfo.getAge())
                .setProvidentFlag(rowInfo.getProvidentFlagEnum() == null ? null : rowInfo.getProvidentFlagEnum().flag)
                .setSocialInsuranceFlag(rowInfo.getSocialInsuranceFlagEnum() == null ? null
                        : rowInfo.getSocialInsuranceFlagEnum().flag)
                .setInsuranceFlag(rowInfo.getInsuranceFlagEnum() == null ? null : rowInfo.getInsuranceFlagEnum().flag)
                .setCarFlag(rowInfo.getCarFlagEnum() == null ? null : rowInfo.getCarFlagEnum().flag)
                .setCarPurchaseType(rowInfo.getCarPurchaseTypeEnum() == null ? null
                        : rowInfo.getCarPurchaseTypeEnum().purchaseType)
                .setCarNo(rowInfo.getCarNo())
                .setCityId(rowInfo.getCityId())
                .setCityName(rowInfo.getCityName())
                ;
        MobileAreaInfo areaInfo = mobileAreaService.getMobileArea(rowInfo.getMobile());
        if (areaInfo != null && StringUtils.isNotBlank(areaInfo.toDisplay())) {
            item.setMobileArea(areaInfo.toDisplay());
        }
        OtherUtils.processIfNotNull(rowInfo.getCarFlagEnum(),e->item.setCarFlag(e.flag));
        OtherUtils.processIfNotNull(rowInfo.getCarPurchaseTypeEnum(),e->item.setCarPurchaseType(e.purchaseType));


        return item;
    }

    /**
     * 增加 channelNames 中的渠道业务字典信息， 并取回所有的渠道业务字典信息。 若channelNames中的渠道已存在，则不追加
     *
     * @param channelNames
     * @param optUserId
     * @return
     */
    private Map<String, Integer> addChannelAndFetchChannelNameMap(Set<String> channelNames, Long optUserId) {
        if (CollectionUtils.isEmpty(channelNames)) {
            return Map.of();
        }

        try {
            bizConfigService.generateChannelDictByNames(channelNames, optUserId);
        } catch (BizException e) {
            ExceptionLogPrinter.printExceptionLog(e, log);
            if (Objects.equals(e.getCode(), ResultCode.ADD_FAILED_FOR_DUPLICATED_BIZ_TYPE_VALUE.code)) {
                throw new BizException(ResultCode.IMPORT_CUST_FAILED,
                        "创建新渠道信息失败，可能其他人员正执行导入操作，请稍后重新导入");
            }
            throw e;
        }
        List<BizDictItem> channelList = bizConfigService.getBizDictItems(BizDictConfigTypeEnum.DATA_CHANNEL, true);
        return channelList.stream()
                .collect(Collectors.toMap(e -> e.getLabel(), e -> e.getIntValue(), (v1, v2) -> v2));
    }

    /**
     * 检查是否使用同名文件重复导入
     *
     * @param cmd
     */
    private void checkDuplicateImport(ImportCustCmd cmd) {
        ImportFileProcInfo info =
                customerImportRecordManager.findByOriFileName(cmd.getOriFileName(), null, ImportProcStatusEnum.INIT,
                        ImportProcStatusEnum.IMPORTING, ImportProcStatusEnum.SUCC);
        if (Objects.nonNull(info)) {
            throw new BizException(ResultCode.IMPORT_CUST_FAILED_4_DUPLICATED_FILE_NAME, GsonHelper.toJson(cmd));
        }
    }

    private void validate4Import(ImportCustCmd cmd) {
        cmd.validate();
        // 判断操作权限
        VerifyUtils.judge(!RoleUtils.nonBizDataAdmin(cmd.getRoles()), ResultCode.IMPORT_CUST_FAILED_4_NOT_AUTH, true,
                "optUserId: " + cmd.getOptUserId());
        if (RoleUtils.isSupper(cmd.getRoles())) {
            return;
        }
        String cmdJson = GsonHelper.toJson(cmd);
        // 分配到非本部门公海，校验部门层级信息
        if (!Objects.equals(cmd.getOptDeptId(), cmd.getTargetDeptId())) {
            boolean childDept = deptService.doesBelongParentDeptId(cmd.getOptDeptId(), cmd.getTargetDeptId(), true);
            if (!childDept) {
                throw new BizException(ResultCode.IMPORT_CUST_FAILED_4_BEYOND_DEPT, cmdJson);
            }
        }
        // 判断目标用户是否归属于目标部门
        if (!Objects.equals(cmd.getTargetUserId(), 0L)) {
            var user = userService.getUserInfo(cmd.getTargetUserId());
            if (!Objects.equals(user.getDepartmentId(), cmd.getTargetDeptId())) {
                throw new BizException(ResultCode.IMPORT_CUST_FAILED_4_TARGET_USER_DEPT_MISMATCH, cmdJson);
            }
            if (!Objects.equals(user.getOnlineStatus(), CommonConstant.YES)) {
                throw new BizException(ResultCode.IMPORT_CUST_FAILED, "目标用户当前处于离线状态，不可分配客户数据");
            }
        }
    }

}
