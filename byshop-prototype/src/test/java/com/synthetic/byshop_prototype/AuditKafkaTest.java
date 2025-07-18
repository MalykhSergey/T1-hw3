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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"audit.mode=kafka","audit.topic=test-audit"})
class AuditKafkaTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    @DisplayName("Аудит - режим Kafka")
    void testKafkaAuditMode() throws Exception {
        Command command = new Command(
                "Test kafka audit command",
                CommandPriority.COMMON,
                "kafkaAuditAuthor",
                Instant.now()
        );
        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk());
        verify(kafkaTemplate, atLeastOnce()).send(eq("test-audit"), startsWith("{\"method\":"));
    }
}