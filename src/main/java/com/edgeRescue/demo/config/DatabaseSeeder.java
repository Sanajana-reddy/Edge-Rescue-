package com.edgeRescue.demo.config;

import com.edgeRescue.demo.model.EmergencyTicket;
import com.edgeRescue.demo.model.TriageResponse;
import com.edgeRescue.demo.repository.TicketRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DatabaseSeeder {

    @Bean
    CommandLineRunner seedEmergencyTickets(TicketRepository ticketRepository) {
        return args -> {
            if (ticketRepository.count() > 0) {
                return;
            }

            List<EmergencyTicket> seedTickets = List.of(
                createTicket(
                    "Collapsed highway overpass with multiple trapped civilians.",
                    new TriageResponse("CRITICAL", "RESCUE", "Immediate heavy rescue crew and medical extraction required."),
                    12.9716, 77.5946, "Bangalore"
                ),
                createTicket(
                    "Floodwater is rising rapidly inside the lower level of the apartment block.",
                    new TriageResponse("MEDIUM", "FLOOD", "Deploy flood response and evacuation support for residents."),
                    10.8505, 76.2711, "Kerala"
                ),
                createTicket(
                    "Elderly patient experiencing severe chest pain and shortness of breath.",
                    new TriageResponse("CRITICAL", "MEDICAL", "Rapid medical response with cardiac support needed."),
                    12.9081, 77.6476, "Bangalore"
                ),
                createTicket(
                    "Large delivery truck has jackknifed and blocked the main arterial road.",
                    new TriageResponse("MEDIUM", "BLOCKAGE", "Traffic rescue and clearance crew needed to restore access."),
                    11.0168, 76.9558, "Kerala"
                ),
                createTicket(
                    "Apartment fire reported on the 4th floor with smoke filling the stairwell.",
                    new TriageResponse("CRITICAL", "OTHER", "Fire brigade and evacuation team required immediately."),
                    28.7041, 77.1025, "Global"
                ),
                createTicket(
                    "Construction site crane collapsed onto a delivery vehicle with injured workers.",
                    new TriageResponse("CRITICAL", "RESCUE", "Urgent rescue and structural safety response required."),
                    12.2958, 76.6394, "Bangalore"
                ),
                createTicket(
                    "Multi-vehicle collision on highway with injured passengers and fuel leakage.",
                    new TriageResponse("CRITICAL", "RESCUE", "Hazmat support and medical extraction needed."),
                    10.0238, 76.3082, "Kerala"
                ),
                createTicket(
                    "Strong smell of gas reported in the restaurant district; smoke alarms are active.",
                    new TriageResponse("MEDIUM", "OTHER", "Utility and gas leak response should investigate immediately."),
                    12.9718, 77.6413, "Bangalore"
                ),
                createTicket(
                    "Landslide has trapped multiple vehicles along the mountain pass.",
                    new TriageResponse("MEDIUM", "BLOCKAGE", "Search and rescue with debris clearance required."),
                    9.9312, 76.2673, "Kerala"
                ),
                createTicket(
                    "Runner collapsed from severe dehydration near the park after an endurance event.",
                    new TriageResponse("LOW", "MEDICAL", "Rapid first aid and transport to a medical facility recommended."),
                    12.9719, 77.6412, "Bangalore"
                )
            );

            ticketRepository.saveAll(seedTickets);
            System.out.println("DatabaseSeeder: seeded " + seedTickets.size() + " emergency tickets.");
        };
    }

    private EmergencyTicket createTicket(String rawMessage, TriageResponse triage, double latitude, double longitude, String region) {
        EmergencyTicket ticket = new EmergencyTicket(rawMessage, triage);
        ticket.setLatitude(latitude);
        ticket.setLongitude(longitude);
        ticket.setRegion(region);
        ticket.setStatus("OPEN");
        ticket.setAssignedWorker("");
        return ticket;
    }
}
