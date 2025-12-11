package com.dowson.codegen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 独立代码生成项目启动类
 * 提供 REST 接口与命令行 Runner 两种触发方式
 */
@SpringBootApplication
public class CodegenApplication {
    /**
     * 应用入口
     */
    public static void main(String[] args) {
        SpringApplication.run(CodegenApplication.class, args);
    }
}
