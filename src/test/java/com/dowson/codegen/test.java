package com.dowson.codegen;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.fill.Column;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SpringBootTest
public class test {
    @Test
    public void FastGenerator() {
        FastAutoGenerator.create("url", "username", "password")
                .globalConfig(builder ->
                        builder.author("dowson")
                                .enableSwagger()
                                .outputDir("D://")
                )
                .dataSourceConfig(builder -> builder.typeConvertHandler((globalConfig, typeRegistry, metaInfo) -> {
                            int typeCode = metaInfo.getJdbcType().TYPE_CODE;
                            if (typeCode == Types.SMALLINT) {
                                // 自定义类型转换
                                return DbColumnType.INTEGER;
                            }
                            return typeRegistry.getColumnType(metaInfo);
                        })
                )
                .packageConfig(builder -> builder.parent("com.dowson.generator")    //设置父包名
                        .moduleName("system")   //设置父包模块名
                        .pathInfo(Collections.singletonMap(OutputFile.xml, "D://")) // 设置mapperXml生成路径
                )
                .strategyConfig(builder ->
                        builder.addInclude("test")  //设置需要生成的表名
                                .addTablePrefix("t_", "c_")  // 设置过滤表前缀
                )
                .templateEngine(new FreemarkerTemplateEngine()) //使用Freemarker引擎模板，默认的是Velocity引擎模板
                .execute();
    }

    @Test
    public void Interaction() {
        FastAutoGenerator.create("url", "username", "password")
                // 全局配置
                .globalConfig((scanner, builder) -> builder.author(scanner.apply("请输入作者名称？")))
                // 包配置
                .packageConfig((scanner, builder) -> builder.parent(scanner.apply("请输入包名？")))
                // 策略配置
                .strategyConfig((scanner, builder) -> builder.addInclude(getTables(scanner.apply("请输入表名，多个英文逗号分隔？所有输入 all")))
                        .entityBuilder()
                        .enableLombok()
                        .addTableFills(
                                new Column("create_time", FieldFill.INSERT)
                        )
                        .build())
                // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }

    protected static List<String> getTables(String tables) {
        return "all".equals(tables) ? Collections.EMPTY_LIST : Arrays.asList(tables.split(","));
    }
}

