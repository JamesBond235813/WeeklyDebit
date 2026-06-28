package com.jhl.silver.union.biz.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class AreaDataSeedRunner implements ApplicationRunner {
    private static final String MOBILE_PRE_RESOURCE = "data/mobile_pre.csv";
    private static final String ID_CARD_AREA_RESOURCE = "data/id_card_area.sql";
    private static final int BATCH_SIZE = 5000;
    private static final Pattern ID_CARD_INSERT_PATTERN =
            Pattern.compile("VALUES \\('([0-9]{6})', '(.+)'\\);");

    private final JdbcTemplate jdbcTemplate;

    public AreaDataSeedRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        seedMobilePrefixAreaIfIncomplete();
        seedIdCardAreaIfEmpty();
        ensureHubeiDirectAdminCities();
    }

    private void seedMobilePrefixAreaIfIncomplete() throws Exception {
        ClassPathResource resource = new ClassPathResource(MOBILE_PRE_RESOURCE);
        if (!resource.exists()) {
            log.warn("mobile prefix seed file not found: {}", MOBILE_PRE_RESOURCE);
            return;
        }
        int seedRows = countSeedRows(resource);
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM mobile_prefix_area", Long.class);
        if (count != null && count >= seedRows) {
            return;
        }

        String sql = """
                INSERT INTO mobile_prefix_area
                    (id, mobile_pre, province, city, carrier, district_code, post_code)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    province = VALUES(province),
                    city = VALUES(city),
                    carrier = VALUES(carrier),
                    district_code = VALUES(district_code),
                    post_code = VALUES(post_code)
                """;
        List<Object[]> batch = new ArrayList<>(BATCH_SIZE);
        int imported = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> cols = parseCsvLine(line);
                if (cols.size() < 7 || StringUtils.isBlank(cols.get(1))) {
                    continue;
                }
                batch.add(new Object[]{
                        parseLong(cols.get(0)),
                        cols.get(1),
                        cols.get(2),
                        cols.get(3),
                        blankToNull(cols.get(4)),
                        blankToNull(cols.get(5)),
                        blankToNull(cols.get(6))
                });
                if (batch.size() >= BATCH_SIZE) {
                    jdbcTemplate.batchUpdate(sql, batch);
                    imported += batch.size();
                    batch.clear();
                }
            }
        }
        if (!batch.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batch);
            imported += batch.size();
        }
        log.info("seeded mobile prefix area, count={}", imported);
    }

    private int countSeedRows(ClassPathResource resource) throws Exception {
        int rows = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            reader.readLine();
            while (reader.readLine() != null) {
                rows++;
            }
        }
        return rows;
    }

    private void seedIdCardAreaIfEmpty() throws Exception {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM id_card_area", Long.class);
        if (count != null && count > 0) {
            return;
        }

        ClassPathResource resource = new ClassPathResource(ID_CARD_AREA_RESOURCE);
        if (!resource.exists()) {
            log.warn("id card area seed file not found: {}", ID_CARD_AREA_RESOURCE);
            return;
        }

        String sql = """
                INSERT INTO id_card_area (code, name)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE name = VALUES(name)
                """;
        List<Object[]> batch = new ArrayList<>(BATCH_SIZE);
        int imported = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = ID_CARD_INSERT_PATTERN.matcher(line);
                if (!matcher.find()) {
                    continue;
                }
                batch.add(new Object[]{matcher.group(1), matcher.group(2)});
                if (batch.size() >= BATCH_SIZE) {
                    jdbcTemplate.batchUpdate(sql, batch);
                    imported += batch.size();
                    batch.clear();
                }
            }
        }
        if (!batch.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batch);
            imported += batch.size();
        }
        log.info("seeded id card area, count={}", imported);
    }

    private void ensureHubeiDirectAdminCities() {
        Long hubeiId = jdbcTemplate.query("""
                        SELECT id FROM region_area
                        WHERE code = '420000' OR name = '湖北省'
                        ORDER BY CASE WHEN code = '420000' THEN 0 ELSE 1 END
                        LIMIT 1
                        """,
                rs -> rs.next() ? rs.getLong("id") : null);
        if (hubeiId == null) {
            return;
        }

        String sql = """
                INSERT INTO region_area (parent_id, name, level, code)
                VALUES (?, ?, 2, ?)
                ON DUPLICATE KEY UPDATE
                    parent_id = VALUES(parent_id),
                    name = VALUES(name),
                    level = VALUES(level)
                """;
        jdbcTemplate.batchUpdate(sql, List.of(
                new Object[]{hubeiId, "仙桃市", "429004"},
                new Object[]{hubeiId, "潜江市", "429005"},
                new Object[]{hubeiId, "天门市", "429006"},
                new Object[]{hubeiId, "神农架林区", "429021"}
        ));
    }

    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean inQuote = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuote && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cell.append('"');
                    i++;
                } else {
                    inQuote = !inQuote;
                }
            } else if (ch == ',' && !inQuote) {
                result.add(cell.toString());
                cell.setLength(0);
            } else {
                cell.append(ch);
            }
        }
        result.add(cell.toString());
        return result;
    }

    private Long parseLong(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return Long.parseLong(value);
    }

    private String blankToNull(String value) {
        return StringUtils.isBlank(value) ? null : value;
    }
}
