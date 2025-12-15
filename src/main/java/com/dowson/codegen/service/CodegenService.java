package com.dowson.codegen.service;

import com.dowson.codegen.CodeGenerator;
import com.dowson.codegen.dto.CodegenRequestDto;
import com.dowson.codegen.vo.CodegenResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import com.dowson.codegen.util.DbPreflightChecker;
import com.dowson.codegen.spec.BusinessSpec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Slf4j
@Service
public class CodegenService {
        // 数据源默认配置：用于执行生成器连接数据库
        @Value("${spring.datasource.url}")
        private String defaultUrl;
        @Value("${spring.datasource.username}")
        private String defaultUsername;
        @Value("${spring.datasource.password}")
        private String defaultPassword;

        // 绝对输出根路径：从 yml 读取，独立项目要求不依赖 user.dir
        @Value("${codegen.outputRoot:}")
        private String outputRoot;
        @Value("${codegen.module:}")
        private String defaultModule;
        @Value("${codegen.package:}")
        private String defaultParentPackage;
        @Value("${codegen.tables:}")
        private String defaultTables;
        @Value("${codegen.specFile:}")
        private String defaultSpecFile;
        @Value("${codegen.fileOverride:false}")
        private boolean fileOverride;

        /**
         * 执行生成或干跑：
         * - 合并请求体与默认数据源配置
         * - 计算绝对输出路径（outputRoot + module）
         * - 非 dryRun 时调用生成器；dryRun 仅返回预期信息
         */
        public CodegenResultVo run(CodegenRequestDto dto) {
                long start = System.currentTimeMillis();
                String url = dto.getUrl() != null && !dto.getUrl().isBlank() ? dto.getUrl() : defaultUrl;
                String username = dto.getUsername() != null && !dto.getUsername().isBlank() ? dto.getUsername()
                                : defaultUsername;
                String password = dto.getPassword() != null && !dto.getPassword().isBlank() ? dto.getPassword()
                                : defaultPassword;
                boolean dryRun = Boolean.TRUE.equals(dto.getDryRun());

                String effModule = (dto.getModule() != null && !dto.getModule().isBlank()) ? dto.getModule()
                                : defaultModule;
                String effParentPackage = (dto.getParentPackage() != null && !dto.getParentPackage().isBlank())
                                ? dto.getParentPackage()
                                : defaultParentPackage;
                String effTables = (dto.getTables() != null && !dto.getTables().isBlank()) ? dto.getTables()
                                : defaultTables;

                if (effModule == null || effModule.isBlank() || effParentPackage == null || effParentPackage.isBlank()
                                || effTables == null || effTables.isBlank()) {
                        throw new com.dowson.codegen.exception.BusinessException(400,
                                        "缺少必要参数：请在请求或配置文件中提供 module/package/tables 默认值");
                }

                String outputPath = outputRoot != null && !outputRoot.isBlank()
                                ? Paths.get(outputRoot, effModule).toString()
                                : Paths.get(System.getProperty("user.dir"), effModule).toString();

                List<BusinessSpec> specs = resolveSpecs(dto);
                if (specs == null || specs.isEmpty()) {
                        log.info("未加载到任何业务规范（BusinessSpec），只生成 MyBatis-Plus 标准代码");
                } else {
                        log.info("成功加载 {} 个业务规范，将生成自定义业务层代码", specs.size());
                }

                if (!dryRun) {
                        DbPreflightChecker.assertReachable(url, username, password);
                        DbPreflightChecker.assertTablesExist(url, username, password, effTables);
                        if (specs != null && !specs.isEmpty()) {
                                CodeGenerator.generateWithSpec(outputPath, effParentPackage, effTables, url, username,
                                                password, specs, fileOverride);
                        } else {
                                CodeGenerator.generate(outputPath, effParentPackage, effTables, url, username, password,
                                                fileOverride);
                        }
                } else {
                        log.info("dryRun module={}, package={}, tables={}", effModule, effParentPackage, effTables);
                }
                long timeMs = System.currentTimeMillis() - start;
                int tableCount = effTables.split(",").length;
                return CodegenResultVo.builder()
                                .outputPath(outputPath)
                                .module(effModule)
                                .parentPackage(effParentPackage)
                                .tableCount(tableCount)
                                .timeMs(timeMs)
                                .dryRun(dryRun)
                                .build();
        }

        private List<BusinessSpec> resolveSpecs(CodegenRequestDto dto) {
                if (dto.getSpecs() != null && !dto.getSpecs().isEmpty()) {
                        log.info("使用请求体内联业务规范 specs，数量={}", dto.getSpecs().size());
                        return dto.getSpecs();
                }
                String specFile = dto.getSpecFile() != null && !dto.getSpecFile().isBlank() ? dto.getSpecFile()
                                : defaultSpecFile;
                if (specFile == null || specFile.isBlank()) {
                        log.info("未配置 specFile，跳过业务规范解析");
                        return null;
                }
                try {
                        java.nio.file.Path original = Paths.get(specFile);
                        java.nio.file.Path userDir = Paths.get(System.getProperty("user.dir"));
                        java.nio.file.Path path;
                        if (original.isAbsolute()) {
                                path = original;
                                if (!Files.exists(path)) {
                                        log.warn("业务规范文件不存在（绝对路径），跳过：{}", path);
                                        return null;
                                }
                        } else {
                                java.nio.file.Path candidate1 = userDir.resolve(specFile).normalize();
                                java.nio.file.Path parent = userDir.getParent();
                                java.nio.file.Path candidate2 = parent != null ? parent.resolve(specFile).normalize()
                                                : null;
                                if (Files.exists(candidate1)) {
                                        path = candidate1;
                                } else if (candidate2 != null && Files.exists(candidate2)) {
                                        path = candidate2;
                                } else {
                                        log.warn("业务规范文件不存在，已尝试路径1={} 路径2={}，原始specFile={}", candidate1,
                                                        candidate2, specFile);
                                        return null;
                                }
                        }
                        log.info("尝试从业务规范文件加载 specs，路径={}", path);
                        ObjectMapper mapper = new ObjectMapper();
                        List<BusinessSpec> specs = mapper.readValue(Files.readAllBytes(path),
                                        new TypeReference<List<BusinessSpec>>() {
                                        });
                        log.info("业务规范文件解析成功，数量={}", specs.size());
                        return specs;
                } catch (IOException e) {
                        log.error("读取业务规范文件失败，忽略业务层生成: {}", e.getMessage());
                        return null;
                }
        }
}
