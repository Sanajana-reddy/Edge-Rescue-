package com.edgeRescue.demo.repository;

import com.edgeRescue.demo.model.EmergencyTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<EmergencyTicket, String> {

    @Query("""
        SELECT t FROM EmergencyTicket t 
        WHERE (:priority IS NULL OR t.priority = :priority)
          AND (:category IS NULL OR t.category = :category)
          AND (:search IS NULL OR LOWER(t.rawMessage) LIKE LOWER(CONCAT('%', :search, '%')) 
                               OR LOWER(t.summary) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY 
          CASE t.priority WHEN 'CRITICAL' THEN 3 WHEN 'MEDIUM' THEN 2 WHEN 'LOW' THEN 1 ELSE 0 END DESC, 
          t.timestamp ASC
    """)
    List<EmergencyTicket> findSortedFiltered(
        @Param("priority") String priority, 
        @Param("category") String category, 
        @Param("search") String search
    );
}
