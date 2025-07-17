package com.synthetic.human_core_starter.infrastructure.api;

import java.time.Instant;

class ApiError {
    public final Instant timestamp;
    public final int status;
    public final String error;
    public final String message;
    public final String path;

    public ApiError(int status, String error, String message, String path) {
        this.timestamp = Instant.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

}