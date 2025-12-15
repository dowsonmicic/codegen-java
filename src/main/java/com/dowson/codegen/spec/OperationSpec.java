package com.dowson.codegen.spec;

import lombok.Data;

@Data
public class OperationSpec {
    private String name;
    private String method;
    private String path;
    private String summary;
    private String permission;
    private Boolean transactional;
    private String requestDto;
    private String responseVo;
}
