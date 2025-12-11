package com.dowson.codegen;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CodegenRunner implements CommandLineRunner {
    @Value("${codegen.enabled:false}")
    private boolean enabled;
    @Value("${codegen.module:}")
    private String module;
    @Value("${codegen.package:}")
    private String parentPackage;
    @Value("${codegen.tables:}")
    private String tables;
    // 绝对输出根路径（从 yml 读取）
    @Value("${codegen.outputRoot:}")
    private String outputRoot;
    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;

    @Override
    /**
     * 应用启动后根据配置自动执行代码生成：
     * - 通过 codegen.enabled 控制是否启用
     * - 校验 module/package/tables 参数完整性
     * - 计算绝对输出路径（优先使用 codegen.outputRoot）并调用生成器
     */
    public void run(String... args) {
        if (!enabled) {
            return;
        }
        if (isBlank(module) || isBlank(parentPackage) || isBlank(tables)) {
            return;
        }
        String outputPath = (outputRoot == null || outputRoot.isBlank())
                ? System.getProperty("user.dir") + "/" + module
                : java.nio.file.Paths.get(outputRoot, module).toString();
        CodeGenerator.generate(outputPath, parentPackage, tables, url, username, password);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
