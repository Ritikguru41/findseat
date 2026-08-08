# ☕ FindSeat Backend (Spring Boot 3.2 + MySQL)

The backend service for **FindSeat** built with Java 17, Spring Boot 3.2.5, Spring Data JPA, MySQL 8, JJWT, Razorpay Java SDK, and OpenPDF.

Repository Reference: [https://github.com/Ritikguru41/findseat](https://github.com/Ritikguru41/findseat)

---

## 🏗 Architecture & Package Structure

```
com.findseat
├── FindSeatApplication.java  # Main Spring Boot Entry Point
├── config/
│   ├── AppConfig.java         # RestTemplate & PasswordEncoder Beans
│   └── WebConfig.java         # CORS Configuration & AuthInterceptor Registration
├── controller/
│   ├── AdminController.java   # Admin stats, user management & delete operations
│   ├── AuthController.java    # Register, OTP verification, Login, Profile
│   ├── BookingController.java # Booking creation, list, PDF download & validation
│   ├── CinemaController.java  # Cinema CRUD operations
│   ├── HealthController.java  # System health check endpoint (/api/health)
│   ├── MovieController.java   # Movie CRUD & genre retrieval
│   ├── PaymentController.java # Razorpay order creation & signature verification
│   ├── SeatController.java    # Seat layout, seat locking & release
│   └── ShowController.java    # Show creation, seat generation & show listings
├── dto/                       # Request & Response Data Objects
├── entity/                    # JPA Entities (User, Movie, Cinema, Show, Seat, Booking)
├── enums/                     # Role (USER, ADMIN), SeatStatus, SeatType, BookingStatus
├── exception/                 # Custom Exception Handlers (@RestControllerAdvice)
├── repository/                # Spring Data JPA Repositories (with JOIN FETCH performance)
├── security/                  # AuthInterceptor, JwtUtil, @RequireAuth custom annotation
└── service/                   # Business logic implementations (PdfService, EmailService, etc.)
```

---

## ⚙️ Prerequisites & Configuration

### Prerequisites
* JDK 17
* Apache Maven 3.8+
* MySQL Server 8.0+

### Application Properties (`src/main/resources/application.properties`)
```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:mysql://localhost:3307/findseat?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password

# JWT & Security Settings
app.frontend.url=http://localhost:5173
jwt.secret=findseat_super_secret_jwt_key_2024
jwt.expiration-ms=604800000

# Razorpay Integration
razorpay.key.id=rzp_test_Sm06AUkqqNZFl1
razorpay.key.secret=xUvvPS2PH9Dn2gLuSxcrCEtX
app.seat-lock-minutes=5
```

---

## 🛠️ Build & Run Commands

### Compile Project
```bash
mvn clean compile
```

### Run Application
```bash
mvn spring-boot:run
```

### Package JAR
```bash
mvn clean package -DskipTests
java -jar target/findseat-backend-1.0.0.jar
```

---

## 🔐 Security & Auth Flow

1. **User Registration**: `POST /api/auth/register` creates an unverified user account and sends a 6-digit OTP to the user's email.
2. **OTP Verification**: `POST /api/auth/verify-otp` verifies the OTP and activates the account.
3. **Login**: `POST /api/auth/login` checks credentials via `BCryptPasswordEncoder` and returns a JWT token containing user ID, email, name, and role.
4. **Authorization**: Custom `@RequireAuth(admin = true/false)` annotation enforced by `AuthInterceptor` checks `Authorization: Bearer <JWT>` header on secured endpoints.

---

## 📑 Ticket PDF & QR Generation

When a booking is confirmed, `BookingService` calls:
* `PdfService`: Uses `OpenPDF` to render a ticket document containing booking ID, movie info, cinema location, seats, date, time, and total price.
* `ZXing`: Generates a embedded QR code linking to `/validate-ticket/{bookingId}` for instant gate verification.
