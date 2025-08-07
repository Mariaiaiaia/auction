| Workflow | Status |
|----------|--------|
| Unit Tests | ![Unit Tests](https://github.com/Mariaiaiaia/auction/actions/workflows/unit-tests.yml/badge.svg) |
| Service Tests | ![Service Tests](https://github.com/Mariaiaiaia/auction/actions/workflows/service-tests.yml/badge.svg) |
| API Tests | ![API Tests](https://github.com/Mariaiaiaia/auction/actions/workflows/api-tests.yml/badge.svg) |

[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![WebFlux](https://img.shields.io/badge/WebFlux-reactive-orange.svg)](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)
[![Kafka](https://img.shields.io/badge/Kafka-event--driven-red.svg)](https://kafka.apache.org/)
[![Redis](https://img.shields.io/badge/Redis-cache-critical.svg)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-ready-blue.svg)](https://www.docker.com/)
[![GitHub Actions](https://img.shields.io/github/actions/workflow/status/Mariaiaiaia/auction/build.yml?branch=main)](https://github.com/Mariaiaiaia/auction/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)


---

## 📌 Overview

This project is a **reactive auction system** built with **Spring WebFlux**. It allows users to:

- Create and participate in auctions
- Place bids in real-time
- Receive invitations to join auctions
- Communicate between microservices via **Kafka**
- Store auction-related data in **MongoDB** and cache hot auctions in **Redis**
- Manage user authentication and authorization with **Spring Security and JWT**
- Upload and store item images in **Amazon S3**
- Access services through a secure **API Gateway**
- Trigger notifications (e.g., auction ending) using **Kafka consumers**

The project follows a **microservice architecture** and includes complete **CI/CD with GitHub Actions and Docker Compose**.

---

## 🧱 Tech Stack

| Layer            | Tech Used                                 |
|------------------|--------------------------------------------|
| Language         | Java 17                                    |
| Framework        | Spring Boot 3, Spring WebFlux              |
| Messaging        | Apache Kafka                               |
| Caching          | Redis                                      |
| Security         | Spring Security, JWT                       |
| API Gateway      | Spring Cloud Gateway                       |
| Storage          | MongoDB, Amazon S3                         |
| CI/CD            | GitHub Actions, Docker, Testcontainers     |
| Tests            | Unit tests, Integration tests (Kafka, Redis, PostgreSQL, Docker Compose) |
| Build Tool       | Maven (multi-module)                       |

---

## 🧩 Microservices

- `gateway-service` – entry point with JWT validation
- `auction-service` – manages auction logic
- `bid-service` – handles real-time bids
- `invitation-service` – sends personalized auction invitations
- `item-service` – manages items and their images (stored in S3)
- `notification-service` – notifies users via Kafka
- `user-service` – user management and profile
- `security-service` – authentication and JWT issuance
- `redis-service` – caches auctions nearing end time
- `shared-library` – common DTOs and Kafka config

---


📦 Auction Microservice Project
A reactive, microservice-based online auction platform built with Java, Spring WebFlux, Kafka, Redis, Docker, and deployed via CI/CD on GitHub Actions.
It supports real-time bidding, user invitations, notifications, and auction lifecycle management.

## 🧩 Architecture

This project consists of the following services:

| Service               | Description                                                   |
|------------------------|---------------------------------------------------------------|
| **Gateway Service**     | Central API gateway with JWT authentication                  |
| **Auction Service**     | Auction lifecycle management and current bid tracking         |
| **Bid Service**         | Bid creation and validation                                   |
| **User Service**        | User profile and management                                   |
| **Security Service**    | Handles registration, login, and JWT token issuance          |
| **Notification Service**| Sends real-time notifications via Kafka                      |
| **Invitation Service**  | Sends user invitations for private auctions                  |
| **Item Service**        | Handles items being auctioned                                |
| **Redis Service**       | Caches popular/ending auctions                               |


This project consists of several microservices:

Service	Description
Gateway Service	API gateway that handles routing and JWT authentication
Auction Service	Manages auction lifecycle, current bid tracking
Bid Service	Handles bid creation and validation
User Service	Manages user data and profiles
Security Service	Handles registration, login, JWT token issuing
Notification Service	Sends real-time notifications via Kafka
Invitation Service	Sends invitations to selected users
Item Service	Manages items listed in auctions
Redis Service	Caches popular/active auctions

Communication between services is done via Reactive Kafka and REST.
Images are stored in Amazon S3 or compatible bucket.

⚙️ Technologies Used
Java 17

Spring Boot + Spring WebFlux

Reactive Kafka (spring-kafka + reactor-kafka)

Redis (Reactive)

R2DBC (PostgreSQL)

Amazon S3 (for image storage)

Docker / Docker Compose

GitHub Actions (CI/CD)

Testcontainers, JUnit 5

- Java 17
- Spring Boot 3.2
- Spring WebFlux (Reactive Stack)
- Kafka (with Reactor Kafka)
- Redis (Reactive)
- PostgreSQL (R2DBC)
- Amazon S3 / LocalStack
- Docker & Docker Compose
- GitHub Actions (CI/CD)
- Testcontainers, JUnit 5, Mockito

---


🚀 How to Run
Requires: Docker, JDK 17, Maven

Clone the project:

bash
Copy
Edit
git clone https://github.com/Mariaiaiaia/auction.git
cd auction
Build the project:

bash
Copy
Edit
mvn clean install
Run services using Docker Compose:

bash
Copy
Edit
docker-compose up
Access gateway at:
http://localhost:8080


## 🚀 Getting Started

### Prerequisites

- Java 17+
- Docker & Docker Compose
- Maven

### Run with Docker Compose

```bash
git clone https://github.com/Mariaiaiaia/auction.git
cd auction
docker-compose up --build

📂 Project Structure
auction/
│
├── auction-service/
├── bid-service/
├── user-service/
├── invitation-service/
├── notification-service/
├── item-service/
├── redis-service/
├── security-service/
├── gateway-service/
├── shared-library/
├── docker-compose.yml
├── .github/
│   └── workflows/
│       └── ci.yml

✅ CI/CD
This project uses GitHub Actions for:

Automated builds and tests

Integration testing with Kafka, Redis, PostgreSQL via Testcontainers

✅ CI/CD
This project uses GitHub Actions for:

Building and testing all microservices

Running integration tests (Kafka, Redis, PostgreSQL via Testcontainers)

Docker builds and deployment (in progress)

Note: One job may be under development.

🧪 Tests
Unit Tests

Integration Tests using Testcontainers

Kafka & Redis handler tests

Reactive WebTestClient for API layer

🧪 Testing
Unit tests for business logic

Integration tests (with Kafka, Redis, PostgreSQL)

WebTestClient for REST APIs

Reactive Kafka/Redis event handler tests



🔐 Authentication
Stateless JWT-based authentication

Gateway checks JWT and propagates identity

SecurityService issues and verifies tokens

🔐 Authentication
Stateless JWT-based auth

Gateway verifies and forwards token

Security Service handles login/register/token


📸 Image Storage
Uploaded images are saved to S3-compatible storage (e.g., AWS S3 or LocalStack)

S3 client integrated via Spring Cloud AWS

📦 Image Storage
User-uploaded images are stored in S3-compatible storage (AWS S3 or LocalStack)

Integrated using Spring Cloud AWS (or similar)

💡 Features
Real-time bidding with backpressure handling

Kafka-based event-driven communication

Auction visibility filtering per user

Redis caching for popular/ending auctions

Invitation system via Kafka

Reactive stack end-to-end

💡 Features
Reactive auction and bidding

Kafka-based real-time updates

User-specific auction visibility

Redis caching for trending/ending auctions

Image upload for auction items

End-to-end reactive microservices

👩‍💻 Author
Maria Novitsky
LinkedIn: linkedin.com/in/maria-novitsky
GitHub: @Mariaiaiaia

Looking for a Java backend developer role in Israel 🇮🇱
With 7 years of experience in insurance and deep understanding of business logic




🏦 Auction Microservices Application








📦 Description
This is a full-featured Auction System built using Reactive Microservices Architecture in Java with Spring WebFlux.
Users can register, create auctions, place bids, send invitations, receive notifications, and more.
The system is scalable, resilient, and integrates with Redis, Kafka, PostgreSQL, MongoDB, and S3.

🧱 Tech Stack
Layer	Tech Used
Language	Java 17
Framework	Spring Boot 3, Spring WebFlux
Messaging	Apache Kafka
Caching	Redis
Security	Spring Security, JWT
API Gateway	Spring Cloud Gateway
Storage	MongoDB, Amazon S3, PostgreSQL
CI/CD	GitHub Actions, Docker, Testcontainers
Tests	Unit tests, API tests (Kafka, Redis, PostgreSQL, Docker Compose), Integration tests (Testcontainers)
Build Tool	Maven (multi-module)

🧩 Microservices
gateway-service – API Gateway, JWT auth, routes

security-service – user registration, login, token handling

user-service – manages user profiles and permissions

item-service – items that can be auctioned

auction-service – create/manage auctions, auto-closing

bid-service – bidding logic with Kafka sync

invitation-service – send auction invites

notification-service – Kafka-based notifications

redis-service – auction caching logic

⚙️ How to Run
bash
Copy
Edit
# Clone the repository
git clone https://github.com/Mariaiaiaia/auction.git
cd auction

# Run all services with Docker Compose
docker-compose up --build
Or run services individually from their module folders.

🧪 Tests
The system includes:

Unit tests for services and handlers

API tests using WebTestClient

Integration tests using Testcontainers (PostgreSQL, Redis, Kafka)

Kafka/Redis behavior testing

CI runs tests on GitHub Actions

☁️ AWS & External Integrations
Amazon S3: store uploaded images for items

MongoDB: notification history

PostgreSQL: persistent storage for users, auctions, bids

🚀 CI/CD
Dockerized microservices

GitHub Actions: Build + test on every push to main

Ready for deployment in cloud platforms

📁 Project Structure
sql
Copy
Edit
auction/
│
├── gateway-service/
├── security-service/
├── user-service/
├── auction-service/
├── bid-service/
├── item-service/
├── invitation-service/
├── notification-service/
├── redis-service/
├── docker-compose.yml
└── ...
🧠 Key Features
Reactive non-blocking backend

Microservice communication via Kafka

Secure with Spring Security and JWT

Redis caching for performance

CI/CD pipeline and test automation

📫 Contact
Feel free to reach out if you're interested in the project or want to collaborate!


🔗 Available URLs:

Gateway: http://localhost:8080

Swagger UI:

Auction: http://localhost:8084/swagger-ui.html

Bid: http://localhost:8082/swagger-ui.html

User: http://localhost:8081/swagger-ui.html

Security: http://localhost:8083/swagger-ui.html


