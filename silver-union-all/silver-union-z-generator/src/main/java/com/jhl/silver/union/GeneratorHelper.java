package com.jhl.silver.union;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.po.TableField;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.config.rules.IColumnType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import com.baomidou.mybatisplus.generator.type.ITypeConvertHandler;
import com.baomidou.mybatisplus.generator.type.TypeRegistry;
import com.google.common.collect.Maps;

import org.apache.ibatis.type.JdbcType;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author: qingren
 * @create_time: 2025/3/16
 */
public class GeneratorHelper {
    /**
     * 作者姓名
     */
    private static final String author = "Way";
    private static final String MAVEN_ROOT_PATH = File.separator + "src" + File.separator + "main";
    private static final String MAVEN_SRC_PATH = MAVEN_ROOT_PATH + File.separator + "java";
    private static final String MAVEN_RESOURCE_PATH = MAVEN_ROOT_PATH + File.separator + "resources";

    public static void generateDalInfo(List<String> tableList, String targetModuleName, String basePackage) {
        if (CollectionUtils.isEmpty(tableList)) {
            throw new RuntimeException("请填写需要生成代码的表名");
        }
        String projectPath = System.getProperty("user.dir");
        List<File> bizDirectory = Arrays.stream(Objects.requireNonNull(new File(projectPath).listFiles()))
                .filter(e -> e.isDirectory() && e.getName().endsWith(targetModuleName))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(bizDirectory) || bizDirectory.size() != 1) {
            throw new RuntimeException("请确认需要生成代码的位置");
        }
        String rootPath = bizDirectory.get(0).getAbsolutePath();
        String srcRootPath = rootPath + MAVEN_SRC_PATH;
        String mapperPath = rootPath + MAVEN_RESOURCE_PATH + File.separator + "mapper";

        String dbUrl = "jdbc:mysql://" + GenConst.DB_HOST + ":" + GenConst.DB_PORT + "/" + GenConst.DB_NAME
                + "?useUnicode=true&characterEncoding=UTF-8";
        FastAutoGenerator fastAutoGenerator = FastAutoGenerator.create(
                new DataSourceConfig.Builder(dbUrl, GenConst.USER_NAME, GenConst.PASSWORD)
                        .typeConvertHandler(new ITypeConvertHandler() {
                            // private static TypeRegistry typeRegistry;
                            @Override
                            public IColumnType convert(
                                    GlobalConfig globalConfig, TypeRegistry typeRegistry,
                                    TableField.MetaInfo metaInfo) {
                                if (Objects.isNull(typeRegistry)) {
                                    typeRegistry = new TypeRegistry(globalConfig);
                                }
                                JdbcType jdbcType = metaInfo.getJdbcType();
                                if (Objects.equals(JdbcType.TINYINT, jdbcType)) {
                                    return DbColumnType.INTEGER;
                                }
                                return typeRegistry.getColumnType(metaInfo);
                            }
                        })
        // .typeConvert(new TinyIntJavaTypeResolver())
        )
                .globalConfig(builder -> {
                    builder.author(author) // 设置作者
                            // 【重要！！】 不覆盖已有的文件， 防止service层被破坏， 若需重新生成目标文件， 请先将原文件删除再执行本代码。
                            // .fileOverride()
                            .dateType(DateType.ONLY_DATE) // datetime 类型映射成Date类型
                            .commentDate("yyyy-MM-dd HH:mm:ss")
                            .outputDir(srcRootPath)
                            .enableSpringdoc()
                            .disableOpenDir();
                })
                .packageConfig(builder -> {
                    Map<OutputFile, String> map = Maps.newHashMap();
                    map.put(OutputFile.xml, mapperPath);// 生成mapper.xml
                    builder.parent(basePackage) // 设置父包名
                            .service("manager")
                            .serviceImpl("manager.impl")
                            .mapper("dal.mapper")
                            .entity("dal.entity")
                            .controller("controller")
                            .pathInfo(map); // 设置生成路径

                })
                .strategyConfig(builder -> {
                    builder
                            // 设置需要生成的表名
                            .addInclude(tableList);
                    builder.entityBuilder().enableColumnConstant().enableLombok()
                            .enableChainModel()
                            .formatFileName("%sDO")
                            .versionPropertyName("version")
                            .fieldUseJavaDoc(true)

                ;

                    builder.serviceBuilder().formatServiceFileName("%sManager")
                            .formatServiceImplFileName("%sManagerImpl");
                    // 不生成controller
                    builder.controllerBuilder().disable();
                })
                .templateEngine(new FreemarkerTemplateEngine());

        fastAutoGenerator.execute();
    }
}
