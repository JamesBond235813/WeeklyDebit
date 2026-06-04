package com.jhl.silver.union.biz.dept.service.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jhl.silver.union.biz.common.BizConstance;
import com.jhl.silver.union.biz.common.ResultCode;
import com.jhl.silver.union.biz.common.enums.CommonStatusEnum;
import com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum;
import com.jhl.silver.union.biz.common.utils.BizHelper;
import com.jhl.silver.union.biz.common.utils.RoleUtils;
import com.jhl.silver.union.biz.data.DeptInfo;
import com.jhl.silver.union.biz.data.DeptNodeInfo;
import com.jhl.silver.union.biz.data.DeptQry;
import com.jhl.silver.union.biz.data.UserQry;
import com.jhl.silver.union.biz.data.convert.DeptConvert;
import com.jhl.silver.union.biz.dept.dal.entity.SuOrgDepartmentInfoDO;
import com.jhl.silver.union.biz.dept.manager.SuOrgDepartmentInfoManager;
import com.jhl.silver.union.biz.dept.service.DeptService;
import com.jhl.silver.union.biz.user.dal.entity.SuUserInfoDO;
import com.jhl.silver.union.biz.user.manager.SuUserInfoManager;
import com.jhl.silver.union.commons.CommonConstant;
import com.jhl.silver.union.commons.exception.BizException;
import com.jhl.silver.union.commons.gson.GsonHelper;
import com.jhl.silver.union.commons.utils.SuDateUtils;
import com.jhl.silver.union.commons.utils.VerifyUtils;
import com.jhl.silver.union.web.data.DeptInfoDTO;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author: qingren
 * @create_time: 2025/3/21
 */
@Service
@Slf4j
public class DeptServiceImpl implements DeptService {
    private static Long ROOT_DEPT_ID = 0L;
    @Resource
    private SuOrgDepartmentInfoManager deptManager;
    @Resource
    private SuUserInfoManager userInfoManager;

    @Resource
    private DeptConvert convert;
    /**
     * 部门树节点缓存, 本缓存仅接受 ROOT_DEPT_ID作为 KEY， 其余均返回空 map
     */
    private LoadingCache<Long, Map<Long, DeptNodeInfo>> deptTreeCache;

    private volatile boolean inited = false;

    @PostConstruct
    public synchronized void init() {
        if (inited) {
            return;
        }
        deptTreeCache = CacheBuilder.newBuilder()
                .expireAfterWrite(20, TimeUnit.MINUTES)
                .softValues()
                .maximumSize(10)
                .initialCapacity(1)
                .build(new CacheLoader<>() {
                    @Override
                    public Map<Long, DeptNodeInfo> load(Long key) throws Exception {
                        if (!Objects.equals(key, ROOT_DEPT_ID)) {
                            return Map.of();
                        }
                        return loadDeptTreeNodeFromDb();
                    }
                });
        inited = true;
    }

    @Override
    public void addDeptInfo(DeptInfo deptInfo) {
        deptInfo.validate();
        VerifyUtils.notNull(deptInfo.getCreateBy(), "deptInfo.getCreateBy()", "请指定创建人ID", true);
        Long parentId = deptInfo.getParentDeptId();
        if (Objects.nonNull(parentId) && parentId != ROOT_DEPT_ID) {
            SuOrgDepartmentInfoDO parentInfo = deptManager.getValidDeptById(parentId);
            VerifyUtils.notNull(parentInfo, "parentId", "上级部门不存在或处于禁用状态，请确认后再操作", true);
        }
        SuOrgDepartmentInfoDO departmentInfoDO = convert.convert2SuOrgDepartmentInfoDO(deptInfo);
        try {
            deptInfo.setId(null);
            deptManager.save(departmentInfoDO);
        } catch (Exception e) {
            if (e instanceof DuplicateKeyException) {
                throw BizException.makeupBy(ResultCode.SAVE_DEPT_FAILED,
                        Lists.newArrayList("部门名称[ " + deptInfo.getDeptName() + " ]已存在"), deptInfo.getDeptName());
            }
        }
        this.clearCache();
    }

    @Override
    public void updateDeptInfo(DeptInfo deptInfo) {
        VerifyUtils.notNull(deptInfo.getId(), "deptInfo.getId()", "请指定需要修改的部门信息", true);
        VerifyUtils.verifyTrue(!Objects.equals(deptInfo.getId(), deptInfo.getParentDeptId()),
                "上级部门指定不正确，请确认后再操作", true);
        VerifyUtils.notNull(deptInfo.getLastModifiedBy(), "deptInfo.getLastModifiedBy()",
                "请指定修改部门信息的操作人 ID", true);
        // 检查上级部门
        Long parentId = deptInfo.getParentDeptId();
        if (Objects.nonNull(parentId) && parentId != ROOT_DEPT_ID) {
            SuOrgDepartmentInfoDO parentInfo = deptManager.getValidDeptById(parentId);
            VerifyUtils.notNull(parentInfo, "parentId", "上级部门不存在或处于禁用状态，请确认后再操作", true);
        }
        if (Objects.isNull(deptInfo.getParentDeptId())) {
            // 不指定上级视为无上级
            deptInfo.setParentDeptId(ROOT_DEPT_ID);
        }
        deptInfo.setCreateBy(null);
        SuOrgDepartmentInfoDO departmentInfoDO = convert.convert2SuOrgDepartmentInfoDO(deptInfo);
        log.warn("用户[ {} ]更新部门信息数据: {}", deptInfo.getLastModifiedBy(),
                GsonHelper.toJson(departmentInfoDO));
        deptManager.updateById(departmentInfoDO);
        this.clearCache();
    }

    @Override
    public void deleteDeptInfo(Long id, Long optUserId) {
        VerifyUtils.notNull(optUserId, "optUserId",
                "请指定删除部门信息的操作人 ID", true);
        if (Objects.isNull(id)) {
            return;
        }
        SuOrgDepartmentInfoDO infoDO = deptManager.getValidDeptById(id);
        if (Objects.isNull(infoDO)) {
            return;
        }

        // 查询子节点信息,存在子节点的， 不可删除
        DeptQry qry = new DeptQry()
                .setParentDeptIds(List.of(id));
        List<SuOrgDepartmentInfoDO> children = deptManager.list(qry.toQueryWrapper(true));
        if (!CollectionUtils.isEmpty(children)) {
            throw BizException.makeupBy(ResultCode.NOT_ALLOWED_DELETE_FOR_EXISTING_CHILDREN,
                    Lists.newArrayList(infoDO.getDeptName()));
        }

        SuOrgDepartmentInfoDO departmentInfoDO = new SuOrgDepartmentInfoDO()
                .setId(id)
                .setLastModifiedBy(optUserId)
                .setDeleteFlag(System.currentTimeMillis());
        deptManager.updateById(departmentInfoDO);
        this.clearCache();
    }

    @Override
    public DeptInfo getDeptInfoById(Long id, boolean validOnly) {
        Map<Long, DeptNodeInfo> map = deptTreeCache.getUnchecked(ROOT_DEPT_ID);
        DeptNodeInfo deptNodeInfo = map.get(id);
        if (Objects.isNull(deptNodeInfo)) {
            return null;
        }
        if (validOnly && !Objects.equals(deptNodeInfo.getStatus(), CommonStatusEnum.OK.status)) {
            return null;
        }
        DeptInfo deptInfo = new DeptInfo();
        BeanUtils.copyProperties(deptNodeInfo, deptInfo);
        return deptInfo;
    }

    @Override
    public DeptNodeInfo getDeptTree() {
        Map<Long, DeptNodeInfo> map = deptTreeCache.getUnchecked(ROOT_DEPT_ID);
        return map.get(ROOT_DEPT_ID);
    }

    @Override
    public List<Long> getAllChildrenIdByParentDeptId(Long parentDeptId, boolean includeSelf) {
        if (Objects.isNull(parentDeptId)) {
            return List.of();
        }
        Map<Long, DeptNodeInfo> map = deptTreeCache.getUnchecked(ROOT_DEPT_ID);
        DeptNodeInfo parent = map.get(parentDeptId);
        List<DeptNodeInfo> list = this.getChildren(parent);
        if (CollectionUtils.isEmpty(list)) {
            if (includeSelf) {
                return List.of(parentDeptId);
            }
            return List.of();
        }

        // 去重
        List<Long> deptIds = list.stream()
                .filter(Objects::nonNull)
                .map(DeptNodeInfo::getId)
                .distinct()
                .collect(Collectors.toList());
        if (includeSelf) {
            deptIds.add(parentDeptId);
        }
        return deptIds;

    }

    @Override
    public List<Long> getAllChildrenIdByParentDeptId(Long parentDeptId, Set<UserAuthRoleEnum> roleSet) {
        if (CollectionUtils.isEmpty(roleSet)) {
            return List.of(parentDeptId);
        }
        if (RoleUtils.hasAny(roleSet, UserAuthRoleEnum.ROLE_SUPPER, UserAuthRoleEnum.ROLE_DEPT_DATA_ADMIN)) {
            return this.getAllChildrenIdByParentDeptId(parentDeptId, true);
        }
        return List.of(parentDeptId);
    }

    @Override
    public boolean doesBelongParentDeptId(Long parentDeptId, Long childDeptId, boolean allowSameDeptBelonging) {
        if (Objects.isNull(parentDeptId)
                || Objects.isNull(childDeptId)
                || parentDeptId < 1L
                || childDeptId < 1L) {
            return false;
        }
        if (Objects.equals(parentDeptId, childDeptId)) {
            return allowSameDeptBelonging;
        }

        Set<UserAuthRoleEnum> roleSet = Set.of(UserAuthRoleEnum.ROLE_DEPT_DATA_ADMIN);
        List<Long> children = this.getAllChildrenIdByParentDeptId(parentDeptId, roleSet);
        return children.contains(childDeptId);
    }

    private List<DeptNodeInfo> getChildren(DeptNodeInfo parent) {
        if (Objects.isNull(parent)) {
            return List.of();
        }
        List<DeptNodeInfo> children = parent.getChildren();
        if (CollectionUtils.isEmpty(children)) {
            return List.of();
        }
        List<DeptNodeInfo> list = Lists.newArrayList();
        list.addAll(children);
        List<DeptNodeInfo> innerList;
        for (DeptNodeInfo child : children) {
            innerList = getChildren(child);
            if (!CollectionUtils.isEmpty(innerList)) {
                list.addAll(innerList);
            }
        }
        return list;
    }

    public Map<Long, DeptNodeInfo> loadDeptTreeNodeFromDb() {
        List<SuOrgDepartmentInfoDO> list = deptManager.list(DeptQry.allValidDeptQuery());
        if (CollectionUtils.isEmpty(list)) {
            return Map.of();
        }
        Map<Long/* deptId */, DeptNodeInfo> nodeMap = list.stream()
                .collect(Collectors.toMap(SuOrgDepartmentInfoDO::getId,
                        e -> convert.convert2DeptNodeInfo(e),
                        (k1, k2) -> k2));
        DeptNodeInfo root = new DeptNodeInfo();
        root.setId(ROOT_DEPT_ID)
                .setParentDeptId(BizConstance.SPEC_ROOT_PARENT_ID)
                .setDeptName("全部部门")
                .setDeptCode(StringUtils.EMPTY)
                .setIntroduction(StringUtils.EMPTY)
                .setStatus(CommonStatusEnum.OK.status);
        nodeMap.put(ROOT_DEPT_ID, root);
        for (DeptNodeInfo info : nodeMap.values()) {
            if (Objects.isNull(info.getParentDeptId()) || info.getParentDeptId() < 0L) {
                continue;
            }
            DeptNodeInfo parent = nodeMap.get(info.getParentDeptId());
            if (Objects.isNull(parent)) {
                continue;
            }
            // 增加子节点
            if (Objects.isNull(parent.getChildren())) {
                parent.setChildren(new ArrayList<>());
            }
            parent.getChildren().add(info);
            // 为当前节点设置父节点
            info.setParentDeptNodeInfo(parent);
        }
        nodeMap.put(ROOT_DEPT_ID, root);
        return nodeMap;
    }

    @Override
    public List<DeptInfo> queryDeptListByName(String deptName) {
        List<DeptInfo> list = deptTreeCache.getUnchecked(ROOT_DEPT_ID)
                .values()
                .stream()
                .filter(e -> StringUtils.contains(e.getDeptName(), deptName))
                .map(e -> {
                    DeptInfo deptInfo = new DeptInfo();
                    BeanUtils.copyProperties(e, deptInfo);
                    return deptInfo;
                })
                .collect(Collectors.toList());
        return list;
    }

    @Override
    public List<DeptInfoDTO> listByDeptInfo(DeptQry deptQry, boolean needExtendQry) {
        List<Long> parentIdList = List.of();
        if (StringUtils.isNotBlank(deptQry.getParentDeptNamePrefix())) {
            DeptQry parentQry = new DeptQry()
                    .setDeptNamePrefix(deptQry.getParentDeptNamePrefix())
                    .setDeleteFlag(BizConstance.NOT_DELETED);
            parentIdList = deptManager.list(parentQry.toQueryWrapper(false))
                    .stream()
                    .map(SuOrgDepartmentInfoDO::getId)
                    .collect(Collectors.toUnmodifiableList());
        }
        deptQry.setParentDeptIds(parentIdList);
        List<SuOrgDepartmentInfoDO> infoDOList = deptManager.list(deptQry.toQueryWrapper(false));
        if (CollectionUtils.isEmpty(infoDOList)) {
            return List.of();
        }
        Set<Long> userIds = Sets.newHashSet();
        Set<Long> parentIds = Sets.newHashSet();
        for (SuOrgDepartmentInfoDO infoDO : infoDOList) {
            userIds.add(infoDO.getCreateBy());
            if (infoDO.getLastModifiedBy() > 0L) {
                userIds.add(infoDO.getLastModifiedBy());
            }
            parentIds.add(infoDO.getParentDeptId());
        }
        UserQry uq = new UserQry()
                .setIds(userIds);

        List<SuUserInfoDO> userList = needExtendQry ? userInfoManager.list(uq.toQryWrapper(false)) : List.of();
        Map<Long/* userID */, String/* realName+username */> userNameMap = BizHelper.makeupUserNameMap(userList);
        DeptQry parentDeptQry = new DeptQry()
                .setIds(parentIds)
                .setDeleteFlag(BizConstance.NOT_DELETED);
        List<SuOrgDepartmentInfoDO> parentDeptList = needExtendQry
                ? deptManager.list(parentDeptQry.toQueryWrapper(false))
                : List.of();
        Map<Long, String> deptIdNameMap = BizHelper.makeupDeptIdName(parentDeptList);

        return infoDOList.stream()
                .map(e -> {
                    DeptInfoDTO info = convert.convert2DeptInfoDTO(e)
                            .setCreateByUserName(userNameMap.getOrDefault(e.getCreateBy(), CommonConstant.HYPHEN))
                            .setLastModifiedByUserName(
                                    userNameMap.getOrDefault(e.getLastModifiedBy(), CommonConstant.HYPHEN))
                            .setParentDeptName(deptIdNameMap.getOrDefault(e.getParentDeptId(), CommonConstant.HYPHEN))
                            .setGmtCreateStr(SuDateUtils.format(e.getGmtCreate(), SuDateUtils.DF_YYYY_MM_DDHHMMSS))
                            .setGmtModifiedStr(SuDateUtils.format(e.getGmtModified(), SuDateUtils.DF_YYYY_MM_DDHHMMSS))
                            .setStatusDesc(CommonStatusEnum.getDescBy(e.getStatus()));
                    return info;
                }).collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Map<Long, String> getDeptNameByIds(Collection<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Map.of();
        }
        Map<Long, DeptNodeInfo> map = this.deptTreeCache.getUnchecked(ROOT_DEPT_ID);

        Map<Long, String> idNames = Maps.newHashMap();
        ids.stream()
                .forEach(id -> {
                    DeptNodeInfo info = map.get(id);
                    if (Objects.isNull(info)) {
                        return;
                    }
                    idNames.put(id, info.getDeptName());
                });
        return idNames;
    }

    @Override
    public String getDeptNameById(Long id) {
        Map<Long, DeptNodeInfo> map = this.deptTreeCache.getUnchecked(ROOT_DEPT_ID);

        return Optional.ofNullable(map.get(id))
                .map(DeptNodeInfo::getDeptName)
                .orElse(null);
    }

    private void clearCache() {
        this.deptTreeCache.invalidateAll();
    }
}
