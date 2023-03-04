# Bank Transactions Microservice

## Description

This project is a microservice built using Java and [Spring Boot](https://spring.io/projects/spring-boot). It provides endpoints for creating and searching bank transactions, as well as checking the status of a specific transaction based on business rules.

## In-Memory Repository

This microservice is designed to be self-contained and not rely on any external services. Therefore, I have implemented an in-memory repository to store transaction data. This means that any transactions created during runtime will only exist while the microservice is running, and the data will be lost once the microservice is stopped or restarted.

If a persistent data store is needed, it should be considered to implement a database or integrate with an external service. However, for the purposes of this exercise, the in-memory repository is sufficient.

## How to Run/Test

1. Clone the repository to your local machine
2. Open the project in your preferred IDE.
3. Build the project using `mvn clean install`
4. Run the project using `./mvnw spring-boot:run`
5. The server will start on http://localhost:8080.
6. Test the endpoints using a tool like [Postman](https://www.postman.com/) or by running the unit tests using `mvn test`