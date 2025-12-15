package com.dowson.codegen.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class CodegenControllerTests {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void configReturnsDefaults() throws Exception {
        String json = mockMvc.perform(get("/codegen/config"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode node = objectMapper.readTree(json);
        Assertions.assertEquals(0, node.get("code").asInt());
        JsonNode data = node.get("data");
        Assertions.assertEquals("test-demo", data.get("module").asText());
        Assertions.assertEquals("com.dowson.testdemo", data.get("package").asText());
        Assertions.assertEquals("author,book,book_category", data.get("tables").asText());
    }

    @Test
    void runDryRunWithDefaults() throws Exception {
        String json = mockMvc.perform(post("/codegen/run")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"dryRun\":true}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode node = objectMapper.readTree(json);
        Assertions.assertEquals(0, node.get("code").asInt());
        JsonNode data = node.get("data");
        Assertions.assertTrue(data.get("dryRun").asBoolean());
        Assertions.assertEquals("test-demo", data.get("module").asText());
        Assertions.assertEquals("com.dowson.testdemo", data.get("parentPackage").asText());
        Assertions.assertEquals(3, data.get("tableCount").asInt());
        Assertions.assertTrue(data.get("outputPath").asText().toLowerCase().contains("test-demo"));
    }
}
