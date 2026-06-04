package com.jhl.silver.union.biz.service;

import com.jhl.silver.union.SilverUnionOpsApplicationTests;
import com.jhl.silver.union.biz.common.enums.SysConfigTypeEnum;
import com.jhl.silver.union.biz.customer.service.BizConfigService;
import com.jhl.silver.union.biz.data.RecvThirdPlatDataConfig;
import com.jhl.silver.union.biz.sys.dal.entity.SysConfigDO;
import com.jhl.silver.union.biz.sys.manager.SysConfigManager;
import com.jhl.silver.union.biz.sys.service.SysConfigService;
import com.jhl.silver.union.biz.sys.service.impl.SysConfigServiceImpl;
import com.jhl.silver.union.commons.gson.GsonHelper;
import com.jhl.silver.union.web.data.BizDictItem;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

class SysConfigServiceImplWithDBTest {

    // @Resource
    // private SysConfigService sysConfigService;

    @Test
    void addRecvThirdPlatDataConfigSetsChannelAndKey() {
        RecvThirdPlatDataConfig config = new RecvThirdPlatDataConfig()
                .setThirdPlatPublicKey(
                        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqm6R5AZp1Jt39jT381fPZZ4F2q2ToK84DsmDG8X4WJKfn0GRpJ2GNnhTBJP0O+7ic64jLleXJKh87Sg87qyFy2G4W0i45cdhTo4+nRI+nkNzdDeeCaabsGbAQZZReMRnrUatQBRYSMYDwYZxc9TT8NVguG5WTmjOWEKt94vrp+Pfh6SKjZIJtCwBDfo8vPydNSWYSD1xRQdQPMa/uYMWyYodG0XZB7dw0/BQeK44w1WbV+haXm3p3fl27zg7B7o+6Zc8C2vaq4CTIymFSL/54Lv/E+u7HLVI40SnPRnO75zedQlpU9aoL+qAZmE/UPPY45NWwQ3BzDb6su1k0H2FpwIDAQAB")
                .setAppId("TCRM01")
                .setPublicKey(
                        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAn47pnY9F3B8LDI4/plp4GgdfhJ/m9zyQcE/XtZfBHM26IKD6X3WVqCVsqzALtFP/Oy7W6jrHNeGy2ddzV+4/rYLFmfOnYBa6JHUutbiIPL+LRyGXShcrVtJFnmEu4PBR/mvKrMkiZuYgbxhqB7Ee9ycLdX3qro/rVR8+JRR1MIvKuHVNBc6B1ZMMGEcp3IIoLQ1CREdysfcGdAol26M4L5FEsXwY0eWHvtX8vr1azkM4tRrAhCFH+9YuI8kcS6rASPJxqsUEGXNYJ+ru24Mcz3eqhfl3re77RdklG+eA65Ig4RC6i+dax9xrN1njAgqmQ0FOvXqtOT4oiJO3VzXCiwIDAQAB")
                .setPrivateKey(
                        "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCfjumdj0XcHwsMjj+mWngaB1+En+b3PJBwT9e1l8EczbogoPpfdZWoJWyrMAu0U/87LtbqOsc14bLZ13NX7j+tgsWZ86dgFrokdS61uIg8v4tHIZdKFytW0kWeYS7g8FH+a8qsySJm5iBvGGoHsR73Jwt1fequj+tVHz4lFHUwi8q4dU0FzoHVkwwYRyncgigtDUJER3Kx9wZ0CiXbozgvkUSxfBjR5Ye+1fy+vVrOQzi1GsCEIUf71i4jyRxLqsBI8nGqxQQZc1gn6u7bgxzPd6qF+Xet7vtF2SUb54DrkiDhELqL51rH3Gs3WeMCCqZDQU69eq05PiiIk7dXNcKLAgMBAAECggEASfsmoNKUjrqqEdlG8+gQtejjRggqPEqNojWzC9TgSm2tNoHNdUN876jimQE+/A7SUeum5JX6ViZfGhiGt6eVSOtQmdBas/f1uP/Id6OnL5uUhZeyoTza8Hewpf3jkZJ8Qh5SrAjadaGQOlK0nvpmJCyraH/It8WtVRuWYfT5XVdIYpgV3BerczIgB9JKeuhuPgpcwNcQ0CmI2g7wEQ7ES+IaZFPuvG6bIAje/kPrK/uoLequO3UpcGY91rSRSN4bVpvEWYQuwdaRPwW4sekNyKvkAo2x65xnuBsy07/OW64m0HeRJa6yBWZsW090/jeP6DwTipad32kyav62ZulA4QKBgQDOUQkd+60TqeoGyrGoP4ha2erRePBijKvJe/Huf1AmOBAZ4MvX0+G/9Anv7LgcBwsbh+sJ4LxhzsqQaKGrmZcdkyXsuEVBLbfGx+3uSqae6oZWrjOhrJg6dgLcvJLsw7pebt4ozJ2mECmrveXGzuGVIteIOhZcnwpHodJbVgPzEQKBgQDF+1SOpByqdWEqOGj/DfwgQMn5m+cCeHWL8f0N2jcsVJxIrU1dXzC2fa+s9IDbRFofeLs/7wcB2KINno6vOEC9TP7MSxRv+txSfQUZxolxsHW3GTFWyCzDDGtf2Aq8WHczTlSgAhAki68gnOocm2K1FJ5qIZ0taY7f6gGRmwGj2wKBgAk8i4HyIH9+3eFL9cQog/w9QUv7dBeVYKN2jxA0Vuw/GkluTPHupG6piEBbgqqOjiq/XQBmNUjTrzHj3UkHaUKDsfD1FvSiDVYy4S4H3YnDyhvbVKhqR65mVh53usQqxw8vO3bsIiqrEpKDv+O0o1i/5JJOt22SGS23yukX4rlhAoGBAJZgz5pE1y02WSZLkJzij3YkIAXDQFVlD8vLc6246R136vldAR2B9ys2DmDtmo5xvY6YEop+UTE6zeRQYgp/TNU8jXC5On3P6teQ9HXeknlTUiZQMWS8SRuh7FDxdT4YZ/oFbkvXJVHM86lu5nfyIqhuT+FHRO7AdfBn+ucQ+M7vAoGBAIoZUbR4RW0QQxQuPu6IHkSo8nM7Vc9DW0Q8yTSjDbz7NincnAW3hcqPpVEoEqJFLv0QtK44lVQbkh2Q6P/XFqbqdnL+rij7/5dQbMuZG4IzCD1dKaf/t94Gi0OWTTqkmFyG1nbBmxEZPry1mcIJc8kUo62GpfyCmGfVZutQIP2W")
                .setChannelName("CRM测试通道1");
        // sysConfigService.addRecvThirdPlatDataConfig(config, 0L);
    }

    @Test
    void addRecvThirdPlatDataConfigSetsChannelAndKeyPROD() {
        String appId = "SSPH01";
        RecvThirdPlatDataConfig config = new RecvThirdPlatDataConfig()
                .setThirdPlatPublicKey(
                        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwLWjUT2kvUervM6DYZ19qHFkVtcyiU6fe+4kTkLZIqvaayu6BesTmQgZ7hBHPFBrfH1jlbZ91MrmCwq9zmI+RI4v4dnqw5qeLqhsAudeby0/tjOYVH76i/3YjvxabeNDRM09QBkbbH4aqn/ROi1NbiSv/TugXC5W/yO98Fl82oYNGKDQinHtyMhbQpTUJ0qGELmtu81gWkTitoRnmmrK5Sldi5bE1W7n7AgE0/vR4SOZ8dn7w2zqDcKvIof8j92Ktu3MWv6maCMYnTU4Os6bdlX0ByBDJg+XTyRE7fzenfzcAxHtwg2FjUTNPEr158ts5r5UNm5rjfXrCXY6EBKitwIDAQAB")
                .setAppId(appId)
                .setPublicKey(
                        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArYDSQ3WTlyk7h2ut1+F5WFCmjUcMmnOhJX5QtV4IkRvtbNAUzpVtv0SRsg+FVeGmXy9j43jTTH+n+wuk6j3TFqrJ85qTGuOAI5S0B5U2hw83hzxItGdrYNDFVx0AXemuNAb6N3voWdp6TI44pPj1wBu8mwX5jZvLXioj0iJTG07CdRaM5BrViRvHArKLje+IEDJOGCNlafk4gYPHpjMTrMAv7sLds5gdNRDmsw12ax/CDTWLoYdwoVMeTSb3yAjMuLi8LzcphAQRDPpsODTINlXAlFaP4rB+F1LaFOj2ayjhnr23a5/TpRNMM3y/A/g3ebl+IFEf7zOSDX3NAtwNnQIDAQAB")
                .setPrivateKey(
                        "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCtgNJDdZOXKTuHa63X4XlYUKaNRwyac6ElflC1XgiRG+1s0BTOlW2/RJGyD4VV4aZfL2PjeNNMf6f7C6TqPdMWqsnzmpMa44AjlLQHlTaHDzeHPEi0Z2tg0MVXHQBd6a40Bvo3e+hZ2npMjjik+PXAG7ybBfmNm8teKiPSIlMbTsJ1FozkGtWJG8cCsouN74gQMk4YI2Vp+TiBg8emMxOswC/uwt2zmB01EOazDXZrH8INNYuhh3ChUx5NJvfICMy4uLwvNymEBBEM+mw4NMg2VcCUVo/isH4XUtoU6PZrKOGevbdrn9OlE0wzfL8D+Dd5uX4gUR/vM5INfc0C3A2dAgMBAAECggEAC0bRo6/LIWp9I36UpHDTpbFMOedxwsIcKactkLQlY2UIx+c4Svr8UI+g1o1asavCV/JpEBpExXaQkOj/VXgiNZlxGcOkpv09UVKzZsDEc//BsKjy4sLMAFVhVhmc8f2jpF8FuUB06A4in8IonVRPCKbk2cCIKTOEgsDDZj9z3lTjM4z1VEK5t2IV9MCoEo/8rnjQbf77dS2fy/FaEPpHmg4OwnJChB/zeNAp8L6HX/aherWVDCjoIqpGLhqMXFzD6u8BOJZ8XRuvjxzk1wBnT7Sz7WgBJZvsd36XP1CTZ0DfNLtUcGgf8mpip71fktKR47A+9UbrZFzeBZPqjk9aEQKBgQDcOOhBYAsOuXv8i+eCUTg1bwgKhjR6Ob0SxkCmBJk2ojQME183yDsIVldiGt2OxgkXXPFnvqVgErA8sgULHN8k8jRsYS/A7L3xIlGdkJMe5U7HKYVBj6bZL/iWhGEup/6nH1Eir6dft7JdRqtm310XQU3MB756Duh91yrFEjMGuQKBgQDJsNtz+w7oEgR/6yRL5/Csqcoy0DQB7U7ACWJxhAK0bEMRhqVPh/0YMJLyrMYHpiuGdRAjLjjsQHr7yYv5iRuikJS1wq/+oJYrSa6MuvgobGcB8e27ApabO32PBujrn3LNh0uTRxIBKX1bVvRN4k5Vi2HFLTmRdHTi17L5Yc5MBQKBgCXHkXidrGfzRCT4L1g47Mal7KA5k8kgHKKa5j5U7Z/kxb8Il74vGIl+nnETIM08hhS++h8NIrvPmPJgq+LTKijhcELjjViwhIlYlN3ODp8vQYuDQPwVU89Qnnnb7fweD97FzZqAvdr63ZuI4yC70vGJX3pQHco8wn/MKOc0DxLpAoGBAIm1mrE+6vejTbU1jOvS6q5Jo8g8BjaGnC1cev28v8gG32g5AegilrmMeNj/BJs2ZyOADEupBK6yfAYsn8j6R4cgppjP3DDD3qShw29NPaEEGvTmBYaZhtFothsRAbnXfaA9T2m4rd9jP7c80MPYz9lyuIfTkCnzVNeJpSbHE/8tAoGAQ/7Za2FqKnG4n1wCt1kj3k9olqNq1H0Ck4HhxqSnsbxuztmkXyo4jnkPfVVCWFGFjmt6VUx3fl53kJsWXBXbU+x32rH+ojsD6SdReauY6oxwQwX3Xd8qC5Bk0eOhFikw5C5xejGtRxbT7dBH1N/CfhpSe9hMGSAVCDfnCSudr94=")
                .setChannelName("CRM渠道01")
                .setChannelId(1L);
        // sysConfigService.addRecvThirdPlatDataConfig(config, 0L);

        String json = GsonHelper.toJson(config);
        String sql = """
                INSERT INTO `sys_config` (`cnf_type`, `cnf_key`, `cnf_desc`, `cnf_value`)
                VALUES
                	('RECV_THIRD_PLAT_DATA', '%s', '接收三方平台推送数据配置', '%s');
                """.formatted(appId, json);
        System.out.println(sql);
    }

}
