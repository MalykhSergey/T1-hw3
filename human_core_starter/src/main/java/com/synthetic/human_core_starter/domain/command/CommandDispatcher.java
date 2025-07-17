package com.synthetic.human_core_starter.domain.command;

import jakarta.validation.Valid;

import java.util.concurrent.ExecutorService;

public class CommandDispatcher {
    private final ExecutorService commonExecutor;
    private final ExecutorService criticalExecutor;

    public CommandDispatcher(ExecutorService commonExecutor, ExecutorService criticalExecutor) {
        this.commonExecutor = commonExecutor;
        this.criticalExecutor = criticalExecutor;
    }

    public void dispatch(@Valid Command command) {
        // Команды с приоритетом CRITICAL исполняются моментально, команды с
        // приоритетом COMMON добавляются в очередь, которая обрабатывается отдельно
        if (command.priority == CommandPriority.CRITICAL) {
            // CriticalExecutor - однопоточный, с вместимостью один.
            // При получении двух Critical задач, выбросит ошибку.
            // Скорее всего, это и есть ожидаемое поведение 😁.
            criticalExecutor.submit(() -> process(command));
        } else {
            commonExecutor.submit(() -> process(command));
        }
    }

    private void process(Command command) {
        try {
            System.out.println("Start process command: " + command.description);
            Thread.sleep(2000);
            System.out.println("End process command: " + command.description);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
