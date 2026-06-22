# TODO - Cloud-safe triage fallback

- [ ] Inspect current `TriageAiService.java` implementation.
- [ ] Implement cloud-safe try-catch in `parseEmergency(String rawMessage)`:
  - [ ] Keep existing local Ollama/LLM call in `try` block.
  - [ ] On `catch (Exception e)`, run deterministic keyword rules engine fallback:
    - [ ] Lowercase message.
    - [ ] Detect FLOOD / MEDICAL / RESCUE keywords with specified priorities and categories.
    - [ ] Default to LOW / OTHER with summary snippet prefixed with `[Cloud Engine]`.
  - [ ] Return a clean `TriageResponse` from both paths.
- [ ] Update imports if required.
- [ ] Compile/test the project (`mvn test` or `mvn -q test`).

