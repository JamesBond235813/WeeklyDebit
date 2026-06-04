package com.jhl.silver.union.biz.data;

import com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum;
import com.jhl.silver.union.commons.utils.VerifyUtils;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.Set;

/**
 * @author: qingren
 * @create_time: 2025/5/7
 */
@Data
@Accessors(chain = true)
public class ImportCustCmd {
    /**
     * 上传的文件
     */

    private transient MultipartFile file;
    /**
     * 原始文件名
     */
    private String oriFileName;
    /**
     * 分配数据的目标部门ID
     */

    private Long targetDeptId;

    /**
     * 分配数据的目标用户ID
     */

    private Long targetUserId;

    /**
     * 操作人用户ID
     */

    private Long optUserId;

    /**
     * 操作人部门ID
     */
    private Long optDeptId;

    /**
     * 操作人具有的权限
     */
    private Set<UserAuthRoleEnum> roles;

    public void validate() {
        VerifyUtils.notNull(optUserId, "optUserId", "请指定操作人", true);
        VerifyUtils.notNull(optDeptId, "optDeptId", "请指定操作人部门", true);
        VerifyUtils.notNull(file, "file", "未上传文件", true);
        VerifyUtils.verifyTrue(!file.isEmpty(), "上传文件为空，拒绝操作", true);
        this.oriFileName = file.getOriginalFilename();
        VerifyUtils.notBlank(oriFileName, "oriFileName", "未识别文件名，请确认后再操作", true);
        VerifyUtils.verifyTrue(oriFileName.length() <= 256, "文件名不可超过256个字符，请确认后再操作", true);
        if (Objects.isNull(targetDeptId)) {
            targetDeptId = optDeptId;
        }
        if (Objects.isNull(targetUserId)) {
            targetUserId = 0L;
        }
    }

    /**
     * 生成导入记录信息
     *
     * @param fileLocalPath 本地文件的绝对路径
     * @return
     */
    public ImportFileProcInfo makeupImportFileProcInfo(String fileLocalPath) {
        ImportFileProcInfo info = ImportFileProcInfo.of(this.oriFileName,fileLocalPath)
                .setTargetDeptId(this.targetDeptId)
                .setTargetUserId(this.targetUserId)
                .setOptUserId(this.optUserId)
                .setOptDeptId(this.optDeptId);
        return info;
    }

}
