package com.dowson.codegen;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

@Slf4j
public class CodeGenerator {
        /**
         * 使用指定的绝对输出路径执行代码生成
         * 
         * @param outputPath    代码生成输出的绝对路径（模块根目录）
         * @param parentPackage 父包名，例如 com.example.demo
         * @param tables        逗号分隔的表名列表
         * @param url           数据库连接 URL
         * @param username      数据库用户名
         * @param password      数据库密码
         */
        public static void generate(String outputPath, String parentPackage, String tables,
                        String url, String username, String password) {
            log.info("开始生成");
                FastAutoGenerator.create(url, username, password)
                                // 全局配置：作者、Java 源码输出目录、生成后不自动打开文件夹
                                .globalConfig(builder -> builder.author("Dowson")
                                                .outputDir(outputPath + "/src/main/java").disableOpenDir())
                                // 包配置：父包、各层子包名、XML 输出路径
                                .packageConfig(builder -> builder.parent(parentPackage)
                                                .entity("entity").service("service").serviceImpl("service.impl")
                                                .mapper("mapper").xml("mapper.xml").controller("controller")
                                                .pathInfo(Collections.singletonMap(OutputFile.xml,
                                                                outputPath + "/src/main/resources/mapper")))
                                // 策略配置：包含表、移除前缀、启用 Lombok 与字段注解、逻辑删除与版本字段、文件命名格式
                                .strategyConfig(builder -> {
                                        builder.addInclude(tables.split(",")).addTablePrefix("t_", "sys_");
                                        builder.entityBuilder().enableLombok().enableTableFieldAnnotation()
                                                        .logicDeleteColumnName("deleted").versionColumnName("version")
                                                        .formatFileName("%s");
                                        builder.controllerBuilder().enableRestStyle().formatFileName("%sController");
                                        builder.serviceBuilder().formatServiceFileName("%sService")
                                                        .formatServiceImplFileName("%sServiceImpl");
                                        builder.mapperBuilder().formatMapperFileName("%sMapper")
                                                        .formatXmlFileName("%sMapper");
                                })
                                // 模板引擎：Velocity
                                .templateEngine(new VelocityTemplateEngine())
                                // 执行生成
                                .execute();
                log.info("生成完成");
        }
}
