package com.synthetic.human_core_starter.infrastructure.aspect;

import com.synthetic.human_core_starter.domain.command.Command;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import jakarta.annotation.PostConstruct;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

@Aspect
public class MetricAspect {
    private final MeterRegistry registry;

    @Autowired
    public MetricAspect(MeterRegistry registry, ExecutorService commonExecutor, ExecutorService criticalExecutor) {
        this.registry = registry;
        ExecutorServiceMetrics.monitor(registry, commonExecutor, "commonExecutorMetric", Tags.empty());
        ExecutorServiceMetrics.monitor(registry, criticalExecutor, "criticalExecutorMetric", Tags.empty());
    }

    @Before("execution(void com.synthetic.human_core_starter.domain.command.CommandDispatcher.dispatch(..)) && args(command)")
    public void countProcessedTasks(Command command) throws Throwable {
        Counter.builder("synthetic.human_core_starter.commands.completed")
                .description("Количество выполненных команд по авторам")
                .tag("author", command.author)
                .register(registry)
                .increment();
    }
}
