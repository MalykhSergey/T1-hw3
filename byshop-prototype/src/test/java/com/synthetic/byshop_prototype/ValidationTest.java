package com.synthetic.byshop_prototype;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synthetic.human_core_starter.domain.command.Command;
import com.synthetic.human_core_starter.domain.command.CommandPriority;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ValidationTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Валидация команд - успешное выполнение с корректными данными")
    void testValidCommandExecution() throws Exception {
        Command validCommand = new Command(
                "Test command description",
                CommandPriority.COMMON,
                "testAuthor",
                Instant.now()
        );
        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCommand)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Валидация команд - ошибка при превышении максимальной длины описания")
    void testValidationFailureDescriptionTooLong() throws Exception {
        String longDescription = "a".repeat(1001); // Превышает максимум в 1000 символов

        Command invalidCommand = new Command(
                longDescription,
                CommandPriority.COMMON,
                "testAuthor",
                Instant.now()
        );

        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCommand)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").exists());
    }

    @Test
    @DisplayName("Валидация команд - ошибка при превышении максимальной длины имени автора")
    void testValidationFailureAuthorTooLong() throws Exception {
        String longAuthor = "a".repeat(101);

        Command invalidCommand = new Command(
                "Valid description",
                CommandPriority.COMMON,
                longAuthor,
                Instant.now()
        );

        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCommand)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("Валидация команд - ошибка при null значениях")
    void testValidationFailureNullValues() throws Exception {
        String jsonWithNullDescription = """
                {
                    "description": null,
                    "priority": "COMMON",
                    "author": "testAuthor",
                    "time": "2024-01-01T00:00:00Z"
                }
                """;

        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithNullDescription))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("Единый формат ошибок - проверка структуры ответа")
    void testErrorResponseFormat() throws Exception {
        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").isNumber())
                .andExpect(jsonPath("$.error").isString())
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.path").isString());

        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").exists());
    }
}