package com.dowson.codegen.spec;

import lombok.Data;

import java.util.List;

@Data
public class BusinessSpec {
    private String resource;
    private String basePath;
    private String packageName;
    private String tag;
    private String description;
    private String entity;
    private String mapper;
    private Boolean useMpService;
    private List<OperationSpec> operations;
}
