package com.jhl.silver.union.biz.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum;
import com.jhl.silver.union.biz.customer.dal.entity.CustNoticeDO;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerInfoItemDO;
import com.jhl.silver.union.biz.customer.manager.CustomerInfoItemManager;
import com.jhl.silver.union.biz.customer.manager.CustNoticeManager;
import com.jhl.silver.union.biz.customer.service.CustNoticeService;
import com.jhl.silver.union.biz.data.UserQry;
import com.jhl.silver.union.biz.user.dal.entity.SuUserInfoDO;
import com.jhl.silver.union.biz.user.manager.SuUserInfoManager;
import com.jhl.silver.union.web.data.customer.CustomerNoticeItemDTO;
import com.jhl.silver.union.web.websocket.CustNoticeWebSocketHandler;
import com.jhl.silver.union.web.websocket.CustNoticeWsMessage;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class CustNoticeServiceImpl implements CustNoticeService {
    private static final int STATUS_UNREAD = 0;
    private static final int STATUS_READ = 1;
    private static final String TYPE_NEW = "NEW";
    private static final String TYPE_UPDATE = "UPDATE";
    private static final String TYPE_ASSIGN = "ASSIGN";
    private static final String SOURCE_API = "API";
    private static final String SOURCE_SYSTEM = "SYSTEM";

    @Resource
    private CustNoticeManager noticeManager;
    @Resource
    private CustomerInfoItemManager customerInfoItemManager;
    @Resource
    private SuUserInfoManager userInfoManager;
    @Resource
    private CustNoticeWebSocketHandler webSocketHandler;

    @Override
    public List<CustomerNoticeItemDTO> listUnread(Long userId, int limit) {
        if (userId == null || userId <= 0) {
            return List.of();
        }
        int size = limit > 0 ? limit : 20;
        ensureUnreadForUnprocessed(userId, size);
        LambdaQueryWrapper<CustNoticeDO> qw = new LambdaQueryWrapper<CustNoticeDO>()
                .eq(CustNoticeDO::getUserId, userId)
                .eq(CustNoticeDO::getStatus, STATUS_UNREAD)
                .orderByDesc(CustNoticeDO::getGmtCreate)
                .last("limit " + size);
        List<CustNoticeDO> list = noticeManager.list(qw);
        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        }
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public void markRead(Long userId, List<Long> ids) {
        if (userId == null || userId <= 0 || CollectionUtils.isEmpty(ids)) {
            return;
        }
        LambdaUpdateWrapper<CustNoticeDO> uw = new LambdaUpdateWrapper<>();
        uw.eq(CustNoticeDO::getUserId, userId)
                .in(CustNoticeDO::getId, ids)
                .set(CustNoticeDO::getStatus, STATUS_READ);
        noticeManager.update(uw);
    }

    @Override
    public void markReadByCustomer(Long userId, Long custId) {
        if (userId == null || userId <= 0 || custId == null || custId <= 0) {
            return;
        }
        LambdaUpdateWrapper<CustNoticeDO> uw = new LambdaUpdateWrapper<>();
        uw.eq(CustNoticeDO::getUserId, userId)
                .eq(CustNoticeDO::getCustId, custId)
                .set(CustNoticeDO::getStatus, STATUS_READ);
        noticeManager.update(uw);
    }

    @Override
    public void notifyNewAssignment(List<CustomerInfoItemDO> inserted, List<CustomerInfoItemDO> updated,
            String source) {
        String src = StringUtils.isBlank(source) ? SOURCE_SYSTEM : source;
        List<CustNoticeDO> notices = new ArrayList<>();
        buildNoticeItems(inserted, TYPE_NEW, src, notices);
        buildNoticeItems(updated, TYPE_UPDATE, src, notices);
        persistAndBroadcast(notices);
    }

    @Override
    public void notifyAssign(List<CustomerInfoItemDO> assigned, String source) {
        String src = StringUtils.isBlank(source) ? SOURCE_SYSTEM : source;
        List<CustNoticeDO> notices = new ArrayList<>();
        buildNoticeItems(assigned, TYPE_ASSIGN, src, notices);
        persistAndBroadcast(notices);
    }

    private void persistAndBroadcast(List<CustNoticeDO> notices) {
        if (notices.isEmpty()) {
            return;
        }
        for (CustNoticeDO notice : notices) {
            noticeManager.save(notice);
            sendWsNotice(notice);
        }
    }

    private void buildNoticeItems(List<CustomerInfoItemDO> items, String type, String source,
            List<CustNoticeDO> notices) {
        if (CollectionUtils.isEmpty(items)) {
            return;
        }
        Map<Long, CustNoticeDO> dedup = new LinkedHashMap<>();
        for (CustomerInfoItemDO item : items) {
            if (item == null || item.getId() == null) {
                continue;
            }
            dedup.putIfAbsent(item.getId(), buildBaseNotice(item, type, source));
        }
        for (CustNoticeDO base : dedup.values()) {
            List<Long> receivers = resolveReceivers(base.getOwnerUserId(), base.getOwnerDeptId());
            if (receivers.isEmpty()) {
                continue;
            }
            for (Long userId : receivers) {
                notices.add(cloneForReceiver(base, userId));
            }
        }
    }

    private CustNoticeDO buildBaseNotice(CustomerInfoItemDO item, String type, String source) {
        Long ownerUserId = item.getOwnerUserId() == null ? 0L : item.getOwnerUserId();
        Long ownerDeptId = item.getOwnerDeptId() == null ? 0L : item.getOwnerDeptId();
        Integer ownerFavorite = item.getOwnerFavorite() == null ? 0 : item.getOwnerFavorite();
        return new CustNoticeDO()
                .setCustId(item.getId())
                .setCustName(item.getName())
                .setCustMobile(item.getMobile())
                .setCustIdCard(item.getIdCardNo())
                .setOwnerUserId(ownerUserId)
                .setOwnerDeptId(ownerDeptId)
                .setOwnerFavorite(ownerFavorite)
                .setNoticeType(type)
                .setSource(source)
                .setStatus(STATUS_UNREAD);
    }

    private void ensureUnreadForUnprocessed(Long userId, int limit) {
        if (userId == null || userId <= 0 || limit <= 0) {
            return;
        }
        LambdaQueryWrapper<CustomerInfoItemDO> cw = new LambdaQueryWrapper<CustomerInfoItemDO>()
                .eq(CustomerInfoItemDO::getOwnerUserId, userId)
                .and(qw -> qw.isNull(CustomerInfoItemDO::getFollowerUserId)
                        .or()
                        .ne(CustomerInfoItemDO::getFollowerUserId, userId))
                .orderByDesc(CustomerInfoItemDO::getGmtCreate)
                .last("limit " + limit);
        List<CustomerInfoItemDO> candidates = customerInfoItemManager.list(cw);
        if (CollectionUtils.isEmpty(candidates)) {
            return;
        }
        List<Long> custIds = candidates.stream()
                .map(CustomerInfoItemDO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (custIds.isEmpty()) {
            return;
        }
        LambdaQueryWrapper<CustNoticeDO> nq = new LambdaQueryWrapper<CustNoticeDO>()
                .eq(CustNoticeDO::getUserId, userId)
                .in(CustNoticeDO::getCustId, custIds)
                .orderByDesc(CustNoticeDO::getGmtCreate);
        List<CustNoticeDO> existing = noticeManager.list(nq);
        Map<Long, CustNoticeDO> noticeMap = new LinkedHashMap<>();
        if (!CollectionUtils.isEmpty(existing)) {
            for (CustNoticeDO notice : existing) {
                if (notice == null || notice.getCustId() == null) {
                    continue;
                }
                noticeMap.putIfAbsent(notice.getCustId(), notice);
            }
        }
        List<CustNoticeDO> toInsert = new ArrayList<>();
        List<Long> toUnreadIds = new ArrayList<>();
        for (CustomerInfoItemDO item : candidates) {
            if (item == null || item.getId() == null) {
                continue;
            }
            CustNoticeDO existed = noticeMap.get(item.getId());
            if (existed == null) {
                CustNoticeDO base = buildBaseNotice(item, TYPE_ASSIGN, SOURCE_SYSTEM);
                CustNoticeDO notice = cloneForReceiver(base, userId);
                toInsert.add(notice);
                continue;
            }
            if (!Objects.equals(existed.getStatus(), STATUS_UNREAD)) {
                toUnreadIds.add(existed.getId());
            }
        }
        if (!toInsert.isEmpty()) {
            noticeManager.saveBatch(toInsert);
        }
        if (!toUnreadIds.isEmpty()) {
            LambdaUpdateWrapper<CustNoticeDO> uw = new LambdaUpdateWrapper<CustNoticeDO>()
                    .in(CustNoticeDO::getId, toUnreadIds)
                    .set(CustNoticeDO::getStatus, STATUS_UNREAD);
            noticeManager.update(uw);
        }
    }

    private List<Long> resolveReceivers(Long ownerUserId, Long ownerDeptId) {
        if (ownerUserId != null && ownerUserId > 0) {
            return List.of(ownerUserId);
        }
        if (ownerDeptId == null || ownerDeptId <= 0) {
            return listUsersByRole(UserAuthRoleEnum.ROLE_SUPPER.name());
        }
        UserQry qry = new UserQry().setDeptIds(List.of(ownerDeptId));
        List<SuUserInfoDO> users = userInfoManager.list(qry.toQryWrapper(true));
        if (CollectionUtils.isEmpty(users)) {
            return List.of();
        }
        return users.stream()
                .filter(this::isDeptDataAdmin)
                .map(SuUserInfoDO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private boolean isDeptDataAdmin(SuUserInfoDO user) {
        if (user == null) {
            return false;
        }
        String roles = user.getRoles();
        if (StringUtils.isBlank(roles)) {
            return false;
        }
        return StringUtils.contains(roles, UserAuthRoleEnum.ROLE_DEPT_DATA_ADMIN.name());
    }

    private List<Long> listUsersByRole(String roleName) {
        UserQry qry = new UserQry();
        List<SuUserInfoDO> users = userInfoManager.list(qry.toQryWrapper(true));
        if (CollectionUtils.isEmpty(users)) {
            return List.of();
        }
        return users.stream()
                .filter(user -> StringUtils.contains(user.getRoles(), roleName))
                .map(SuUserInfoDO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private CustNoticeDO cloneForReceiver(CustNoticeDO base, Long userId) {
        return new CustNoticeDO()
                .setUserId(userId)
                .setDeptId(base.getOwnerDeptId())
                .setCustId(base.getCustId())
                .setCustName(base.getCustName())
                .setCustMobile(base.getCustMobile())
                .setCustIdCard(base.getCustIdCard())
                .setOwnerUserId(base.getOwnerUserId())
                .setOwnerDeptId(base.getOwnerDeptId())
                .setOwnerFavorite(base.getOwnerFavorite())
                .setNoticeType(base.getNoticeType())
                .setSource(base.getSource())
                .setStatus(base.getStatus());
    }

    private void sendWsNotice(CustNoticeDO notice) {
        if (notice == null || notice.getUserId() == null || notice.getUserId() <= 0) {
            return;
        }
        CustomerNoticeItemDTO dto = convert(notice);
        CustNoticeWsMessage message = new CustNoticeWsMessage()
                .setType("NOTICE")
                .setNotice(dto);
        webSocketHandler.sendToUser(notice.getUserId(), message);
    }

    private CustomerNoticeItemDTO convert(CustNoticeDO notice) {
        if (notice == null) {
            return null;
        }
        return new CustomerNoticeItemDTO()
                .setId(notice.getId())
                .setUserId(notice.getUserId())
                .setDeptId(notice.getDeptId())
                .setCustId(notice.getCustId())
                .setCustName(notice.getCustName())
                .setCustMobile(notice.getCustMobile())
                .setCustIdCard(notice.getCustIdCard())
                .setOwnerUserId(notice.getOwnerUserId())
                .setOwnerDeptId(notice.getOwnerDeptId())
                .setOwnerFavorite(notice.getOwnerFavorite())
                .setNoticeType(notice.getNoticeType())
                .setSource(notice.getSource())
                .setStatus(notice.getStatus())
                .setGmtCreate(notice.getGmtCreate());
    }
}
