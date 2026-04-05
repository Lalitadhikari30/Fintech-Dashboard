# Finance Data Processing and Access Control Backend

A production-grade backend system for a **Finance Dashboard** built with Java Spring Boot. The system supports financial record management, user role-based access control, and dashboard analytics.

## Tech Stack

| Technology | Purpose |
|---|---|
| **Java 17** | Core language |
| **Spring Boot 3.2.5** | Application framework |
| **Spring Security** | Authentication & authorization |
| **Spring Data JPA** | Data access layer |
| **JWT (jjwt 0.12.5)** | Stateless authentication tokens |
| **MySQL 8+** | Primary Database |
| **Swagger/OpenAPI 3** | API documentation |
| **Lombok** | Boilerplate reduction |
| **BCrypt** | Password hashing |

## Architecture

The project follows a **layered architecture** pattern:

```
Controller → Service → Repository → Database
```

```
src/main/java/com/fintech/dashboard/
├── config/           # Security, Swagger, DataSeeder configs
├── controller/       # REST API endpoints (4 controllers)
├── dto/
│   ├── request/      # Input validation DTOs (6 files)
│   └── response/     # Output DTOs (7 files)
├── exception/        # Custom exceptions + global handler
├── model/
│   └── enums/        # Role, RecordType, UserStatus enums
├── repository/       # Spring Data JPA repositories
├── security/         # JWT filter, token provider, UserDetailsService
└── service/          # Business logic layer (4 services)
```

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- (Optional) MySQL 8+ for production profile

### Database Setup

Ensure MySQL is running on `localhost:3306`. Create the database credentials as configured in `application.yml` (default `root` / `Lalit9968@`). Spring Boot will automatically create the `fintech_dashboard` database and tables upon startup.

### Run Application
```bash
cd backend
mvn spring-boot:run
```

The application starts at `http://localhost:8080`.

### Default Admin Credentials
On first startup, a default admin user is seeded:
- **Email:** `admin@fintech.com`
- **Password:** `admin123`

## API Documentation

Interactive Swagger UI is available at: `http://localhost:8080/swagger-ui.html`

### Authentication Endpoints
| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/api/v1/auth/register` | Register a new user | Public |
| POST | `/api/v1/auth/login` | Login and get JWT token | Public |

### User Management Endpoints (ADMIN Only)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/users` | List all users (paginated) |
| GET | `/api/v1/users/{id}` | Get user by ID |
| PATCH | `/api/v1/users/{id}/role` | Update user role |
| PATCH | `/api/v1/users/{id}/status` | Activate/deactivate user |

### Financial Records Endpoints
| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/api/v1/records` | Create a record | ADMIN |
| GET | `/api/v1/records` | List/filter records (paginated) | All authenticated |
| GET | `/api/v1/records/{id}` | Get record by ID | All authenticated |
| PUT | `/api/v1/records/{id}` | Update a record | ADMIN |
| DELETE | `/api/v1/records/{id}` | Soft-delete a record | ADMIN |

**Filter Parameters:** `type`, `category`, `userId`, `startDate`, `endDate`

Example: `GET /api/v1/records?type=INCOME&category=Salary&startDate=2024-01-01&endDate=2024-12-31`

### Dashboard Analytics Endpoints (All Authenticated Users)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/dashboard/summary` | Full summary (income, expenses, net balance, category breakdown, recent activity) |
| GET | `/api/v1/dashboard/category-breakdown` | Category-wise totals |
| GET | `/api/v1/dashboard/recent` | Last 5 transactions |
| GET | `/api/v1/dashboard/monthly-summary` | Monthly income vs expense trends |

## Role-Based Access Control

| Role | Records (Read) | Records (Write) | Dashboard | User Management |
|---|---|---|---|---|
| **VIEWER** | ✅ | ❌ | ✅ | ❌ |
| **ANALYST** | ✅ | ❌ | ✅ | ❌ |
| **ADMIN** | ✅ | ✅ | ✅ | ✅ |

Access control is enforced at **two layers**:
1. **URL-level:** Spring Security `SecurityFilterChain` with `requestMatchers`
2. **Method-level:** `@PreAuthorize` annotations on controller methods

## Key Design Decisions

1. **JWT Stateless Auth:** No server-side sessions. The token is self-contained with user email and role.
2. **Soft Delete for Records:** Financial records are never physically deleted — they are marked `deleted=true`. This preserves audit trails, which is critical in fintech.
3. **BigDecimal for Amounts:** Avoids floating-point precision errors — essential for financial calculations.
4. **JPQL for Aggregation:** Dashboard analytics queries run in the database, not in Java memory, ensuring scalability.
5. **Consistent Error Responses:** All errors return a structured `ErrorResponse` body with timestamp, status, message, and field-level details for validation errors.
6. **DTO Pattern:** Entities are never exposed directly to clients. Response DTOs filter out sensitive data (e.g., passwords).
7. **Pagination:** All list endpoints return paginated results to avoid loading entire datasets.

## Data Model

### User
| Field | Type | Constraints |
|---|---|---|
| id | Long | Auto-generated |
| name | String | Required, max 100 chars |
| email | String | Required, unique |
| password | String | BCrypt hashed |
| role | Enum | VIEWER, ANALYST, ADMIN |
| status | Enum | ACTIVE, INACTIVE |
| createdAt | DateTime | Auto-set |
| updatedAt | DateTime | Auto-updated |

### Financial Record
| Field | Type | Constraints |
|---|---|---|
| id | Long | Auto-generated |
| amount | BigDecimal | Required, > 0, precision(19,4) |
| type | Enum | INCOME, EXPENSE |
| category | String | Required, max 100 chars |
| recordDate | LocalDate | Required |
| description | String | Optional, max 500 chars |
| deleted | Boolean | Default false (soft delete) |
| createdBy | User (FK) | Set from authenticated user |
| createdAt | DateTime | Auto-set |
| updatedAt | DateTime | Auto-updated |

## Validation Rules

- **Registration:** Name (2-100 chars), valid email, password (6-100 chars)
- **Financial Record:** Positive amount (> 0), valid type enum, non-blank category, required date
- **Role Update:** Must be a valid Role enum value
- **Status Update:** Must be a valid UserStatus enum value

## Error Handling

All errors follow a consistent structure:
```json
{
  "timestamp": "2024-01-01 12:00:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Input validation failed. Check 'details' for specifics.",
  "path": "/api/v1/records",
  "details": ["amount: Amount must be greater than 0"]
}
```

| Scenario | HTTP Status |
|---|---|
| Resource not found | 404 |
| Duplicate email | 409 |
| Invalid credentials | 401 |
| Insufficient permissions | 403 |
| Validation failure | 400 |
| Server error | 500 |
