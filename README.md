# 🎬 FindSeat - Modern Movie Ticket Booking System

[![GitHub Repository](https://img.shields.io/badge/GitHub-Ritikguru41%2FFindSeat-blue?logo=github)](https://github.com/Ritikguru41/findseat)
[![Spring Boot](https://img.shields.io/badge/Backend-Spring%20Boot%203.2-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/Frontend-React%2018%20%2B%20Vite-blue?logo=react)](https://react.dev/)
[![MySQL](https://img.shields.io/badge/Database-MySQL%208.0-blue?logo=mysql)](https://www.mysql.com/)

**FindSeat** is a full-stack, enterprise-grade movie ticket booking application designed for seamless movie discovery, interactive real-time seat selection, payment integration with Razorpay, instant PDF ticket generation with QR code verification, and a comprehensive admin management suite.

---

## ✨ Key Features

### 👤 User Capabilities
* **Authentication & Security**: Secure registration and login with Email OTP verification via JavaMailSender and JSON Web Tokens (JWT).
* **Movie Discovery**: Search movies by title, filter by genres, view ratings, duration, language, release dates, and trailers.
* **Showtimes & Cinemas**: Browse showtimes organized by cinema hall and date.
* **Interactive Seat Map**: Choose seats in real-time with visual indicators for AVAILABLE, LOCKED, and BOOKED status, including Normal and Premium tiers.
* **Temporary Seat Locking**: 5-minute concurrency seat lock mechanism to prevent double booking.
* **Payment Integration**: Integrated Razorpay payment gateway (Test & Live modes) with signature verification.
* **PDF Ticket & QR Verification**: Automatic PDF ticket generation complete with printable layout, booking metadata, and a scannable verification QR code.
* **My Bookings Dashboard**: View past and upcoming bookings with single-click PDF download.

### 🛡️ Admin Management Suite
* **Analytics Dashboard**: Real-time stats on total revenue, total bookings, active users, upcoming shows, and top-performing movies.
* **Movie Management**: Full CRUD operations for movies (titles, descriptions, poster/trailer URLs, genres, ratings, duration).
* **Cinema Management**: Add and manage cinema locations and screen capacities.
* **Show Management**: Schedule movie shows per cinema, date, time, screen, and ticket price with automatic seat map initialization.
* **User & Booking Audit**: Inspect all registered users and global booking records.

---

## 🛠️ Technology Stack

| Domain | Technology | Description |
| :--- | :--- | :--- |
| **Frontend** | React 18 + Vite | Fast, responsive single-page application |
| | Tailwind CSS | Modern utility-first styling |
| | Axios | HTTP client with request/response interceptors |
| | Lucide React | Clean icon system |
| **Backend** | Java 17 | Core runtime |
| | Spring Boot 3.2.5 | Enterprise REST API framework |
| | Spring Data JPA / Hibernate | ORM persistence layer |
| | MySQL 8.0 | Relational database |
| | JJWT (io.jsonwebtoken) | JWT authentication token handler |
| | Razorpay Java SDK | Payment gateway integration |
| | OpenPDF | PDF ticket generator |
| | ZXing | QR Code builder for ticket validation |

---

## 📁 Repository Structure

```
FindSeat/
├── backend/                  # Java Spring Boot Backend Application
│   ├── src/
│   │   ├── main/java/com/findseat/
│   │   │   ├── config/      # CORS & Web Configuration
│   │   │   ├── controller/  # REST Controllers (Auth, Movies, Shows, Seats, Bookings, Admin)
│   │   │   ├── dto/         # Request & Response Data Transfer Objects
│   │   │   ├── entity/      # JPA Entities (User, Movie, Cinema, Show, Seat, Booking)
│   │   │   ├── enums/       # Role, SeatStatus, SeatType, BookingStatus
│   │   │   ├── exception/   # Global Exception Handling
│   │   │   ├── repository/  # Spring Data JPA Repositories (with JOIN FETCH optimizations)
│   │   │   ├── security/    # JwtUtil & AuthInterceptor
│   │   │   └── service/     # Business Logic & Service Layers
│   │   └── resources/
│   │       └── application.properties # Application Configuration
│   ├── pom.xml               # Maven Build Dependencies
│   └── README.md             # Backend Specific Documentation
│
├── frontend/                 # React 18 + Vite Frontend Application
│   ├── src/
│   │   ├── api/             # Axios instance & Interceptors
│   │   ├── components/      # UI Components (Navbar, Footer, Admin Sidebar)
│   │   ├── context/         # AuthContext state management
│   │   └── pages/           # Pages (Home, Login, Register, MovieDetail, BookingPage, etc.)
│   ├── package.json
│   ├── vite.config.js
│   └── README.md             # Frontend Specific Documentation
└── README.md                 # Project Overview Documentation
```

---

## ⚡ Quick Start Guide

### Prerequisites
* **Java**: JDK 17 or later
* **Node.js**: v18+ and npm
* **MySQL**: MySQL 8.0+ server running on `localhost:3307` (or update `application.properties`)
* **Maven**: Installed or accessible via IDE

---

### 1. Database Setup

1. Start your MySQL database server.
2. Create the database:
```sql
CREATE DATABASE findseat;
```
*(Spring Boot Hibernate `ddl-auto=update` will automatically create and update the required table schemas).*

---

### 2. Backend Setup (Spring Boot)

1. Open terminal in the `backend/` directory:
```bash
cd backend
```
2. Verify `src/main/resources/application.properties` settings:
```properties
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3307/findseat?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD

app.frontend.url=http://localhost:5173
jwt.secret=findseat
razorpay.key.id=YOUR_RAZORPAY_KEY_ID
razorpay.key.secret=YOUR_RAZORPAY_KEY_SECRET
```
3. Compile & run the Spring Boot application:
```bash
mvn spring-boot:run
```
The backend server will start at `http://localhost:8080`.

---

### 3. Frontend Setup (React + Vite)

1. Open terminal in the `frontend/` directory:
```bash
cd frontend
```
2. Install dependencies:
```bash
npm install
```
3. Verify environment variables in `.env`:
```env
```
4. Start the Vite development server:
```bash
npm run dev
```
The application will be accessible at `http://localhost:5173`.

---

## 🌐 API Overview

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Register new user & send OTP | No |
| `POST` | `/api/auth/verify-otp` | Verify registration OTP | No |
| `POST` | `/api/auth/login` | Authenticate user & return JWT | No |
| `GET` | `/api/movies` | Get list of movies | No |
| `GET` | `/api/shows/movie/{id}` | Get upcoming showtimes for a movie | No |
| `GET` | `/api/seats/show/{showId}` | Get seat status map for a show | No |
| `POST` | `/api/seats/lock` | Lock selected seats temporarily | User |
| `POST` | `/api/payments/create-order` | Create Razorpay payment order | User |
| `POST` | `/api/payments/verify` | Verify payment & issue booking | User |
| `GET` | `/api/bookings/my` | Get current user's booking history | User |
| `GET` | `/api/bookings/download/{id}` | Download PDF ticket with QR code | User |
| `GET` | `/api/admin/stats` | View administrative analytics | Admin |

---


