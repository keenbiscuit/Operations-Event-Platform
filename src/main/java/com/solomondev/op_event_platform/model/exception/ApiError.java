package com.solomondev.op_event_platform.model.exception;

public record ApiError (
    int status,
    String error,
    String message
){}
 