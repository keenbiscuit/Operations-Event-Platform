package com.solomondev.op_event_platform.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.solomondev.op_event_platform.entity.Alert;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    @Query(value = """
            select a from Alert a
            where exists (
                select m.id from OrgMembership m
                where m.org.id = a.ruleAssignment.asset.org.id
                  and m.user.externalSubject = :subject
            )
              and (:status is null or a.status = :status)
              and (:severity is null or a.severity = :severity)
              and (:assetId is null or a.ruleAssignment.asset.id = :assetId)
              and (:query = '' or
                   lower(a.ruleAssignment.asset.name) like lower(concat('%', :query, '%')) or
                   exists (
                       select ae.id from AlertEvent ae
                       where ae.alert.id = a.id
                         and lower(ae.event.eventType) like lower(concat('%', :query, '%'))
                   ))
              and a.createdAt >= :fromTime
              and a.createdAt <= :toTime
            """,
            countQuery = """
            select count(a) from Alert a
            where exists (
                select m.id from OrgMembership m
                where m.org.id = a.ruleAssignment.asset.org.id
                  and m.user.externalSubject = :subject
            )
              and (:status is null or a.status = :status)
              and (:severity is null or a.severity = :severity)
              and (:assetId is null or a.ruleAssignment.asset.id = :assetId)
              and (:query = '' or
                   lower(a.ruleAssignment.asset.name) like lower(concat('%', :query, '%')) or
                   exists (
                       select ae.id from AlertEvent ae
                       where ae.alert.id = a.id
                         and lower(ae.event.eventType) like lower(concat('%', :query, '%'))
                   ))
              and a.createdAt >= :fromTime
              and a.createdAt <= :toTime
            """)
    Page<Alert> searchAccessible(
            @Param("subject") String subject,
            @Param("status") String status,
            @Param("severity") String severity,
            @Param("assetId") Long assetId,
            @Param("query") String query,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime,
            Pageable pageable);

    @Query("""
            select a from Alert a
            where a.id = :alertId
              and exists (
                select m.id from OrgMembership m
                where m.org.id = a.ruleAssignment.asset.org.id
                  and m.user.externalSubject = :subject
              )
            """)
    Optional<Alert> findAccessibleById(
            @Param("alertId") Long alertId,
            @Param("subject") String subject);

    @Query("""
            select count(a) from Alert a
            where exists (
                select m.id from OrgMembership m
                where m.org.id = a.ruleAssignment.asset.org.id
                  and m.user.externalSubject = :subject
              )
              and a.status = :status
            """)
    long countAccessibleByStatus(@Param("subject") String subject, @Param("status") String status);

    @Query("""
            select count(a) from Alert a
            where exists (
                select m.id from OrgMembership m
                where m.org.id = a.ruleAssignment.asset.org.id
                  and m.user.externalSubject = :subject
              )
            """)
    long countAccessible(@Param("subject") String subject);
}
