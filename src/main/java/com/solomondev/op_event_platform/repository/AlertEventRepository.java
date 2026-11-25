package com.solomondev.op_event_platform.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.solomondev.op_event_platform.entity.AlertEvent;

public interface AlertEventRepository extends JpaRepository<AlertEvent, Long> {

    List<AlertEvent> findAllByEvent_IdIn(Collection<Long> eventIds);

    List<AlertEvent> findAllByAlert_IdIn(Collection<Long> alertIds);
}
