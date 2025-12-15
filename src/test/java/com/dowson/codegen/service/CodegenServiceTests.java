package com.dowson.codegen.service;

import com.dowson.codegen.dto.CodegenRequestDto;
import com.dowson.codegen.vo.CodegenResultVo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CodegenServiceTests {
    @Autowired
    CodegenService service;

    @Test
    void runDryRunFallbacksToYml() {
        CodegenRequestDto dto = CodegenRequestDto.builder().dryRun(true).build();
        CodegenResultVo result = service.run(dto);
        Assertions.assertTrue(result.isDryRun());
        Assertions.assertEquals("test-demo", result.getModule());
        Assertions.assertEquals("com.dowson.testdemo", result.getParentPackage());
        Assertions.assertEquals(3, result.getTableCount());
        Assertions.assertTrue(result.getOutputPath().toLowerCase().contains("test-demo"));
    }
}
