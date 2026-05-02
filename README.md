# Homework Search Microservice

A Spring Boot microservice for searching and filtering homework in a university public homework library system.

## Features

- **Advanced Search**: Full-text search with Elasticsearch
- **Filtering**: Search by title, author, tags, date range, file size, and more
- **Faceted Search**: Aggregate results by tags and authors
- **Caching**: Redis-based caching for performance optimization
- **Pagination**: Efficient result pagination
- **REST API**: Well-documented RESTful API
- **Performance**: 5-second search results guarantee
- **Scalability**: Designed for 27,000+ users
- **Monitoring**: Metrics and health checks with Micrometer/Prometheus
- **Documentation**: OpenAPI/Swagger documentation

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Java**: 17+
- **Search Engine**: Elasticsearch 8.10
- **Database**: PostgreSQL 15
- **Cache**: Redis 7
- **Build Tool**: Maven 3.9
- **Container**: Docker & Docker Compose

## Project Structure

```
homework-search-service/
├── src/main/
│   ├── java/com/university/homework/
│   │   ├── config/              # Configuration classes
│   │   ├── controller/          # REST controllers
│   │   ├── service/             # Business logic
│   │   ├── repository/          # Data access
│   │   ├── entity/              # JPA entities
│   │   ├── dto/                 # Data transfer objects
│   │   ├── exception/           # Exception handlers
│   │   └── util/                # Utility classes
│   └── resources/
│       └── application*.yml     # Configuration files
├── src/test/
│   └── java/                    # Test classes
├── pom.xml                      # Maven configuration
├── Dockerfile                   # Docker image
└── docker-compose.yml           # Local development setup
```

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 15
- Elasticsearch 8.10
- Redis 7

### Installation

#### Option 1: Using Docker Compose (Recommended)

```bash
# Clone repository
git clone https://github.com/DadasahebMoreYou/homework-search-service.git
cd homework-search-service

# Start all services
docker-compose up -d

# Check service status
docker-compose logs -f homework-search-service

# Access API
curl http://localhost:8080/api/v1/homework/search
```

#### Option 2: Local Development

```bash
# 1. Start dependencies
docker-compose up -d postgres elasticsearch redis

# 2. Build application
mvn clean package

# 3. Run application
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# 4. Access API
curl http://localhost:8080/api/v1/homework/search
```

## API Endpoints

### Search Homework

```bash
GET /api/v1/homework/search?title=Linear&tags=math&sortBy=relevance&page=1&pageSize=20

Query Parameters:
- title: Search term for homework title (optional)
- author: Author name exact match (optional)
- tags: Comma-separated tags, AND logic (optional)
- attachmentName: Attachment file name search (optional)
- dateFrom: Start date YYYY-MM-DD (optional)
- dateTo: End date YYYY-MM-DD (optional)
- fileSizeMin: Minimum file size in bytes (optional)
- fileSizeMax: Maximum file size in bytes (optional)
- createdByRole: Filter by creator role, comma-separated (optional)
- sortBy: Sort order - relevance, date_desc, size_asc (default: relevance)
- page: Page number 1-based (default: 1)
- pageSize: Results per page 1-100 (default: 20)
```

Example Response:
```json
{
  "success": true,
  "total": 150,
  "page": 1,
  "perPage": 20,
  "totalPages": 8,
  "executionTimeMs": 142,
  "results": [
    {
      "id": "hw-001",
      "title": "Linear Algebra Assignment",
      "description": "Solutions to matrix problems",
      "author": {
        "id": "user-123",
        "name": "Dr. Smith",
        "role": "TEACHER"
      },
      "tags": ["math", "linear-algebra"],
      "attachments": [
        {
          "id": "att-001",
          "name": "assignment.pdf",
          "size": 2097152,
          "mimeType": "application/pdf"
        }
      ],
      "createdAt": "2026-04-20T10:30:00Z",
      "viewCount": 342,
      "rating": 4.5,
      "relevanceScore": 15.67
    }
  ],
  "facets": {
    "tags": [
      {"name": "math", "count": 45},
      {"name": "physics", "count": 32}
    ],
    "authors": [
      {"name": "Dr. Smith", "count": 12}
    ]
  },
  "timestamp": "2026-04-29T15:45:00Z"
}
```

### Get Available Tags

```bash
GET /api/v1/homework/tags

Response:
[
  {
    "id": 1,
    "name": "math",
    "description": "Mathematics related content",
    "usageCount": 145
  },
  ...
]
```

### Health Check

```bash
GET /api/v1/homework/health

GET /actuator/health
```

## Configuration

### Environment Variables

```bash
# Database
DB_URL=jdbc:postgresql://postgres:5432/homework
DB_USER=homework_user
DB_PASSWORD=homework_password

# Elasticsearch
ELASTICSEARCH_URL=http://elasticsearch:9200

# Redis
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=

# Application
SPRING_PROFILES_ACTIVE=prod
```

### Application Properties

Edit `application.yml` or environment-specific files:
- `application-dev.yml` - Development
- `application-staging.yml` - Staging
- `application-prod.yml` - Production

## Testing

### Unit Tests

```bash
mvn test
```

### Integration Tests

```bash
mvn verify
```

### With Coverage

```bash
mvn clean verify jacoco:report
open target/site/jacoco/index.html
```

## Monitoring & Metrics

### Prometheus Metrics

```
http://localhost:8080/actuator/prometheus
```

Key Metrics:
- `homework_search_duration` - Search execution time
- `homework_tags_retrieval` - Tag retrieval time
- `http_requests_total` - Total HTTP requests
- `jvm_memory_usage_bytes` - JVM memory usage

### Health Checks

```bash
curl http://localhost:8080/actuator/health
```

## Performance Optimization

### Search Performance

1. **Elasticsearch Indexing**
   - 20 primary shards for horizontal scaling
   - 2 replicas for high availability
   - 1-second refresh interval

2. **Caching Strategy**
   - Search results cached for 60 seconds
   - Tags cached for 24 hours
   - Configurable TTL

3. **Database Optimization**
   - Connection pooling (HikariCP): 20-50 connections
   - Strategic indexing on frequently queried fields
   - Query result caching

### Target SLA

- Search results: < 5 seconds (p99)
- Tag retrieval: < 1 second
- Cache hit rate: > 60%

## Security

- CORS configuration
- Input validation on all endpoints
- Rate limiting ready (implement with Spring Cloud Gateway)
- SQL injection protection via parameterized queries
- XSS protection via output encoding

## Deployment

### Kubernetes (Helm)

```bash
# Build Docker image
docker build -t homework-search-service:1.0.0 .

# Push to registry
docker push your-registry/homework-search-service:1.0.0

# Deploy with Helm (create chart separately)
helm install homework-search ./chart
```

### OpenShift

```bash
# Create deployment
oc new-app java~https://github.com/DadasahebMoreYou/homework-search-service.git

# Configure resources
oc set resources deployment homework-search --limits=memory=1Gi,cpu=500m

# Check status
oc status
```

## Troubleshooting

### Elasticsearch Connection Issues

```bash
docker-compose logs elasticsearch
curl http://localhost:9200
```

### Database Connection Issues

```bash
docker-compose logs postgres
psql -h localhost -U homework_user -d homework
```

### Redis Connection Issues

```bash
docker-compose logs redis
redis-cli -h localhost ping
```

## Maintenance

### Index Management

```bash
# Recreate Elasticsearch index
mvn spring-boot:run -Dspring-boot.run.arguments="--elasticsearch.action=recreate-index"
```

### Backup & Restore

```bash
# PostgreSQL backup
docker exec homework-db pg_dump -U homework_user homework > backup.sql

# PostgreSQL restore
docker exec -i homework-db psql -U homework_user homework < backup.sql
