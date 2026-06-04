package com.jhl.silver.union.web.controller.platform;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhl.silver.union.biz.platform.dal.entity.BizPlatformDO;
import com.jhl.silver.union.biz.platform.service.BizPlatformService;
import com.jhl.silver.union.commons.SuResult;
import com.jhl.silver.union.web.data.platform.PlatformPageRequest;
import com.jhl.silver.union.web.data.platform.PlatformSaveRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/biz/platform")
@Tag(name = "分期/租赁平台管理")
public class PlatformController {

    @Resource
    private BizPlatformService bizPlatformService;

    @PostMapping("/page")
    @Operation(summary = "分页查询平台列表")
    public SuResult<IPage<BizPlatformDO>> pageList(@RequestBody PlatformPageRequest request) {
        Page<BizPlatformDO> page = new Page<>(request.getPage(), request.getPageSize());
        LambdaQueryWrapper<BizPlatformDO> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(request.getName())) {
            wrapper.like(BizPlatformDO::getName, request.getName());
        }
        if (StringUtils.isNotBlank(request.getType())) {
            wrapper.eq(BizPlatformDO::getType, request.getType());
        }
        if (request.getStatus() != null) {
            wrapper.eq(BizPlatformDO::getStatus, request.getStatus());
        }
        wrapper.orderByDesc(BizPlatformDO::getId);

        return new SuResult<IPage<BizPlatformDO>>().setCode(0).setData(bizPlatformService.page(page, wrapper));
    }

    @PostMapping("/save")
    @Operation(summary = "保存或更新平台")
    public SuResult<Boolean> save(@RequestBody PlatformSaveRequest request) {
        BizPlatformDO entity = new BizPlatformDO();
        BeanUtils.copyProperties(request, entity);

        boolean result;
        if (entity.getId() != null) {
            // Update
            entity.setGmtModified(new Date());
            result = bizPlatformService.updateById(entity);
        } else {
            // Insert
            entity.setGmtCreate(new Date());
            entity.setGmtModified(new Date());
            result = bizPlatformService.save(entity);
        }
        return new SuResult<Boolean>().setCode(0).setData(result);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除平台")
    public SuResult<Boolean> delete(@RequestBody Long id) {
        return new SuResult<Boolean>().setCode(0).setData(bizPlatformService.removeById(id));
    }

    @GetMapping("/options")
    @Operation(summary = "获取平台选项列表(启用状态)")
    public SuResult<List<BizPlatformDO>> getOptions() {
        LambdaQueryWrapper<BizPlatformDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizPlatformDO::getStatus, 1);
        wrapper.orderByDesc(BizPlatformDO::getId);
        return new SuResult<List<BizPlatformDO>>().setCode(0).setData(bizPlatformService.list(wrapper));
    }
}
