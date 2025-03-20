# **KTX Core Module**

## **Overview**
The **KTX Core Module** serves as a **set of reusable interfaces** that define the contract for event-driven logging and processing. Instead of containing implementations, it acts as a **shared dependency** that any service can use, ensuring **plug-and-play architecture** across different modules.

This module allows different components to interact via standardized interfaces without needing to know the underlying implementations, making the system **modular, loosely coupled, and easily extensible**.

---

## **Purpose & Benefits**

### **1️⃣ Decoupling Implementations**
- The core module **only contains interfaces** (e.g., `KtxEvent<T>`, `KtxUser`, `KtxEventService<T>`, `KtxEventProducer<T>`).
- **No direct dependency on implementations**, making it easy to replace or extend adapters (e.g., switching from MySQL to MongoDB or Kafka to RabbitMQ).

### **2️⃣ Plug-and-Play Architecture**
- Any module implementing these interfaces can **seamlessly integrate** with others.
- A service consuming `KtxEventService<T>` doesn’t need to know if the implementation uses MySQL, Kafka, or any other system.
- This ensures that any compliant implementation can be injected and work without additional configuration.

### **3️⃣ Reusability Across Multiple Projects**
- Instead of rewriting the same interfaces for different projects, this module serves as a **shared contract**.
- Any microservice or system can **reuse** the core module, making integrations faster and reducing duplication.

---

## **📌 How It Works**
### **1️⃣ Core Module (Interfaces Only)**
The **ktx-core** module defines interfaces for:
- **Events (`KtxEvent<T>`)** → Standard structure for event logging.
- **Users (`KtxUser`)** → Defines user-related operations.
- **Event Services (`KtxEventService<T>`)** → Generic event persistence contract.
- **Event Producers (`KtxEventProducer<T>`)** → Defines how events are published.

### **2️⃣ Implementation Modules (Adapters)**
Other modules (e.g., `ktx-audit-service`) **implement these interfaces**, providing:
- **Database storage (MySQL, MongoDB, etc.)** via `KtxEventService<T>`.
- **Messaging (Kafka, RabbitMQ, etc.)** via `KtxEventProducer<T>`.
- **Web APIs** for logging events.

### **3️⃣ Dependency Injection for Flexibility**
- Services are injected with **only the interface**, allowing easy swapping of implementations.
- Example: A module using `KtxEventService<T>` can switch between a MySQL or MongoDB implementation without any code change.

---

## **Setup & Usage**
### **1️⃣ Add as a Dependency**
To use `ktx-core` in a module, add it as a dependency:
```xml
<dependency>
    <groupId>com.kitano</groupId>
    <artifactId>ktx-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

### **2️⃣ Implement the Interfaces**
Example: Implementing `KtxEventService<T>`
```java
@Service
public class SecurityEventService implements KtxEventService<SecurityEvent> {
    private final SecurityEventRepository repository;

    public SecurityEventService(SecurityEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public SecurityEvent save(SecurityEvent event) {
        return repository.save(event);
    }
}
```

---

## **📌 Summary**
**Why was this repository created?**
- To provide a **standard contract** for event logging across different services.
- To **decouple implementations** from business logic, making it easy to swap or extend.
- To enable a **plug-and-play architecture**, where services can integrate seamlessly without extra configuration.
- To **improve maintainability** by avoiding duplicated logic across multiple projects.

**What problem does it solve?**
- Reduces **tight coupling** between components.
- Ensures that **new implementations** (e.g., switching databases or messaging systems) don’t require core changes.
- Allows **scalability** by enabling different modules to evolve independently.

This module acts as the **foundation** for event-driven services, ensuring a **clean, maintainable, and reusable architecture**.

