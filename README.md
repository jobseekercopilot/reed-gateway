# Reed Gateway

A Spring Boot microservice that provides a gateway to the Reed.co.uk job search API, enabling seamless integration with external job search services.

## Features

- **Job Search Integration**: Connect to Reed.co.uk API for job listings
- **RESTful API**: Expose standardized endpoints for job search operations
- **External API Abstraction**: Transform Reed API responses into application-specific DTOs
- **Error Handling**: Comprehensive exception handling with structured error responses
- **Configuration Management**: Externalized configuration using Spring Boot properties
- **Actuator Endpoints**: Health checks and monitoring capabilities

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Web** - REST API framework
- **Spring WebFlux** - Reactive programming support
- **Spring Validation** - Request validation
- **Spring Actuator** - Monitoring and health checks
- **Lombok** - Boilerplate reduction
- **Maven** - Build and dependency management

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Reed.co.uk API key

## Project Structure

```
reed-gateway/
├── src/main/java/com/jobseekercopilot/reedgateway/
│   ├── ReedGatewayApplication.java          # Application entry point
│   ├── client/
│   │   └── ReedApiClient.java               # Reed API client implementation
│   ├── config/
│   │   ├── ReedApiProperties.java           # Configuration properties
│   │   └── RestTemplateConfig.java          # HTTP client configuration
│   ├── controller/
│   │   └── ReedSearchController.java        # REST API endpoints
│   ├── exception/
│   │   └── GlobalExceptionHandler.java      # Exception handling
│   └── model/dto/
│       ├── ExternalJob.java                 # External job representation
│       ├── ExternalSalary.java              # Salary information
│       ├── ExternalSearchRequest.java       # Search request DTO
│       ├── ExternalSearchResponse.java      # Search response DTO
│       ├── ReedJobDto.java                  # Reed-specific job DTO
│       ├── ReedSearchResponse.java          # Reed search response
│       └── ErrorResponse.java               # Error response structure
└── src/main/resources/
    └── application.yml                       # Application configuration
```

## Configuration

The application requires the following configuration in `application.yml`:

```yaml
reed:
  api:
    key: ${REED_API_KEY}           # Your Reed.co.uk API key
    base-url: https://api.reed.co.uk
```

### Environment Variables

- `REED_API_KEY` - Your Reed.co.uk API authentication key

## Building the Application

```bash
mvn clean install
```

## Running the Application

```bash
mvn spring-boot:run
```

The application will start on the default port (typically 8080).

## API Endpoints

### Search Jobs

```
GET /api/reed/search
```

Search for jobs using the Reed.co.uk API.

**Query Parameters:**
- `keywords` - Job title or keywords
- `location` - Location (city, region, or postcode)
- `distance` - Search radius in miles (default: 10)
- `minimumSalary` - Minimum salary filter
- `maximumSalary` - Maximum salary filter
- `jobType` - Type of employment (permanent, contract, etc.)
- `page` - Page number for pagination

**Example Request:**
```bash
curl "http://localhost:8080/api/reed/search?keywords=software+engineer&location=London"
```

## Architecture

The application follows a layered architecture:

1. **Controller Layer**: Handles HTTP requests and responses
2. **Client Layer**: Communicates with external Reed API
3. **Model/DTO Layer**: Data transfer objects for request/response mapping
4. **Configuration Layer**: Externalized configuration management
5. **Exception Handling**: Centralized error handling

## Error Handling

The application provides structured error responses:

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/reed/search"
}
```

## Monitoring

Spring Actuator endpoints are available for monitoring:

- `/actuator/health` - Application health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics

## Development

### Running Tests

```bash
mvn test
```

### Building JAR

```bash
mvn clean package
```

The executable JAR will be created in the `target/` directory.

## Dependencies

Key dependencies are managed through Maven:

- Spring Boot Starter Web (REST API)
- Spring Boot Starter WebFlux (Reactive support)
- Spring Boot Starter Validation (Request validation)
- Spring Boot Starter Actuator (Monitoring)
- Lombok (Code generation)

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is part of the Job Seeker Copilot ecosystem.

## Related Projects

- [Job Seeker Copilot](https://github.com/mcgeeverbernard1992/job-seeker-copilot) - Main application

## Support

For issues and feature requests, please use the GitHub issue tracker.