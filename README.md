# ShopApp Backend

Backend for ShopApp — a Spring Boot REST + event-driven service that uses Apache Kafka for asynchronous order processing.

---

## Quick snapshot (recommended versions)

- Java: 21 (LTS)
- Spring Boot: 3.3.x
- Spring Kafka: 3.2.x
- Kafka broker (dev stack): 3.7.1
- MySQL: 8.2.x
- Redis: 7.2.x
- Build: Maven (Maven Wrapper included)
- Container: Docker, Docker Compose

---

## What this repo contains

src/main/java/com/project/shopapp
├── config/          # Configurations (Kafka, Async, DB, Redis)
├── controllers/     # REST HTTP endpoints
├── services/        # Business logic, Producers, Consumers
├── repositories/    # JPA repositories
└── models/          # Entities & DTOs
resources/
├── application.yml  # Main runtime configuration
docker-compose.yml   # Integration stack (MySQL, Redis, Kafka, Zookeeper)
mvnw / mvnw.cmd      # Maven Wrapper

---

## Architecture 

┌─────────────────────────────────────────────────────────────────┐
│                    ShopApp Backend (Mono)                       │
├─────────────────┬─────────────────┬─────────────────────────────┤
│   REST Layer    │  Event Layer    │        Data Layer           │
│                 │                 │                             │
│  ┌───────────┐  │  ┌───────────┐  │  ┌──────────┐  ┌──────────┐ │
│  │Controller │  │  │Kafka      │  │  │ MySQL    │  │ Redis    │ │
│  │           │──┼─▶│Producer   │  │  │ Database │  │ Cache    │ │
│  └───────────┘  │  └───────────┘  │  │          │  │(Product  │ │
│        │        │        │        │  │(Orders,  │  │ Search)  │ │
│  ┌───────────┐  │  ┌───────────┐  │  │ Products)│  └──────────┘ │
│  │Controller │  │  │Kafka      │  │  └──────────┘       ▲       │
│  │(Consumer) │◀─┼──│Consumer   │  │        │            │       │
│  └───────────┘  │  └───────────┘  │        ▼            │       │
│                 │        │        │  ┌───────────┐      │       │
│                 │  ┌───────────┐  │  │Order      │──────┘       │
│                 │  │Order      │  │  │Processor  │              │
│                 │  │Processors │──┼─▶│Services   │              │
│                 │  └───────────┘  │  └───────────┘              │
└─────────────────┴─────────────────┴─────────────────────────────┘
         │                   │                       │
         │ (HTTP)            │ (Kafka Events)        │ (DB Queries)
         ▼                   ▼                       ▼
┌─────────────┐      ┌─────────────┐         ┌─────────────┐
│   Client    │      │   Kafka     │         │  External   │
│   (UI/App)  │      │   Topics    │         │  Services   │
└─────────────┘      └─────────────┘         └─────────────┘

---

## Running locally (no Docker)

Prereqs: Java 21 installed.

From project root:

Windows PowerShell:
```powershell
cd 'C:\Users\TIN\SpringBoot\shopapp_mono\shopapp_backend_java'
.\mvnw.cmd -DskipTests package
.\mvnw.cmd spring-boot:run
```

Set env vars if needed:
```powershell
$env:SPRING_DATASOURCE_URL = 'jdbc:mysql://localhost:3306/shopapp'
$env:KAFKA_BOOTSTRAP_SERVERS = 'localhost:9092'
```

If Kafka/MySQL/Redis are not running locally, either run docker-compose stack or configure tests to use embedded services.

---

## Running with Docker Compose (recommended for integration)

From backend folder (where `docker-compose.yml` is located):

```powershell
cd 'C:\Users\TIN\SpringBoot\shopapp_mono\shopapp_backend_java'
docker-compose down
docker-compose up -d --build
docker-compose logs -f app
```

Important Kafka networking note:
- If app runs in Docker Compose, use the broker service name (e.g. `broker:29092`) as bootstrap server.
- If app runs on host, use `localhost:9092`. Broker must advertise a host-reachable address for each client type.
- Recommended broker config: multi-listener with INTERNAL and PLAINTEXT and correct `KAFKA_ADVERTISED_LISTENERS`.

---

## Configuration snippets (important)

application.yml (examples):
```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      group-id: order-processor
      properties:
        spring.json.trusted.packages: "*"
        spring.json.use.type.headers: false
        spring.json.type.mapping: "orderDTO:com.project.shopapp.DTO.OrderDTO"
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/shopapp}
    username: ${SPRING_DATASOURCE_USERNAME:root}
    password: ${SPRING_DATASOURCE_PASSWORD:password}
```

## Operational commands

- Build:
```powershell
.\mvnw.cmd -DskipTests package
```
- Run app (maven):
```powershell
.\mvnw.cmd spring-boot:run
```
- Docker Compose:
```powershell
docker-compose up -d --build
docker-compose logs -f app
docker-compose down
```
- Check Kafka topics:
```bash
kafka-topics --bootstrap-server localhost:9092 --describe --topic order-requests
```

---
