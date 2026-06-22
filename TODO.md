# TODO - dashboard.html SLA timer integration

- [x] Update dashboard.html: add SLA timer inline element per incident card (claimed vs unclaimed states)
- [x] Add CSS keyframes `flashRed` and `.sla-breach` behavior
- [x] Add JS `ticketTimestamps` dictionary + `data-start` attribute on timer DOM
- [x] Add high-frequency (1000ms) interval loop to compute/format elapsed time and update UI
- [x] Trigger `.sla-breach` when elapsed >= 180s for unclaimed incidents
- [ ] Ensure existing endpoints/claim/resolve logic and analytics/queue behavior remain intact
- [ ] Sanity check by running app + verifying timer updates offline and on Render



