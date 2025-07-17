package com.synthetic.human_core_starter.domain.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
public class Command {
    @NotNull
    @Size(max = 1000)
    public final String description;

    @NotNull
    public final CommandPriority priority;

    @NotNull
    @Size(max = 100)
    public final String author;

    @NotNull
    public final Instant time;

    public Command(String description, CommandPriority priority, String author, Instant time) {
        this.description = description;
        this.priority = priority;
        this.author = author;
        this.time = time;
    }
}
