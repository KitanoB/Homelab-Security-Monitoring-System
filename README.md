# Homelab Security Monitoring System

<img src="./docs/images/homelab.png" alt="logo" width="200" height="200"/>

## Overview
The **Homelab Security Monitoring System** is a microservice-based project designed to enhance security monitoring in a homelab environment. It consists of:
- **Audit Logging Service**: Logs security events (failed login attempts, successful logins, system modifications, unauthorized access, etc.).
- **User Notification Service**: Sends real-time alerts via email, WebSockets, or webhooks when a security event occurs.
- **Kafka Event Streaming**: Used for asynchronous processing and real-time event distribution.
- **CI/CD & DevOps**: Leveraging GitHub Actions, OpenShift for automated deployment.

This project follows the **Hexagonal Architecture (Ports and Adapters Pattern)** to ensure a clean separation between business logic and external dependencies.

---

## What is Hexagonal Architecture?
**Hexagonal Architecture** (also called **Ports and Adapters**) is a software design pattern that promotes:
- Decoupling between the core business logic and external systems (databases, APIs, messaging systems).
- High testability by allowing easy replacement of external dependencies with mocks.
- Better maintainability and scalability, especially in microservices.

<img src="./docs/images/Hexagonal_Architecture.png" alt="drawing" width="200" height="200"/>


### **Core Principles:**
1. **Domain Layer**: Contains the core business logic and domain models (pure Java objects without dependencies on frameworks).
2. **Application Layer**: Orchestrates use cases and interacts with domain logic.
3. **Ports** (Interfaces): Defines how external systems (databases, message queues, APIs) should interact with the domain.
4. **Adapters** (Implementations): External services such as repositories (database access), messaging (Kafka), and notifications.

---

## **Hexagonal Architecture in Our Microservice Project**
Each microservice in our project will follow this structure:

```
security-audit-service/   
│── src/main/java/com/homelab/security/audit/  
│   ├── domain/                # Core Business Logic (Entities, Services, Use Cases)
│   ├── application/           # Application Logic (Orchestrates Use Cases)
│   ├── infrastructure/        # External Adapters (Database, Kafka, REST Clients)
│   │   ├── repository/        # Implements Database interactions
│   │   ├── messaging/         # Kafka producer/consumer
│   │   ├── web/               # REST Controllers (APIs)
│── tests/                    # Unit and Integration Tests
│── pom.xml                    # Dependencies and Build Configuration
```

### **How It Works in Our System**
1. **Security Events are Triggered** (e.g., failed login attempt, unknown device access).
2. **Audit Service Receives Event**
    - The REST API records the event.
    - The database adapter saves it in **Oracle**.
    - The Kafka producer sends an event to the `security-events` topic.
3. **Notification Service Listens for Events**
    - Kafka consumer receives the event.
    - Notification adapter sends an **email alert** or **WebSocket event**.
4. **CI/CD Pipeline Deploys Services**
    - GitHub Actions runs tests, builds images, and deploys to **OpenShift**.

---

## **Technologies Used**
- **Java 17 + Spring Boot** (Backend services)
- **Kafka** (Event-driven architecture)
- **Oracle Database + JPA** (Persistence layer)
- **GitHub Actions + OpenShift** (CI/CD & Deployment)
- **JUnit** (Testing)
- **WebSockets, Email, Webhooks** (Notifications)

---

## **Next Steps**
- [ ] Set up the **Security Audit Service** with Hexagonal Architecture.
- [ ] Implement **Kafka Producer** to publish security events.
- [ ] Build the **Notification Service** to listen for security alerts.
- [ ] Automate testing and deployment with **GitHub Actions & OpenShift**.

---

This project serves as a **real-world demonstration of security monitoring**, event-driven systems, and **best practices in microservices architecture with Hexagonal design**. 🚀

