package com.solomondev.op_event_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.solomondev.op_event_platform.entity.Asset;

public interface AssestRepository extends JpaRepository <Asset, Long> {
    
}
