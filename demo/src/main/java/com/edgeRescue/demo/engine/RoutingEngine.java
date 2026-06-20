package com.edgeRescue.demo.engine;

import com.edgeRescue.demo.model.EmergencyTicket;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

@Component
public class RoutingEngine {

    private final PriorityBlockingQueue<EmergencyTicket> ticketQueue = new PriorityBlockingQueue<>();

    public void addTicket(EmergencyTicket ticket) {
        ticketQueue.add(ticket);
    }

    public List<EmergencyTicket> getAllSortedTickets() {
        List<EmergencyTicket> sortedList = new ArrayList<>();
        ticketQueue.forEach(sortedList::add);
        sortedList.sort(null); 
        return sortedList;
    }

    public boolean resolveTicket(String ticketId) {
        return ticketQueue.removeIf(ticket -> ticket.getId().equals(ticketId));
    }
}