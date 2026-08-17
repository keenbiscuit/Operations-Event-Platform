package com.solomondev.op_event_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.solomondev.op_event_platform.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {

}
