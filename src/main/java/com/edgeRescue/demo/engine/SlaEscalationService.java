package com.edgeRescue.demo.engine;

import com.edgeRescue.demo.model.EmergencyTicket;
import com.edgeRescue.demo.repository.TicketRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class SlaEscalationService {

    private final TicketRepository ticketRepository;

    public SlaEscalationService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void escalateSlaTickets() {
        System.out.println("⏳ Asynchronous background SLA monitoring cycle active...");
        Instant cutoff = Instant.now().minusSeconds(300); // 5 minutes

        // Pull MEDIUM tickets (optionally filter by raw message/summary later if needed)
        List<EmergencyTicket> mediumTickets = ticketRepository.findSortedFiltered("MEDIUM", null, null);

        for (EmergencyTicket ticket : mediumTickets) {
            if (ticket.getTimestamp() != null && ticket.getTimestamp().isBefore(cutoff)) {
                ticket.setPriority("CRITICAL");
                ticket.appendToSummary(" [SLA ESCALATED]");
            }
        }

        ticketRepository.saveAll(mediumTickets);
    }
}

