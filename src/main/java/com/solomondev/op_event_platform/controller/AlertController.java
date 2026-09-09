package com.solomondev.op_event_platform.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.solomondev.op_event_platform.model.dto.AlertResponseDto;
import com.solomondev.op_event_platform.service.AlertService;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {
    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public ResponseEntity<List<AlertResponseDto>> getAllAlerts(@RequestParam(required = false) String status) {
        List<AlertResponseDto> alerts;
        if (status == null)
            alerts = alertService.getAllAlerts();
        else
            alerts = alertService.getAlertsByStatus(status);

        return new ResponseEntity<>(alerts, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertResponseDto> getAlertById(@PathVariable Long id) {
        AlertResponseDto response = alertService.getAlertById(id);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/{id}/acknowledge")
    public ResponseEntity<AlertResponseDto> acknowledgeAlert(@PathVariable Long id) {
        AlertResponseDto response = alertService.acknowledgeAlert(id);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
