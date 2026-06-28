package com.jhl.silver.union.biz.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.jhl.silver.union.biz.region.dal.entity.IdCardAreaDO;
import com.jhl.silver.union.biz.region.dal.entity.RegionAreaDO;
import com.jhl.silver.union.biz.region.manager.IdCardAreaManager;
import com.jhl.silver.union.biz.region.manager.RegionAreaManager;
import com.jhl.silver.union.biz.region.service.impl.RegionAreaServiceImpl;
import com.jhl.silver.union.web.data.region.RegionItemDTO;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

class RegionAreaServiceImplTest {

    @Test
    void includesHubeiDirectAdminCitiesWhenFallbackToIdCardArea() {
        RegionAreaManager regionAreaManager = Mockito.mock(RegionAreaManager.class);
        IdCardAreaManager idCardAreaManager = Mockito.mock(IdCardAreaManager.class);
        RegionAreaServiceImpl service = new RegionAreaServiceImpl();
        ReflectionTestUtils.setField(service, "regionAreaManager", regionAreaManager);
        ReflectionTestUtils.setField(service, "idCardAreaManager", idCardAreaManager);

        Mockito.when(regionAreaManager.list(ArgumentMatchers.<Wrapper<RegionAreaDO>>any()))
                .thenReturn(List.of());
        Mockito.when(idCardAreaManager.list()).thenReturn(List.of(
                area("420000", "湖北省"),
                area("420100", "武汉市"),
                area("429004", "仙桃市*"),
                area("429005", "潜江市*"),
                area("429006", "天门市*")
        ));

        List<RegionItemDTO> cities = service.listChildren(420000L);

        Assertions.assertTrue(cities.stream().anyMatch(item -> "仙桃市".equals(item.getName())));
        Assertions.assertTrue(cities.stream().anyMatch(item -> "潜江市".equals(item.getName())));
        Assertions.assertTrue(cities.stream().anyMatch(item -> "天门市".equals(item.getName())));
    }

    private IdCardAreaDO area(String code, String name) {
        return new IdCardAreaDO().setCode(code).setName(name);
    }
}
