package com.jhl.silver.union.biz.customer.service;

import com.github.pagehelper.PageInfo;
import com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerImportRecordDO;
import com.jhl.silver.union.biz.data.ImportCustCmd;
import com.jhl.silver.union.biz.data.ImportFileProcInfo;
import com.jhl.silver.union.web.data.ImportFileRecordDTO;
import com.jhl.silver.union.web.data.PagedListImportRecordRequest;
import com.jhl.silver.union.web.data.admin.PushCustInfoItem;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * @author: qingren
 * @create_time: 2025/4/28
 */
public interface ImportCustDataService {

    /**
     * 将文件写入本地磁盘
     *
     * @param file
     * @return 返回本地绝对路径
     */
    String saveFile(MultipartFile file);

    /**
     * 保存并校验上传的文件信息
     *
     * @param cmd
     * @return
     */
    ImportFileProcInfo saveAndValidateFile(ImportCustCmd cmd);

    /**
     * 导入客户信息。 有损操作，执行后， info中的客户信息数据会被清空
     *
     * @param info
     */
    void asyncImportCustInfo(ImportFileProcInfo info);

    /**
     * 增加客户信息，并分配给指定的部门以及人员
     *
     * @param itemList     客户信息列表
     * @param targetDeptId 分配的目标部门 ID
     * @param targetUserId 分配的用户 ID
     */
    void addCustInfo(List<PushCustInfoItem> itemList, Long targetDeptId, Long targetUserId);

    void addApiPushedCustInfo(List<PushCustInfoItem> itemList, Long targetDeptId, Long targetUserId);

    /**
     * 分页获取导入记录
     *
     * @param request
     * @param optUserId
     * @param optUserDeptId
     * @param roles
     * @return
     */
    PageInfo<ImportFileRecordDTO> pagedListImportRecords(PagedListImportRecordRequest request, Long optUserId,
            Long optUserDeptId, Set<UserAuthRoleEnum> roles);

    /**
     * 根据ID取导入记录信息
     *
     * @param id
     * @return
     */
    CustomerImportRecordDO getById(Long id);

    /**
     * 下载导入记录中的重复客户信息的文件
     *
     * @param custImportRecId
     * @return
     */
    ResponseEntity<byte[]> downloadExistedCustFile(Long custImportRecId, Long optUserId, Long optUserDeptId,
            Set<UserAuthRoleEnum> roles);
}
