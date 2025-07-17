package com.synthetic.human_core_starter.infrastructure.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@Aspect
public class AuditAspect {
    private final ObjectMapper objectMapper;
    private final Logger logger;
    private final AuditProperties props;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public AuditAspect(AuditProperties props, ObjectMapper objectMapper, Logger logger, KafkaTemplate<String, String> kafkaTemplate) {
        this.objectMapper = objectMapper;
        this.logger = logger;
        this.props = props;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Around("@annotation(com.synthetic.human_core_starter.infrastructure.WeylandWatchingYou)")
    public Object audit(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        String method = sig.getDeclaringTypeName() + "." + sig.getName();
        Object[] args = pjp.getArgs();
        Object result = null;
        Throwable error = null;
        try {
            result = pjp.proceed();
            return result;
        } catch (Throwable t) {
            error = t;
            throw t;
        } finally {
            Map<String, Object> record = new LinkedHashMap<>();
            record.put("method", method);
            record.put("args", args);
            record.put("result", result);
            record.put("error", error != null ? error.getMessage() : null);
            String msg;
            try {
                msg = objectMapper.writeValueAsString(record);
            } catch (JsonProcessingException e) {
                msg = record.toString();
                logger.error("Failed to serialize audit record", e);
            }
            if ("kafka".equalsIgnoreCase(props.getMode())) {
                kafkaTemplate.send(props.getTopic(), msg);
            } else {
                logger.info("AUDIT: {}", msg);
            }
        }
    }
}
