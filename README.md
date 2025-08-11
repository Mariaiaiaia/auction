[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![WebFlux](https://img.shields.io/badge/WebFlux-reactive-orange.svg)](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)
[![Kafka](https://img.shields.io/badge/Kafka-event--driven-red.svg)](https://kafka.apache.org/)
[![Redis](https://img.shields.io/badge/Redis-cache-critical.svg)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-ready-blue.svg)](https://www.docker.com/)
[![Unit Tests](https://github.com/Mariaiaiaia/auction/actions/workflows/unit-tests.yml/badge.svg)](https://github.com/Mariaiaiaia/auction/actions)
[![Service Tests](https://github.com/Mariaiaiaia/auction/actions/workflows/service-tests.yml/badge.svg)](https://github.com/Mariaiaiaia/auction/actions)
[![API Tests](https://github.com/Mariaiaiaia/auction/actions/workflows/api-tests.yml/badge.svg)](https://github.com/Mariaiaiaia/auction/actions)


# 📦 Auction Microservices Platform  

Welcome to Auction, a reactive, microservices-based platform  where users can sell their items through public or private auctions — just like in real life. The architecture is designed for high performance, fault tolerance, and guaranteed message delivery.
It is built on Spring WebFlux and Apache Kafka, enabling the processing of a high volume of bids while preserving strict ordering.

---
## 🧱 Tech Stack

| Layer            | Tech Used                                                              |
|------------------|------------------------------------------------------------------------|
| Language         | Java 17                                                                |
| Framework        | Spring Boot 3, Spring WebFlux                                          |
| Messaging        | Apache Kafka                                                           |
| Caching          | Redis                                                                  |
| Security         | Spring Security, JWT                                                   |
| API Gateway      | Spring Cloud Gateway                                                   |
| Storage          | MongoDB, Amazon S3, PostgreSQL                                         |
| CI/CD            | GitHub Actions                                                         |
| Tests            | Unit tests, Service tests (Testcontainers), API tests (Docker Compose) |
| Build Tool       | Maven (multi-module)                                                   |

---
## 🧩 Microservices 

#### 🏆 Auction Service 
- **Purpose:**
Manages auctions, processes bids, integrates with other services via Kafka and Redis.
- **Key Features:**
  - **Guaranteed Bid Delivery:** All bids are published to Kafka, ensuring they are processed even if the Auction Service is temporarily down.
  - **Sequential Bid Processing:** Each auction is assigned a single Kafka partition, ensuring all bids are processed in strict chronological order.
  - **Bid Validation:** Every bid is validated (minimum amount, auction status, etc.) before being saved to the database.
  - **Redis Caching:** Stores auctions ending within the next hour in Redis for faster access.
  - **Automatic Auction Management:**  
Every 15 minutes: add auctions ending within the next hour to the cache.  
Every hour: check and automatically close finished auctions.  
Upon auction closure, notifications are sent to participants via Kafka.

#### 🎯 Bid Service
- **Purpose:**
Handles bid creation, validation, and lifecycle management of bids placed by users during auctions.

- **Key Features:**
  - **Bid Creation and Validation:** Validates each incoming bid according to auction rules.
  - **Bid Cleanup on Auction Deletion:** Deletes all bids related to a deleted auction upon receiving a Kafka message from the Auction Service.
  - **Access Control:**  
Only the seller of an auction can view all bids placed on their auction. 

#### 💌 Invitation Service
- **Purpose:**
Manages invitations for private auctions, ensuring that only authorized participants can join and bid.
- **Key Features:**
  - **Seller-Only Invitations:** Only the auction’s seller can send invitations to other users for a private auction.
  - **Kafka-Based Messaging:** Sends invitation messages via Kafka to ensure reliable delivery.
  - **User Verification:** Retrieves invited user details from the User Service via Kafka.
  - **Access Control via Redis:** If the user accepts the invitation, a (AuctionID, UserID) entry is saved in Redis.
This mapping is later used by the Auction Service to check whether a user can view a private auction.
  - **Seamless Integration with Auction Service:**
When a user attempts to view a private auction, the Auction Service queries Redis to verify access rights.
This check ensures that only invited users can see and interact with private auctions.

#### 🖼️ Item Service
- **Purpose:**
Manages items listed in auctions.
- **Key Features:**
  - **Image Storage:** Saves item images to Amazon Cloud Storage.  
  - **Metadata Storage:** Stores image URLs and item metadata in the database.

#### 🔔 Notification Service
- **Purpose:**
Handles storage and delivery of notifications and messages.
- **Key Features:**
  - **Notification Storage:** Stores all notifications in MongoDB.
  - **Auction Deletion Handling:** Listens to Kafka for auction deletion events and deletes all notifications related to the deleted auction.

#### 🔐 Security Service
- **Purpose:**
Manages user authentication and token lifecycle.

- **Key Features:**
  - **Login:** Verifies user credentials with the User Service and generates JWT tokens upon successful login.
  - **Logout:** Invalidates JWT tokens by saving them into Redis to prevent reuse.
  - Handles logout by saving invalidated JWT tokens (retrieved from notification headers) into Redis to prevent reuse.

#### 👤 User Service
- **Purpose:**
Manages user accounts and handles user registration.
- **Key Features:**
  - **Account Management:** Allows users to create and manage their accounts.
  - **User Registration:** Handles user registration workflow including validation.
  - **Data Provisioning:** Provides user data to other services via synchronous or asynchronous communication.

#### 🌐 Gateway Service
- **Purpose:** Acts as the central entry point for all incoming requests, ensuring secure and stateless authentication.
- **Key Features:**
  - **JWT Validation:** Extracts the JWT token from the Authorization header.
  - **Redis Blacklist Check:** Checks if the token exists in the Redis store of invalidated tokens (e.g., after logout).
  - **Secure Forwarding:**  Forwards requests with valid tokens to downstream services.


#### 🧩 Core (Shared Library)
- **Purpose:**
A shared library module that holds common entities and security-related classes used across all microservices.
- **Key Features:**
  - **Common Entities:** Provides common domain entities (e.g., AuctionDTO, UserDTO) shared between services to ensure consistency.
  - **Security Utilities:** Includes security utilities and classes (e.g., JWT handling, authentication filters).


---

## 🚀 Getting Started

#### Prerequisites

- Java 17+
- Docker & Docker Compose
- Maven

#### Run with Docker Compose

```bash
git clone https://github.com/Mariaiaiaia/auction.git
cd auction
docker-compose up --build
```

---

### 👩‍💻 Author
Maria Novitsky  
LinkedIn: https://www.linkedin.com/in/maria-novitsky
