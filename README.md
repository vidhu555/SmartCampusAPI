# Smart Campus Sensor & Room Management API

## Overview
A RESTful API built with JAX-RS (Jersey) for managing campus rooms and sensors as part of the 5COSC022W Client-Server Architectures module at the University of Westminster.

Base URL: `http://localhost:8080/SmartCampusAPI/api/v1`

## How to Build and Run

### Requirements
- Java JDK 17
- Apache Maven
- NetBeans IDE
- Apache Tomcat

### Steps
1. Clone this repository
2. Open NetBeans → File → Open Project → select the cloned folder
3. Right-click project → Clean and Build
4. Right-click project → Run
5. API will be available at `http://localhost:8080/SmartCampusAPI/api/v1`

## Sample curl Commands

### 1. Discovery Endpoint
```bash
curl http://localhost:8080/SmartCampusAPI/api/v1/
```

### 2. Create a Room
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"LIB-301","name":"Library Quiet Study","capacity":50}'
```

### 3. Create a Sensor
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"CO2-001","type":"CO2","status":"ACTIVE","currentValue":400,"roomId":"LIB-301"}'
```

### 4. Filter Sensors by Type
```bash
curl http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=CO2
```

### 5. Add a Reading to a Sensor
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/CO2-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":425.5}'
```

### 6. Get Reading History
```bash
curl http://localhost:8080/SmartCampusAPI/api/v1/sensors/CO2-001/readings
```

### 7. Delete a Room (success)
```bash
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/LIB-301
```

### 8. Try deleting room with sensors (409 Conflict)
```bash
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/LIB-301
```

## Report — Question Answers

### Part 1.1 — JAX-RS Lifecycle
By default, JAX-RS creates a new instance of each resource class per request (request-scoped). This means instance variables are not shared between requests. To safely share in-memory data, a Singleton DataStore using ConcurrentHashMap is used to prevent race conditions when multiple requests access data simultaneously.

### Part 1.2 — HATEOAS
HATEOAS embeds navigation links inside responses so clients can discover available actions dynamically without consulting external documentation. This reduces tight coupling — if an endpoint changes, clients following links adapt automatically, whereas clients relying on static docs break.

### Part 2.1 — IDs vs Full Objects
Returning only IDs reduces payload size which is good for bandwidth, but forces clients to make additional requests to retrieve full data. Returning full objects increases payload but eliminates extra round-trips. For dashboards needing full data, returning full objects is better. For large lists where clients need only a few records, returning IDs is more efficient.

### Part 2.2 — Idempotency of DELETE
Yes, DELETE is idempotent in this implementation. The first call removes the room and returns 204 No Content. All subsequent identical calls find the room already gone and also return 204. The server state after multiple calls is identical to after the first call, satisfying the definition of idempotency.

### Part 3.1 — @Consumes mismatch
If a client sends text/plain or application/xml to a method annotated @Consumes(APPLICATION_JSON), JAX-RS automatically returns HTTP 415 Unsupported Media Type without invoking the method at all. The framework content negotiation layer rejects the request before it reaches the business logic.

### Part 3.2 — @QueryParam vs @PathParam
Query parameters (/sensors?type=CO2) are semantically correct for filtering because the resource itself is /sensors and the type is just a filter criterion. Path parameters (/sensors/type/CO2) incorrectly imply that type/CO2 is a distinct resource. Query params also allow combining multiple filters easily such as ?type=CO2&status=ACTIVE.

### Part 4.1 — Sub-Resource Locator Benefits
Delegating nested paths to separate classes keeps each class focused on a single resource following the Single Responsibility Principle. In large APIs, a single monolithic controller becomes unmaintainable. Sub-resource locators allow independent testing, separate concerns, and cleaner code organization.

### Part 5.2 — 422 vs 404
A 404 means the requested URL was not found. A 422 means the request was syntactically valid JSON but semantically unprocessable because the roomId references a room that does not exist. The URL itself was found correctly, but the data inside the request payload was invalid. HTTP 422 more precisely communicates that the content of the request is the problem, not the endpoint.

### Part 5.4 — Stack Trace Security Risks
Exposing stack traces reveals internal class and package names which maps the codebase structure, library versions which allow attackers to find known CVEs, server file paths, and application logic flow. This information enables targeted attacks exploiting known library vulnerabilities or crafting inputs that trigger specific code paths.

### Part 5.5 — Filters vs Manual Logging
Filters implement logging in one place applied automatically to every request and response. Manual Logger.info() calls in every method are error-prone, easy to forget, create code duplication, and make changing logging logic require editing dozens of files. Filters follow the DRY principle and make logging a separate replaceable concern.
