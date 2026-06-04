package com.jhl.silver.union.biz.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhl.silver.union.biz.common.enums.DispatchModeEnum;
import com.jhl.silver.union.biz.customer.dal.entity.CustDispatchCursorDO;
import com.jhl.silver.union.biz.customer.dal.entity.CustDispatchDailyStatDO;
import com.jhl.silver.union.biz.customer.dal.entity.CustDispatchPlanDO;
import com.jhl.silver.union.biz.customer.dal.entity.CustDispatchPlanUserDO;
import com.jhl.silver.union.biz.customer.dal.mapper.CustDispatchDailyStatMapper;
import com.jhl.silver.union.biz.customer.manager.CustDispatchCursorManager;
import com.jhl.silver.union.biz.customer.manager.CustDispatchDailyStatManager;
import com.jhl.silver.union.biz.customer.manager.CustDispatchPlanManager;
import com.jhl.silver.union.biz.customer.manager.CustDispatchPlanUserManager;
import com.jhl.silver.union.biz.customer.service.CustDispatchService;
import com.jhl.silver.union.biz.data.UserQry;
import com.jhl.silver.union.biz.dept.service.DeptService;
import com.jhl.silver.union.biz.user.dal.entity.SuUserInfoDO;
import com.jhl.silver.union.biz.user.manager.SuUserInfoManager;
import com.jhl.silver.union.commons.CommonConstant;
import com.jhl.silver.union.commons.CommonResultCode;
import com.jhl.silver.union.commons.exception.BizException;
import com.jhl.silver.union.commons.utils.SuDateUtils;
import com.jhl.silver.union.commons.utils.VerifyUtils;
import com.jhl.silver.union.web.data.admin.DispatchPlanDTO;
import com.jhl.silver.union.web.data.admin.DispatchPlanMemberDTO;
import com.jhl.silver.union.web.data.admin.DispatchPlanMemberRequest;
import com.jhl.silver.union.web.data.admin.SaveDispatchPlanRequest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CustDispatchServiceImpl implements CustDispatchService {
    @Resource
    private CustDispatchPlanManager planManager;
    @Resource
    private CustDispatchPlanUserManager planUserManager;
    @Resource
    private CustDispatchCursorManager cursorManager;
    @Resource
    private CustDispatchDailyStatManager dailyStatManager;
    @Resource
    private CustDispatchDailyStatMapper dailyStatMapper;
    @Resource
    private SuUserInfoManager userInfoManager;
    @Resource
    private DeptService deptService;

    private static final String DATE_TIME_FMT = SuDateUtils.DF_YYYY_MM_DDHHMMSS;

    @Override
    public DispatchPlanDTO getActivePlan() {
        CustDispatchPlanDO plan = getLatestPlan();
        if (plan == null) {
            return new DispatchPlanDTO().setMode(DispatchModeEnum.MANUAL.code);
        }
        DispatchPlanDTO dto = new DispatchPlanDTO()
                .setId(plan.getId())
                .setMode(plan.getMode())
                .setEffectStart(SuDateUtils.format(plan.getEffectStart(), DATE_TIME_FMT))
                .setEffectEnd(SuDateUtils.format(plan.getEffectEnd(), DATE_TIME_FMT));
        LambdaQueryWrapper<CustDispatchPlanUserDO> qw = new LambdaQueryWrapper<CustDispatchPlanUserDO>()
                .eq(CustDispatchPlanUserDO::getPlanId, plan.getId())
                .orderByAsc(CustDispatchPlanUserDO::getSortNo)
                .orderByAsc(CustDispatchPlanUserDO::getId);
        List<CustDispatchPlanUserDO> userList = planUserManager.list(qw);
        if (CollectionUtils.isEmpty(userList)) {
            dto.setMembers(List.of());
            return dto;
        }
        Set<Long> userIds = userList.stream()
                .map(CustDispatchPlanUserDO::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, SuUserInfoDO> userMap = userInfoManager.list(new UserQry()
                        .setIds(userIds)
                        .toQryWrapper(true))
                .stream()
                .collect(Collectors.toMap(SuUserInfoDO::getId, Function.identity(), (v1, v2) -> v2));
        Map<Long, String> deptNameMap = deptService.getDeptNameByIds(
                userList.stream().map(CustDispatchPlanUserDO::getDeptId).collect(Collectors.toList()));
        List<DispatchPlanMemberDTO> members = userList.stream()
                .map(item -> {
                    SuUserInfoDO user = userMap.get(item.getUserId());
                    return new DispatchPlanMemberDTO()
                            .setUserId(item.getUserId())
                            .setUserName(user != null ? user.getRealName() : "")
                            .setDeptId(item.getDeptId())
                            .setDeptName(deptNameMap.getOrDefault(item.getDeptId(), ""))
                            .setDailyLimit(item.getDailyLimit())
                            .setSortNo(item.getSortNo())
                            .setStatus(item.getStatus());
                })
                .collect(Collectors.toList());
        dto.setMembers(members);
        return dto;
    }

    @Override
    public boolean isAutoModeActive() {
        return getActiveAutoPlan() != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveDispatchPlan(SaveDispatchPlanRequest request, Long optUserId) {
        request.validate();
        Date startTime = parseDateTime(request.getEffectStart(), "effectStart");
        Date endTime = parseDateTime(request.getEffectEnd(), "effectEnd");
        VerifyUtils.verifyTrue(startTime.before(endTime), "生效时间区间不合法", true);

        CustDispatchPlanDO plan = getLatestPlan();
        if (plan == null) {
            plan = new CustDispatchPlanDO();
        }
        plan.setMode(StringUtils.upperCase(request.getMode()))
                .setEffectStart(startTime)
                .setEffectEnd(endTime)
                .setStatus(CommonConstant.YES)
                .setOptUserId(optUserId);
        planManager.saveOrUpdate(plan);

        LambdaQueryWrapper<CustDispatchPlanUserDO> delQw = new LambdaQueryWrapper<CustDispatchPlanUserDO>()
                .eq(CustDispatchPlanUserDO::getPlanId, plan.getId());
        planUserManager.remove(delQw);
        cursorManager.remove(new LambdaQueryWrapper<CustDispatchCursorDO>()
                .eq(CustDispatchCursorDO::getPlanId, plan.getId()));

        if (!CollectionUtils.isEmpty(request.getMembers())) {
            List<CustDispatchPlanUserDO> toSave = new ArrayList<>();
            int index = 0;
            for (DispatchPlanMemberRequest member : request.getMembers()) {
                if (member == null) {
                    continue;
                }
                member.validate();
                CustDispatchPlanUserDO user = new CustDispatchPlanUserDO()
                        .setPlanId(plan.getId())
                        .setDeptId(member.getDeptId())
                        .setUserId(member.getUserId())
                        .setDailyLimit(member.getDailyLimit())
                        .setSortNo(member.getSortNo() != null ? member.getSortNo() : index)
                        .setStatus(CommonConstant.YES);
                toSave.add(user);
                index++;
            }
            if (!toSave.isEmpty()) {
                planUserManager.saveBatch(toSave);
            }
        }
    }

    @Override
    public Optional<DispatchUserResult> pickAutoAssignee(Long targetDeptId) {
        // if (targetDeptId == null || targetDeptId <= 0) {
        //     return Optional.empty();
        // }
        CustDispatchPlanDO plan = getActiveAutoPlan();
        if (plan == null) {
            return Optional.empty();
        }
        List<CustDispatchPlanUserDO> planUsers = planUserManager.list(new LambdaQueryWrapper<CustDispatchPlanUserDO>()
                .eq(CustDispatchPlanUserDO::getPlanId, plan.getId())
                .eq(Objects.nonNull(targetDeptId) && targetDeptId > 0L, CustDispatchPlanUserDO::getDeptId, targetDeptId)
                .eq(CustDispatchPlanUserDO::getStatus, CommonConstant.YES)
                .orderByAsc(CustDispatchPlanUserDO::getSortNo)
                .orderByAsc(CustDispatchPlanUserDO::getId));
        if (CollectionUtils.isEmpty(planUsers)) {
            return Optional.empty();
        }
        List<CustDispatchPlanUserDO> eligible = filterOnlineUsers(planUsers);
        if (eligible.isEmpty()) {
            return Optional.empty();
        }
        Long lastUserId = getLastCursorUserId(plan.getId(), targetDeptId);
        int startIndex = findNextIndex(eligible, lastUserId);
        int size = eligible.size();
        for (int i = 0; i < size; i++) {
            int idx = (startIndex + i) % size;
            CustDispatchPlanUserDO candidate = eligible.get(idx);
            Integer limit = candidate.getDailyLimit();
            if (limit == null || limit <= 0) {
                continue;
            }
            if (!isUserOnline(candidate.getUserId())) {
                continue;
            }
            boolean ok = incrementAutoCount(candidate.getUserId(), candidate.getDeptId(), limit);
            if (ok) {
                updateCursor(plan.getId(), targetDeptId, candidate.getUserId());
                return Optional.of(new DispatchUserResult(candidate.getUserId(), candidate.getDeptId()));
            }
        }
        return Optional.empty();
    }

    @Override
    public void recordManualAssignment(Long userId, Long deptId, int count) {
        if (userId == null || userId <= 0 || count <= 0) {
            return;
        }
        if (deptId == null) {
            deptId = 0L;
        }
        Date statDate = getTodayDate();
        int updated = dailyStatMapper.incrementManualCountBy(statDate, userId, count);
        if (updated > 0) {
            return;
        }
        CustDispatchDailyStatDO stat = new CustDispatchDailyStatDO()
                .setStatDate(statDate)
                .setUserId(userId)
                .setDeptId(deptId)
                .setAutoCount(0)
                .setManualCount(count);
        try {
            dailyStatManager.save(stat);
        } catch (Exception e) {
            dailyStatMapper.incrementManualCountBy(statDate, userId, count);
        }
    }

    private boolean incrementAutoCount(Long userId, Long deptId, int limit) {
        if (deptId == null) {
            deptId = 0L;
        }
        Date statDate = getTodayDate();
        int updated = dailyStatMapper.incrementAutoCount(statDate, userId, limit);
        if (updated > 0) {
            return true;
        }
        CustDispatchDailyStatDO stat = new CustDispatchDailyStatDO()
                .setStatDate(statDate)
                .setUserId(userId)
                .setDeptId(deptId)
                .setAutoCount(1)
                .setManualCount(0);
        try {
            dailyStatManager.save(stat);
            return true;
        } catch (Exception e) {
            updated = dailyStatMapper.incrementAutoCount(statDate, userId, limit);
            return updated > 0;
        }
    }

    private List<CustDispatchPlanUserDO> filterOnlineUsers(List<CustDispatchPlanUserDO> planUsers) {
        List<Long> userIds = planUsers.stream()
                .map(CustDispatchPlanUserDO::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userIds)) {
            return List.of();
        }
        UserQry qry = new UserQry()
                .setIds(userIds)
                .setOnlineStatus(CommonConstant.YES);
        List<SuUserInfoDO> onlineUsers = userInfoManager.list(qry.toQryWrapper(true));
        if (CollectionUtils.isEmpty(onlineUsers)) {
            return List.of();
        }
        Set<Long> onlineSet = onlineUsers.stream()
                .map(SuUserInfoDO::getId)
                .collect(Collectors.toSet());
        return planUsers.stream()
                .filter(item -> onlineSet.contains(item.getUserId()))
                .collect(Collectors.toList());
    }

    private boolean isUserOnline(Long userId) {
        if (userId == null || userId <= 0) {
            return false;
        }
        SuUserInfoDO user = userInfoManager.queryUserById(userId);
        return user != null && Objects.equals(user.getOnlineStatus(), CommonConstant.YES);
    }

    private int findNextIndex(List<CustDispatchPlanUserDO> users, Long lastUserId) {
        if (lastUserId == null || lastUserId <= 0) {
            return 0;
        }
        int idx = 0;
        for (int i = 0; i < users.size(); i++) {
            if (Objects.equals(users.get(i).getUserId(), lastUserId)) {
                idx = i + 1;
                break;
            }
        }
        if (idx >= users.size()) {
            idx = 0;
        }
        return idx;
    }

    private Long getLastCursorUserId(Long planId, Long deptId) {
        CustDispatchCursorDO cursor = cursorManager.getOne(new LambdaQueryWrapper<CustDispatchCursorDO>()
                .eq(CustDispatchCursorDO::getPlanId, planId)
                .eq(CustDispatchCursorDO::getDeptId, deptId));
        return cursor != null ? cursor.getLastUserId() : null;
    }

    private void updateCursor(Long planId, Long deptId, Long lastUserId) {
        CustDispatchCursorDO cursor = cursorManager.getOne(new LambdaQueryWrapper<CustDispatchCursorDO>()
                .eq(CustDispatchCursorDO::getPlanId, planId)
                .eq(CustDispatchCursorDO::getDeptId, deptId));
        if (cursor == null) {
            cursor = new CustDispatchCursorDO()
                    .setPlanId(planId)
                    .setDeptId(deptId)
                    .setLastUserId(lastUserId);
            cursorManager.save(cursor);
        } else {
            cursorManager.updateById(cursor.setLastUserId(lastUserId));
        }
    }

    private CustDispatchPlanDO getLatestPlan() {
        return planManager.getOne(new LambdaQueryWrapper<CustDispatchPlanDO>()
                .eq(CustDispatchPlanDO::getStatus, CommonConstant.YES)
                .orderByDesc(CustDispatchPlanDO::getGmtModified)
                .last("limit 1"));
    }

    private CustDispatchPlanDO getActiveAutoPlan() {
        CustDispatchPlanDO plan = getLatestPlan();
        if (plan == null) {
            return null;
        }
        if (!DispatchModeEnum.isAuto(plan.getMode())) {
            return null;
        }
        Date now = new Date();
        if (plan.getEffectStart() != null && now.before(plan.getEffectStart())) {
            return null;
        }
        if (plan.getEffectEnd() != null && now.after(plan.getEffectEnd())) {
            return null;
        }
        return plan;
    }

    private Date parseDateTime(String value, String field) {
        try {
            return SuDateUtils.parse(value, DATE_TIME_FMT);
        } catch (ParseException e) {
            throw new BizException(CommonResultCode.INVALID_PARAMS, field);
        }
    }

    private Date getTodayDate() {
        LocalDate today = LocalDate.now();
        return java.sql.Date.valueOf(today);
    }
}
