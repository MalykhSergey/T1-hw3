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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "audit.mode=kafka",
        "audit.topic=test-audit-topic",
        "management.endpoints.web.exposure.include=metrics"
})
class DispatcherTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Различные приоритеты команд")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testDifferentCommandPriorities() throws Exception {
        Command commonCommand = new Command(
                "Common priority command",
                CommandPriority.COMMON,
                "testAuthor",
                Instant.now()
        );

        Command criticalCommand = new Command(
                "Critical priority command",
                CommandPriority.CRITICAL,
                "testAuthor",
                Instant.now()
        );

        // Обе команды должны быть успешно приняты
        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commonCommand)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criticalCommand)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Переполнение очереди - критические задачи")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testCriticalExecutorOverflow() throws Exception {
        Command criticalCommand1 = new Command(
                "Critical command 1",
                CommandPriority.CRITICAL,
                "testAuthor",
                Instant.now()
        );

        Command criticalCommand2 = new Command(
                "Critical command 2",
                CommandPriority.CRITICAL,
                "testAuthor",
                Instant.now()
        );

        // Первая команда должна быть принята
        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criticalCommand1)))
                .andExpect(status().isOk());

        // Вторая команда должна вызвать ошибку из-за переполнения
        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criticalCommand2)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }

    @Test
    @DisplayName("Переполнение очереди - обычные задачи")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testCommonExecutorOverflow() throws Exception {
        // Заполняем очередь до предела (ArrayBlockingQueue с capacity = 2)
        for (int i = 0; i < 4; i++) { // 2 активных потока + 2 в очереди
            Command command = new Command(
                    "Common command " + i,
                    CommandPriority.COMMON,
                    "testAuthor",
                    Instant.now()
            );

            mockMvc.perform(post("/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isOk());
        }

        // Следующая команда должна вызвать ошибку переполнения
        Command overflowCommand = new Command(
                "Overflow command",
                CommandPriority.COMMON,
                "testAuthor",
                Instant.now()
        );

        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overflowCommand)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }
}