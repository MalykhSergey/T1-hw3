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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class MetricsTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Метрики - проверка занятости исполнителей")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testExecutorMetrics() throws Exception {
        Command command = new Command(
                "Test command for metrics",
                CommandPriority.COMMON,
                "metricsAuthor",
                Instant.now()
        );

        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/metrics/executor.active")
                        .param("tag", "name:commonExecutorMetric"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("executor.active"))
                .andExpect(jsonPath("$.measurements").isArray())
                .andExpect(jsonPath("$.measurements[0].value").value(1));

        mockMvc.perform(get("/actuator/metrics/executor.queued")
                        .param("tag", "name:commonExecutorMetric"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("executor.queued"))
                .andExpect(jsonPath("$.measurements").isArray())
                .andExpect(jsonPath("$.measurements[0].value").value(0));
    }

    @Test
    @DisplayName("Метрики - количество выполненных заданий по авторам")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testCommandCompletionMetrics() throws Exception {
        String author1 = "author1";
        String author2 = "author2";

        Command command1 = new Command("Command from author1", CommandPriority.COMMON, author1, Instant.now());
        Command command2 = new Command("Command from author2", CommandPriority.COMMON, author2, Instant.now());
        Command command3 = new Command("Another command from author1", CommandPriority.COMMON, author1, Instant.now());

        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command2)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command3)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/metrics/synthetic.human_core_starter.commands.completed")
                        .param("tag", "author:" + author1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("synthetic.human_core_starter.commands.completed"))
                .andExpect(jsonPath("$.measurements[0].value").value(2));

        mockMvc.perform(get("/actuator/metrics/synthetic.human_core_starter.commands.completed")
                        .param("tag", "author:" + author2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("synthetic.human_core_starter.commands.completed"))
                .andExpect(jsonPath("$.measurements[0].value").value(1));
    }
}