package com.dowson.codegen.controller;

import com.dowson.codegen.dto.CodegenRequestDto;
import com.dowson.codegen.service.CodegenService;
import com.dowson.codegen.vo.CodegenResultVo;
import com.dowson.codegen.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/codegen")
@Validated
@RequiredArgsConstructor
@Tag(name = "Codegen", description = "代码生成接口")
@Slf4j
public class CodegenController {
    private final CodegenService codegenService;

    @Value("${codegen.module:}")
    private String module;
    @Value("${codegen.package:}")
    private String parentPackage;
    @Value("${codegen.tables:}")
    private String tables;

    @Operation(summary = "执行代码生成", description = "根据入参触发代码生成，支持干跑；请求体为空时回退到 yml 默认值")
    @PostMapping("/run")
    public Result<CodegenResultVo> run(@RequestBody(required = false) @Valid CodegenRequestDto dto) {
        CodegenRequestDto effective = dto == null ? new CodegenRequestDto() : dto;
        return Result.success(codegenService.run(effective));
    }

    @Operation(summary = "查看当前配置", description = "返回 application 配置中的 codegen 参数")
    @GetMapping("/config")
    public Result<Map<String, String>> config() {
        return Result.success(Map.of(
                "module", module,
                "package", parentPackage,
                "tables", tables));
    }
}
