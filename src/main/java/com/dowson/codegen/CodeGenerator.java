package com.dowson.codegen;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;
import com.dowson.codegen.engine.SafeVelocityTemplateEngine;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.dowson.codegen.spec.BusinessSpec;
import com.dowson.codegen.spec.OperationSpec;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

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
                        String url, String username, String password, boolean fileOverride) {
                log.info("开始生成 MyBatis-Plus 标准代码: outputPath={}, package={}, tables={}, fileOverride={}",
                                outputPath, parentPackage, tables, fileOverride);
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
                                        var eb = builder.entityBuilder().enableLombok().enableTableFieldAnnotation()
                                                        .logicDeleteColumnName("deleted").versionColumnName("version")
                                                        .formatFileName("%s");
                                        var cb = builder.controllerBuilder().enableRestStyle()
                                                        .formatFileName("%sController");
                                        var sb = builder.serviceBuilder().formatServiceFileName("%sService")
                                                        .formatServiceImplFileName("%sServiceImpl");
                                        var mb = builder.mapperBuilder().formatMapperFileName("%sMapper")
                                                        .formatXmlFileName("%sMapper");
                                        // 覆盖已存在文件（按策略分别开启）
                                        if (fileOverride) {
                                                eb.enableFileOverride();
                                                cb.enableFileOverride();
                                                sb.enableFileOverride();
                                                mb.enableFileOverride();
                                        }
                                })
                                .templateEngine(new SafeVelocityTemplateEngine())
                                // 执行生成
                                .execute();
                log.info("MyBatis-Plus 标准代码生成完成");
        }

        public static void generateWithSpec(String outputPath, String parentPackage, String tables,
                        String url, String username, String password, List<BusinessSpec> specs, boolean fileOverride) {
                int specCount = (specs == null) ? 0 : specs.size();
                log.info("开始生成（包含业务规范）: outputPath={}, package={}, tables={}, specs={}, fileOverride={}",
                                outputPath, parentPackage, tables, specCount, fileOverride);
                FastAutoGenerator.create(url, username, password)
                                .globalConfig(builder -> builder.author("Dowson")
                                                .outputDir(outputPath + "/src/main/java").disableOpenDir())
                                .packageConfig(builder -> builder.parent(parentPackage)
                                                .entity("entity").mapper("mapper").xml("mapper.xml")
                                                .pathInfo(Collections.singletonMap(OutputFile.xml,
                                                                outputPath + "/src/main/resources/mapper")))
                                .strategyConfig(builder -> {
                                        builder.addInclude(tables.split(",")).addTablePrefix("t_", "sys_");
                                        var eb = builder.entityBuilder().enableLombok().enableTableFieldAnnotation()
                                                        .logicDeleteColumnName("deleted").versionColumnName("version")
                                                        .formatFileName("%s");
                                        var cb = builder.controllerBuilder().formatFileName("%sController");
                                        var mb = builder.mapperBuilder().formatMapperFileName("%sMapper")
                                                        .formatXmlFileName("%sMapper");
                                        var sb = builder.serviceBuilder().formatServiceFileName("%sService")
                                                        .formatServiceImplFileName("%sServiceImpl");
                                        if (fileOverride) {
                                                eb.enableFileOverride();
                                                cb.enableFileOverride();
                                                mb.enableFileOverride();
                                                sb.enableFileOverride();
                                        }
                                })
                                .templateEngine(new SafeVelocityTemplateEngine())
                                .templateConfig(builder -> builder.disable(
                                                com.baomidou.mybatisplus.generator.config.TemplateType.CONTROLLER,
                                                com.baomidou.mybatisplus.generator.config.TemplateType.SERVICE,
                                                com.baomidou.mybatisplus.generator.config.TemplateType.SERVICE_IMPL))
                                .execute();
                if (specs != null && !specs.isEmpty()) {
                        renderBusiness(outputPath, parentPackage, specs);
                } else {
                        log.info("业务规范列表为空，跳过业务层模板渲染");
                }
        }

        private static void renderBusiness(String outputPath, String parentPackage, List<BusinessSpec> specs) {
                try {
                        VelocityEngine ve = new VelocityEngine();
                        ve.setProperty("resource.loaders", "class");
                        ve.setProperty("resource.loader.class.class",
                                        "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
                        ve.init();
                        log.info("开始按业务规范渲染自定义模板, specs={}", specs.size());
                        for (BusinessSpec spec : specs) {
                                Map<String, Object> model = new HashMap<>();
                                model.put("parentPackage", parentPackage);
                                model.put("spec", spec);
                                String baseJava = outputPath + "/src/main/java/" + parentPackage.replace('.', '/');
                                Path controllerFile = Paths.get(baseJava, "controller",
                                                spec.getResource() + "Controller.java");
                                Path serviceFile = Paths.get(baseJava, "service", spec.getResource() + "Service.java");
                                Path serviceImplFile = Paths.get(baseJava, "service", "impl",
                                                spec.getResource() + "ServiceImpl.java");
                                Path dtoDir = Paths.get(baseJava, "dto");
                                Path voDir = Paths.get(baseJava, "vo");
                                Files.createDirectories(controllerFile.getParent());
                                Files.createDirectories(serviceFile.getParent());
                                Files.createDirectories(serviceImplFile.getParent());
                                Files.createDirectories(dtoDir);
                                Files.createDirectories(voDir);
                                log.info("生成业务类: resource={}, controller={}, service={}, serviceImpl={}",
                                                spec.getResource(), controllerFile, serviceFile, serviceImplFile);
                                writeWithVelocity(ve, "templates/business-controller.vm", model, controllerFile);
                                writeWithVelocity(ve, "templates/business-service.vm", model, serviceFile);
                                writeWithVelocity(ve, "templates/business-service-impl.vm", model, serviceImplFile);
                                for (OperationSpec op : spec.getOperations()) {
                                        if (op.getRequestDto() != null && !op.getRequestDto().isBlank()) {
                                                Map<String, Object> dm = new HashMap<>();
                                                dm.put("parentPackage", parentPackage);
                                                dm.put("className", op.getRequestDto());
                                                writeWithVelocity(ve, "templates/business-dto.vm", dm,
                                                                dtoDir.resolve(op.getRequestDto() + ".java"));
                                        }
                                        if (op.getResponseVo() != null && !op.getResponseVo().isBlank()) {
                                                Map<String, Object> vm = new HashMap<>();
                                                vm.put("parentPackage", parentPackage);
                                                vm.put("className", op.getResponseVo());
                                                writeWithVelocity(ve, "templates/business-vo.vm", vm,
                                                                voDir.resolve(op.getResponseVo() + ".java"));
                                        }
                                }
                        }
                } catch (Exception e) {
                        log.error("业务模板生成失败", e);
                }
        }

        private static void writeWithVelocity(VelocityEngine ve, String templatePath, Map<String, Object> model,
                        Path target) throws Exception {
                org.apache.velocity.Template template = ve.getTemplate(templatePath, "UTF-8");
                VelocityContext context = new VelocityContext(model);
                try (java.io.OutputStream os = Files.newOutputStream(target);
                                OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
                        template.merge(context, writer);
                }
        }
}
