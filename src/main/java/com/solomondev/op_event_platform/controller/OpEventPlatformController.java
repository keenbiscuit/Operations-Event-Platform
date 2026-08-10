package com.solomondev.op_event_platform.controller;



import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/op-event-platform")
public class OpEventPlatformController {
    
    // http://localhost:8080/op-event-platform/health
    @GetMapping("/health")
    public String opEventPlatform() {
        return "Operations Event Platform is Running!";
    }
}
