package com.synthetic.human_core_starter.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synthetic.human_core_starter.domain.command.CommandDispatcher;
import com.synthetic.human_core_starter.infrastructure.api.ErrorController;
import com.synthetic.human_core_starter.infrastructure.aspect.AuditAspect;
import com.synthetic.human_core_starter.infrastructure.aspect.AuditProperties;
import com.synthetic.human_core_starter.infrastructure.aspect.MetricAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.*;

@EnableConfigurationProperties(AuditProperties.class)
@Configuration
public class ApplicationConfig {
    @Bean(name = "commonExecutor")
    @ConditionalOnMissingBean(name = "commonExecutor")
    public ExecutorService commonExecutor() {
        return new ThreadPoolExecutor(2, 2, 0L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2));
    }

    @Bean(name = "criticalExecutor")
    @ConditionalOnMissingBean(name = "criticalExecutor")
    public ExecutorService criticalExecutor() {
        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS, new SynchronousQueue<>());
    }

    @Bean
    @ConditionalOnMissingBean
    public Logger logger() {
        return LoggerFactory.getLogger("logger");
    }

    @Bean
    @ConditionalOnMissingBean
    public CommandDispatcher commandDispatcher(@Qualifier("commonExecutor") ExecutorService commonExecutor, @Qualifier("criticalExecutor") ExecutorService criticalExecutor) {
        return new CommandDispatcher(commonExecutor, criticalExecutor);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditAspect auditAspect(AuditProperties auditProperties, ObjectMapper objectMapper, KafkaTemplate<String, String> kafkaTemplate, Logger logger) {
        return new AuditAspect(auditProperties, objectMapper, logger, kafkaTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public MetricAspect metricAspect(MeterRegistry registry, @Qualifier("commonExecutor") ExecutorService commonExecutor, @Qualifier("criticalExecutor") ExecutorService criticalExecutor) {
        return new MetricAspect(registry, commonExecutor, criticalExecutor);
    }

    @Bean
    public ErrorController errorController() {
        return new ErrorController();
    }
}
