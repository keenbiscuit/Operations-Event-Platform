package com.solomondev.op_event_platform.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.solomondev.op_event_platform.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query(value = """
            select e from Event e
            where exists (
                select m.id from OrgMembership m
                where m.org.id = e.asset.org.id
                  and m.user.externalSubject = :subject
            )
              and (:query = '' or
                   lower(e.eventType) like lower(concat('%', :query, '%')) or
                   lower(e.asset.name) like lower(concat('%', :query, '%')))
              and (:alertStatus is null or exists (
                   select ae.id from AlertEvent ae
                   where ae.event.id = e.id and ae.alert.status = :alertStatus
              ))
              and (:severity is null or exists (
                   select ae.id from AlertEvent ae
                   where ae.event.id = e.id and ae.alert.severity = :severity
              ))
              and e.occurredAt >= :fromTime
              and e.occurredAt <= :toTime
            """,
            countQuery = """
            select count(e) from Event e
            where exists (
                select m.id from OrgMembership m
                where m.org.id = e.asset.org.id
                  and m.user.externalSubject = :subject
            )
              and (:query = '' or
                   lower(e.eventType) like lower(concat('%', :query, '%')) or
                   lower(e.asset.name) like lower(concat('%', :query, '%')))
              and (:alertStatus is null or exists (
                   select ae.id from AlertEvent ae
                   where ae.event.id = e.id and ae.alert.status = :alertStatus
              ))
              and (:severity is null or exists (
                   select ae.id from AlertEvent ae
                   where ae.event.id = e.id and ae.alert.severity = :severity
              ))
              and e.occurredAt >= :fromTime
              and e.occurredAt <= :toTime
            """)
    Page<Event> searchAccessible(
            @Param("subject") String subject,
            @Param("query") String query,
            @Param("alertStatus") String alertStatus,
            @Param("severity") String severity,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime,
            Pageable pageable);

    @Query("""
            select e from Event e
            where e.id = :eventId
              and exists (
                select m.id from OrgMembership m
                where m.org.id = e.asset.org.id
                  and m.user.externalSubject = :subject
              )
            """)
    Optional<Event> findAccessibleById(
            @Param("eventId") Long eventId,
            @Param("subject") String subject);

    @Query("""
            select count(e) from Event e
            where exists (
                select m.id from OrgMembership m
                where m.org.id = e.asset.org.id
                  and m.user.externalSubject = :subject
            )
            """)
    long countAccessible(@Param("subject") String subject);
}
