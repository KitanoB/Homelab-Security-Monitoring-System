# **Hexagonal Architecture for **

## **Overview**

This module implements **Hexagonal Architecture (Ports and Adapters Pattern)** to ensure **modularity, reusability, and
scalability** in event logging and processing. It separates **business logic** from **infrastructure** so that external
dependencies (MySQL, Kafka, REST API) can be easily replaced or extended.

---

## **Architecture Overview**

```
                         +----------------------+
                         |    External Systems  |
                         |----------------------|
                         |   - REST API         |
                         |   - Database (MySQL) |
                         |   - Kafka Messaging  |
                         +----------------------+
                                  ↑  ↑  ↑  
       +----------------------------------------------------------+
       |                      Infrastructure Layer                |
       |----------------------------------------------------------|
       |  - SecurityEventRepository (JPA)   → MySQL               |
       |  - SecurityEventProducer (Kafka)   → Kafka               |
       |  - SecurityEventController (REST)  → API                 |
       +----------------------------------------------------------+
                                  ↑  ↑  ↑  
       +----------------------------------------------------------+
       |                     Application Layer                    |
       |----------------------------------------------------------|
       |  - SecurityService                                       |
       |    (Handles event processing and interacts with         |
       |     repositories and producers)                         |
       +----------------------------------------------------------+
                                  ↑  ↑  ↑  
       +----------------------------------------------------------+
       |                     Core Domain Layer                    |
       |----------------------------------------------------------|
       |  - KtxEvent<T>         (Generic Event Interface)         |
       |  - KtxUser             (Generic User Interface)          |
       |  - KtxEventService<T>  (Event Service Interface)         |
       |  - KtxEventProducer<T> (Event Producer Interface)        |
       +----------------------------------------------------------+
                                  ↑  ↑  ↑  
                 (Ports - Interfaces decouple domain & adapters)
```

### **Benefits of This Architecture**

- **Reusability**: The domain logic is independent of external dependencies.
- **Scalability**: Easily add new services, databases, or messaging systems.
- **Testability**: Mock adapters for unit testing without external systems.
- **Maintainability**: Clear separation of concerns prevents coupling.

---

## **Components**

### **1️⃣ Core Domain Layer**

- **`KtxEvent<T>`** → Defines a generic event structure.
- **`KtxUser`** → Represents a user interface.
- **`KtxEventService<T>`** → Interface for event operations.
- **`KtxEventProducer<T>`** → Defines event publishing contract.

### **2️⃣ Application Layer**

- **`SecurityService`** → Implements event processing and interacts with repositories and Kafka producers.

### **3️⃣ Infrastructure Layer**

- **`SecurityEventRepository`** → Handles MySQL persistence.
- **`SecurityEventProducer`** → Publishes events to Kafka.
- **`SecurityEventController`** → Exposes REST API endpoints.

### **4️⃣ External Systems**

- **MySQL** → Stores security events.
- **Kafka** → Publishes security event messages.
- **REST API** → Exposes endpoints for event logging and retrieval.

---

## **Setup & Installation**

### **1️⃣ Configure Database (MySQL)**

Ensure you have a running MySQL instance and update **`application.properties`**:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ktx_audit
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
```

### **2️⃣ Run Docker for Kafka**

Use `docker-compose.yml` to spin up Kafka and Zookeeper:

```sh
docker-compose up -d
```

### **3️⃣ Start the Application**

```sh
mvn clean spring-boot:run
```

### **4️⃣ Test API with Postman**

#### **Log a Security Event**

```http
POST /api/events/log
```

##### **JSON Payload**:

```json
{
  "eventType": "AUTH",
  "level": "WARNING",
  "criticality": "REGULAR",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "ipAddress": "127.0.0.1",
  "message": "Failed login attempt"
}
```

#### **Retrieve All Events**

```http
GET /api/events/all
```

---

## **Built With**

- **Spring Boot** (REST API & Service Layer)
- **Spring Data JPA** (Persistence Layer)
- **MySQL** (Database)
- **Kafka** (Messaging)
- **Lombok** (Code Simplification)
- **Docker** (Kafka & Zookeeper Setup)

---

## **Future Improvements**

- **Add support for MongoDB as an alternative storage option**.
- **Enhance logging with distributed tracing (Zipkin, OpenTelemetry)**.
- **Implement event replay for Kafka to handle failure recovery**.

---

## **Contributing**

Feel free to contribute by submitting **pull requests** or opening **issues**.

