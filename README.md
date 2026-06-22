# 🚨 Edge-Rescue: Autonomous Local-First Crisis Dispatch Matrix

An intelligent, local-first emergency response system designed to route distress signals, resolve dispatch conflicts, and maintain operational continuity during catastrophic network failures. 

---

## 🌟 Core Architecture & Technical Highlights

* **Local-First Intelligence:** Operates completely offline during communication blackouts by leveraging an on-premise **Llama 3.2 (1B)** LLM instance via **Ollama**.
* **Smart Regional Routing:** Automatically intercepts incoming hardware geolocation coordinates or performs semantic text evaluation to route signals directly to isolated command desks (e.g., `BANGALORE` vs. `KERALA`).
* **Conflict Resolution State Engine:** Implements a strict, atomic ticket lifecycle state machine (`OPEN` ➔ `CLAIMED` ➔ `RESOLVED`) to prevent multiple field responders from addressing the same incident.
* **Asynchronous SLA Escalation:** Runs an independent background daemon thread to actively monitor lower-priority unresolved tickets, automatically upgrading their critical status over time to prevent resource starvation.
* **Tactical Command Dashboard:** Features aggregate metric tracking cards and synthesized HTML5 Web Audio API alerts to provide instant auditory and visual notifications to operators when `CRITICAL` entries hit the grid.

---

## 🛠️ The Technology Stack

| Layer | Component | Description |
| :--- | :--- | :--- |
| **Backend Core** | Spring Boot 3.x | Reactive web endpoints and scheduling architecture |
| **Database** | H2 Database Engine | Persistent, lightweight, file-based relational storage |
| **AI Triage** | Ollama (Llama 3.2: 1B) | Fully offline semantic extraction and triage pipeline |
| **Frontend UI** | HTML5 / JS / Tailwind CSS | Responsive multi-portal dashboard mapping workspace |
| **Automation** | Java `@Scheduled` Threads | Dynamic time-travel validated background SLA workers |

---

## 📂 System Project Structure

```text
Edge-Rescue/
├── src/main/java/com/edgeRescue/demo/
│   ├── controller/      # API Routes & Rate Limiters
│   ├── model/           # Persistent SQL Entity Definitions
│   ├── repository/      # Spring Data JPA H2 Access Layer
│   ├── service/         # Ollama AI Integrations & SLA Workers
│   └── DemoApplication.java
└── src/main/resources/
    ├── application.properties
    └── static/          # Frontend Web Interfaces
        ├── index.html       # Entry Gateway Module
        ├── citizen.html     # SOS Transmission Interface
        └── dashboard.html   # Command Desk Core

Execution & Quickstart
Prerequisites
Ollama App installed and running

Run the model locally in your terminal:

Bash
ollama run llama3.2:1b
Running the Application Natively
Navigate to the root directory and execute the Maven wrapper boot sequence:

Bash
.\mvnw.cmd clean spring-boot:run
Once the compilation logs confirm initialization, open your workspace at:
👉 http://localhost:8082/index.html

