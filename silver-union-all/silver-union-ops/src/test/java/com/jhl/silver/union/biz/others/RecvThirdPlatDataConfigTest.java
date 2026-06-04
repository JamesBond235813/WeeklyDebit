package com.jhl.silver.union.biz.others;

import com.jhl.silver.union.biz.data.RecvThirdPlatDataConfig;
import com.jhl.silver.union.commons.gson.GsonHelper;
import org.junit.jupiter.api.Test;

/**
 * @author: qingren
 * @create_time: 2026/1/26
 */
public class RecvThirdPlatDataConfigTest {
    @Test
    void recvThirdPlatDataConfigTest() {
        RecvThirdPlatDataConfig config=  new RecvThirdPlatDataConfig()
                .setThirdPlatPublicKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqm6R5AZp1Jt39jT381fPZZ4F2q2ToK84DsmDG8X4WJKfn0GRpJ2GNnhTBJP0O+7ic64jLleXJKh87Sg87qyFy2G4W0i45cdhTo4+nRI+nkNzdDeeCaabsGbAQZZReMRnrUatQBRYSMYDwYZxc9TT8NVguG5WTmjOWEKt94vrp+Pfh6SKjZIJtCwBDfo8vPydNSWYSD1xRQdQPMa/uYMWyYodG0XZB7dw0/BQeK44w1WbV+haXm3p3fl27zg7B7o+6Zc8C2vaq4CTIymFSL/54Lv/E+u7HLVI40SnPRnO75zedQlpU9aoL+qAZmE/UPPY45NWwQ3BzDb6su1k0H2FpwIDAQAB")
                .setAppId("TCRM01")
                .setPublicKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAn47pnY9F3B8LDI4/plp4GgdfhJ/m9zyQcE/XtZfBHM26IKD6X3WVqCVsqzALtFP/Oy7W6jrHNeGy2ddzV+4/rYLFmfOnYBa6JHUutbiIPL+LRyGXShcrVtJFnmEu4PBR/mvKrMkiZuYgbxhqB7Ee9ycLdX3qro/rVR8+JRR1MIvKuHVNBc6B1ZMMGEcp3IIoLQ1CREdysfcGdAol26M4L5FEsXwY0eWHvtX8vr1azkM4tRrAhCFH+9YuI8kcS6rASPJxqsUEGXNYJ+ru24Mcz3eqhfl3re77RdklG+eA65Ig4RC6i+dax9xrN1njAgqmQ0FOvXqtOT4oiJO3VzXCiwIDAQAB")
                .setPrivateKey("MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCfjumdj0XcHwsMjj+mWngaB1+En+b3PJBwT9e1l8EczbogoPpfdZWoJWyrMAu0U/87LtbqOsc14bLZ13NX7j+tgsWZ86dgFrokdS61uIg8v4tHIZdKFytW0kWeYS7g8FH+a8qsySJm5iBvGGoHsR73Jwt1fequj+tVHz4lFHUwi8q4dU0FzoHVkwwYRyncgigtDUJER3Kx9wZ0CiXbozgvkUSxfBjR5Ye+1fy+vVrOQzi1GsCEIUf71i4jyRxLqsBI8nGqxQQZc1gn6u7bgxzPd6qF+Xet7vtF2SUb54DrkiDhELqL51rH3Gs3WeMCCqZDQU69eq05PiiIk7dXNcKLAgMBAAECggEASfsmoNKUjrqqEdlG8+gQtejjRggqPEqNojWzC9TgSm2tNoHNdUN876jimQE+/A7SUeum5JX6ViZfGhiGt6eVSOtQmdBas/f1uP/Id6OnL5uUhZeyoTza8Hewpf3jkZJ8Qh5SrAjadaGQOlK0nvpmJCyraH/It8WtVRuWYfT5XVdIYpgV3BerczIgB9JKeuhuPgpcwNcQ0CmI2g7wEQ7ES+IaZFPuvG6bIAje/kPrK/uoLequO3UpcGY91rSRSN4bVpvEWYQuwdaRPwW4sekNyKvkAo2x65xnuBsy07/OW64m0HeRJa6yBWZsW090/jeP6DwTipad32kyav62ZulA4QKBgQDOUQkd+60TqeoGyrGoP4ha2erRePBijKvJe/Huf1AmOBAZ4MvX0+G/9Anv7LgcBwsbh+sJ4LxhzsqQaKGrmZcdkyXsuEVBLbfGx+3uSqae6oZWrjOhrJg6dgLcvJLsw7pebt4ozJ2mECmrveXGzuGVIteIOhZcnwpHodJbVgPzEQKBgQDF+1SOpByqdWEqOGj/DfwgQMn5m+cCeHWL8f0N2jcsVJxIrU1dXzC2fa+s9IDbRFofeLs/7wcB2KINno6vOEC9TP7MSxRv+txSfQUZxolxsHW3GTFWyCzDDGtf2Aq8WHczTlSgAhAki68gnOocm2K1FJ5qIZ0taY7f6gGRmwGj2wKBgAk8i4HyIH9+3eFL9cQog/w9QUv7dBeVYKN2jxA0Vuw/GkluTPHupG6piEBbgqqOjiq/XQBmNUjTrzHj3UkHaUKDsfD1FvSiDVYy4S4H3YnDyhvbVKhqR65mVh53usQqxw8vO3bsIiqrEpKDv+O0o1i/5JJOt22SGS23yukX4rlhAoGBAJZgz5pE1y02WSZLkJzij3YkIAXDQFVlD8vLc6246R136vldAR2B9ys2DmDtmo5xvY6YEop+UTE6zeRQYgp/TNU8jXC5On3P6teQ9HXeknlTUiZQMWS8SRuh7FDxdT4YZ/oFbkvXJVHM86lu5nfyIqhuT+FHRO7AdfBn+ucQ+M7vAoGBAIoZUbR4RW0QQxQuPu6IHkSo8nM7Vc9DW0Q8yTSjDbz7NincnAW3hcqPpVEoEqJFLv0QtK44lVQbkh2Q6P/XFqbqdnL+rij7/5dQbMuZG4IzCD1dKaf/t94Gi0OWTTqkmFyG1nbBmxEZPry1mcIJc8kUo62GpfyCmGfVZutQIP2W")
                .setChannelName("CRM测试通道1");
        System.out.println(GsonHelper.toJson(config));
    }

}
