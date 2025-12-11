package com.dowson.codegen.service;

import com.dowson.codegen.CodeGenerator;
import com.dowson.codegen.dto.CodegenRequestDto;
import com.dowson.codegen.vo.CodegenResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import com.dowson.codegen.util.DbPreflightChecker;

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

                if (!dryRun) {
                        DbPreflightChecker.assertReachable(url, username, password);
                        DbPreflightChecker.assertTablesExist(url, username, password, effTables);
                        CodeGenerator.generate(outputPath, effParentPackage, effTables, url, username, password);
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
}
