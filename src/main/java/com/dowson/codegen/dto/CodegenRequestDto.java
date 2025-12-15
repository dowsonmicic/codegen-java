package com.dowson.codegen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.dowson.codegen.spec.BusinessSpec;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodegenRequestDto {

    @Size(max = 64, message = "module 长度不能超过64")
    private String module;

    @Size(max = 128, message = "package 长度不能超过128")
    private String parentPackage;

    @Size(max = 512, message = "tables 长度不能超过512")
    private String tables;
    private Boolean dryRun;
    private String url;
    private String username;
    private String password;
    private String specFile;
    private List<BusinessSpec> specs;
}
