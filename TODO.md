 # EdgeRescue Cloud Deployment Verification Tasks

- [ ] Update/confirm `POST /api/tickets/submit` in `TicketController.java` uses robust `Map<String, Object>` extraction, type-coercion, and try/catch returning structured 500s.
- [ ] Update `src/main/resources/static/citizen.html` `sendData(...)` to use host-relative `/api/tickets/submit`, explicit JSON headers, and try/catch/finally with guaranteed button unlock.
- [ ] Build/test: `mvn test` (or `mvn -q test`).
- [ ] Runtime check on Render: submit from citizen portal and confirm button never freezes; verify JSON error body returned on failure.

