package com.dowson.codegen.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodegenResultVo {
    @Schema(description = "输出路径")
    private String outputPath;
    @Schema(description = "模块名")
    private String module;
    @Schema(description = "父包名")
    private String parentPackage;
    @Schema(description = "表数量")
    private int tableCount;
    @Schema(description = "耗时毫秒")
    private long timeMs;
    @Schema(description = "是否为干跑")
    private boolean dryRun;
}

