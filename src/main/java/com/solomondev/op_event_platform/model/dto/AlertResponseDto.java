package com.solomondev.op_event_platform.model.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponseDto {

    private Long id;

    private Long ruleAssignmentId;

    private String status;

    private String severity;

    private Integer notificationCount;

    private LocalDateTime createdAt;

    private LocalDateTime acknowledgedAt;

    private LocalDateTime resolvedAt;

    private LocalDateTime lastNotifiedAt;

}
