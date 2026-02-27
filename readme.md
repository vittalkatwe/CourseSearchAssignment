# Course Search — Spring Boot + Elasticsearch

A Spring Boot application that indexes course documents into Elasticsearch and exposes a REST API for searching with filters, pagination, sorting, autocomplete suggestions, and fuzzy search.

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Project Structure](#project-structure)
3. [Part 1 – Launch Elasticsearch](#part-1--launch-elasticsearch)
4. [Part 2 – Build and Run the Application](#part-2--build-and-run-the-application)
5. [Part 3 – Verify Data Ingestion](#part-3--verify-data-ingestion)
6. [Part 4 – Search API (Assignment A)](#part-4--search-api-assignment-a)
7. [Part 5 – Autocomplete & Fuzzy Search (Assignment B)](#part-5--autocomplete--fuzzy-search-assignment-b)
8. [Running Integration Tests](#running-integration-tests)
9. [Configuration Reference](#configuration-reference)

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 17+ |
| Maven | 3.8+ |
| Docker Desktop | 20+ |
| Docker Compose | v2+ |

---

## Project Structure

```
course-search/
├── docker-compose.yml
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/example/coursesearch/
    │   │   ├── CourseSearchApplication.java
    │   │   ├── config/
    │   │   │   ├── ElasticsearchConfig.java      # ES client with JavaTimeModule
    │   │   │   └── JacksonConfig.java            # Spring MVC ObjectMapper
    │   │   ├── document/
    │   │   │   └── CourseDocument.java           # ES document entity
    │   │   ├── repository/
    │   │   │   └── CourseRepository.java         # Spring Data ES repository
    │   │   ├── service/
    │   │   │   ├── CourseSearchRequest.java      # Search params DTO
    │   │   │   ├── CourseSearchResponse.java     # Response DTO
    │   │   │   ├── CourseSearchService.java      # Core search + suggest logic
    │   │   │   └── DataIngestionService.java     # Startup bulk indexer
    │   │   └── controller/
    │   │       └── CourseSearchController.java   # REST endpoints
    │   └── resources/
    │       ├── application.yaml
    │       ├── sample-courses.json               # 55 sample courses
    │       └── elasticsearch/
    │           ├── settings.json
    │           └── mappings.json
    └── test/
        └── java/com/example/coursesearch/
            └── CourseSearchIntegrationTest.java
```

---

## Part 1 – Launch Elasticsearch

### Step 1 — Make sure Docker Desktop is running

Open Docker Desktop and wait until the status shows **"Engine running"**.

### Step 2 — Start Elasticsearch

From the project root directory, run:

```bash
docker-compose up -d
```

This starts a single-node Elasticsearch 8.10.4 cluster on `localhost:9200` with security disabled.

### Step 3 — Verify Elasticsearch is running

Wait about 15–20 seconds, then run:

```bash
curl http://localhost:9200
```

Expected response:

```json
{
  "name" : "...",
  "cluster_name" : "docker-cluster",
  "version" : {
    "number" : "8.10.4",
    ...
  },
  "tagline" : "You Know, for Search"
}
```

Check cluster health:

```bash
curl http://localhost:9200/_cluster/health?pretty
```

---

## Part 2 – Build and Run the Application

### Step 1 — Build the project

```bash
mvn clean package -DskipTests
```

### Step 2 — Run the application

```bash
mvn spring-boot:run
```

Or run the JAR directly:

```bash
java -jar target/course-search-0.0.1-SNAPSHOT.jar
```

The application starts on **http://localhost:8080**.

On startup, `DataIngestionService` automatically:
1. Clears any existing data in the `courses` index
2. Reads `sample-courses.json` from the classpath
3. Bulk-indexes all 55 courses into Elasticsearch
4. Populates the `suggest` completion field for autocomplete

---

## Part 3 – Verify Data Ingestion

### Check document count via Elasticsearch directly

```bash
curl http://localhost:9200/courses/_count?pretty
```

Expected response:

```json
{
  "count" : 55,
  "_shards" : {
    "total" : 1,
    "successful" : 1,
    "failed" : 0
  }
}
```

### Check the application logs on startup

You should see these log lines:

```
Cleared existing courses from index.
Starting bulk ingestion of sample course data...
Parsed 55 courses from JSON. Indexing...
Ingestion complete. Total courses now in Elasticsearch: 55
```

### Browse indexed documents directly

```bash
curl "http://localhost:9200/courses/_search?pretty&size=3"
```

---

## Part 4 – Search API (Assignment A)

### Endpoint

```
GET /api/search
```

### Query Parameters

| Parameter   | Type    | Description                                           | Default    |
|-------------|---------|-------------------------------------------------------|------------|
| `q`         | string  | Full-text search on title and description             | —          |
| `minAge`    | integer | Minimum age filter (overlapping range match)          | —          |
| `maxAge`    | integer | Maximum age filter (overlapping range match)          | —          |
| `category`  | string  | Exact match — e.g. `Math`, `Science`, `Art`           | —          |
| `type`      | string  | Exact match — `ONE_TIME`, `COURSE`, or `CLUB`         | —          |
| `minPrice`  | decimal | Minimum price                                         | —          |
| `maxPrice`  | decimal | Maximum price                                         | —          |
| `startDate` | ISO8601 | Only courses with `nextSessionDate` on or after this  | —          |
| `sort`      | string  | `upcoming` (default), `priceAsc`, `priceDesc`         | `upcoming` |
| `page`      | integer | Page number, 0-indexed                                | `0`        |
| `size`      | integer | Results per page                                      | `10`       |

### Response Format

```json
{
  "total": 55,
  "courses": [
    {
      "id": "1",
      "title": "Introduction to Algebra",
      "description": "A foundational course in algebra...",
      "category": "Math",
      "type": "COURSE",
      "gradeRange": "6th–8th",
      "minAge": 11,
      "maxAge": 14,
      "price": 49.99,
      "nextSessionDate": "2025-06-10T15:00:00Z"
    },
    ...
  ]
}
```

---

### Example 1 — Return all courses (default: sorted by soonest upcoming)

```bash
curl "http://localhost:8080/api/search"
```

Expected: `total: 55`, courses ordered by `nextSessionDate` ascending.

---

### Example 2 — Full-text keyword search

```bash
curl "http://localhost:8080/api/search?q=algebra"
```

Expected: Courses with "algebra" in the title or description.

---

### Example 3 — Filter by category

```bash
curl "http://localhost:8080/api/search?category=Science"
```

Expected: Only courses where `category = "Science"`.

---

### Example 4 — Filter by type

```bash
curl "http://localhost:8080/api/search?type=CLUB"
```

Expected: Only courses where `type = "CLUB"`.

---

### Example 5 — Filter by age range

```bash
curl "http://localhost:8080/api/search?minAge=10&maxAge=13"
```

Expected: Courses whose age range overlaps with 10–13.

---

### Example 6 — Filter by price range

```bash
curl "http://localhost:8080/api/search?minPrice=20&maxPrice=60"
```

Expected: Only courses where `price` is between 20 and 60.

---

### Example 7 — Filter by start date

```bash
curl "http://localhost:8080/api/search?startDate=2025-07-01T00:00:00Z"
```

Expected: Only courses with `nextSessionDate` on or after July 1st 2025.

---

### Example 8 — Sort by price ascending

```bash
curl "http://localhost:8080/api/search?sort=priceAsc"
```

Expected: All 55 courses ordered from lowest to highest price.

---

### Example 9 — Sort by price descending

```bash
curl "http://localhost:8080/api/search?sort=priceDesc"
```

Expected: All 55 courses ordered from highest to lowest price.

---

### Example 10 — Combine multiple filters

```bash
curl "http://localhost:8080/api/search?category=Technology&type=COURSE&minAge=8&maxAge=15&minPrice=50&maxPrice=100&sort=priceAsc"
```

Expected: Technology COURSE type, age overlap 8–15, price 50–100, sorted by price ascending.

---

### Example 11 — Pagination (page 2, 5 results per page)

```bash
curl "http://localhost:8080/api/search?page=1&size=5"
```

Expected: Courses 6–10 by nextSessionDate. `total` still shows 55.

---

### Example 12 — Combined keyword + filters + pagination

```bash
curl "http://localhost:8080/api/search?q=science&category=Science&sort=priceAsc&page=0&size=5"
```

---

## Part 5 – Autocomplete & Fuzzy Search (Assignment B)

### Autocomplete Endpoint

```
GET /api/search/suggest?q={partialTitle}
```

Returns up to 10 course title suggestions matching the partial input using Elasticsearch's Completion Suggester.

#### Example — Suggest courses starting with "phy"

```bash
curl "http://localhost:8080/api/search/suggest?q=phy"
```

Expected response:

```json
["Physics for Beginners", "Photography for Kids"]
```

#### Example — Suggest courses starting with "mat"

```bash
curl "http://localhost:8080/api/search/suggest?q=mat"
```

Expected response:

```json
["Math for Everyday Life", "Math Olympiad Training"]
```

#### Example — Suggest courses starting with "rob"

```bash
curl "http://localhost:8080/api/search/suggest?q=rob"
```

Expected response:

```json
["Robotics Club"]
```

#### Example — Suggest courses starting with "in"

```bash
curl "http://localhost:8080/api/search/suggest?q=in"
```

Expected response:

```json
[
  "Introduction to Algebra",
  "Introduction to Biology",
  "Introduction to Chemistry",
  "Introduction to Coding with Scratch",
  "Introduction to Economics",
  "Improv and Theater Games"
]
```

---

### Fuzzy Search (Typo Tolerance)

Fuzzy matching is built into the main `/api/search` endpoint via the `q` parameter. Elasticsearch's `fuzziness: AUTO` tolerates 1–2 character differences depending on word length.

#### Example — "dinors" matches "Dinosaurs 101"

```bash
curl "http://localhost:8080/api/search?q=dinors"
```

Expected response:

```json
{
  "total": 1,
  "courses": [
    {
      "id": "2",
      "title": "Dinosaurs 101",
      "category": "Science",
      "price": 19.99,
      "nextSessionDate": "2025-06-12T10:00:00Z"
    }
  ]
}
```

#### Example — "phisics" matches "Physics for Beginners"

```bash
curl "http://localhost:8080/api/search?q=phisics"
```

Expected: Returns "Physics for Beginners".

#### Example — "algebre" matches "Introduction to Algebra"

```bash
curl "http://localhost:8080/api/search?q=algebre"
```

Expected: Returns "Introduction to Algebra".

#### Example — "roboics" matches "Robotics Club"

```bash
curl "http://localhost:8080/api/search?q=roboics"
```

Expected: Returns "Robotics Club".

---

## Running Integration Tests

Integration tests use **Testcontainers** to spin up a real Elasticsearch container automatically. Docker must be running.

```bash
mvn test
```

### What the tests cover

| Test | Description |
|------|-------------|
| `testFullTextSearch_returnsMatchingCourses` | Keyword "algebra" returns correct course |
| `testCategoryFilter_returnsOnlyMatchingCategory` | `category=Science` returns only Science courses |
| `testTypeFilter_returnsOnlyMatchingType` | `type=COURSE` returns only COURSE type |
| `testPriceRangeFilter` | Price between 10–50 filters correctly |
| `testAgeRangeFilter` | Age overlap 5–10 returns correct courses |
| `testSortByPriceAsc` | Results ordered low to high price |
| `testSortByPriceDesc` | Results ordered high to low price |
| `testDateFilter` | Only courses on or after a given date returned |
| `testPagination` | Page 0 and page 1 return different, non-overlapping results |
| `testFuzzySearch_typoStillMatches` | "dinors" returns "Dinosaurs 101" |

---

## Configuration Reference

### `application.yaml`

```yaml
spring:
  application:
    name: course-search

server:
  port: 8080

logging:
  level:
    com.example.coursesearch: DEBUG
    org.springframework.data.elasticsearch: DEBUG
```

The Elasticsearch host is configured directly in `ElasticsearchConfig.java`:

```java
RestClient.builder(new HttpHost("localhost", 9200, "http")).build()
```

To point at a different host, change the host/port values in `ElasticsearchConfig.java`.

### `docker-compose.yml`

```yaml
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.10.4
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    ports:
      - "9200:9200"
```

Security is disabled for local development convenience.

---

## Quick-Start Checklist

```bash
# 1. Start Docker Desktop

# 2. Start Elasticsearch
docker-compose up -d

# 3. Wait ~20 seconds and verify
curl http://localhost:9200

# 4. Build and run (auto-indexes 55 courses on startup)
mvn spring-boot:run

# 5. Verify 55 courses indexed
curl http://localhost:9200/courses/_count

# 6. Search all courses
curl "http://localhost:8080/api/search"

# 7. Filter by category, sort by price
curl "http://localhost:8080/api/search?category=Science&sort=priceAsc"

# 8. Fuzzy search with typo
curl "http://localhost:8080/api/search?q=dinors"

# 9. Autocomplete
curl "http://localhost:8080/api/search/suggest?q=phy"

# 10. Run integration tests (Docker must be running)
mvn test
```
