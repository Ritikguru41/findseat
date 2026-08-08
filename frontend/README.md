# ⚛️ FindSeat Frontend (React 18 + Vite + Tailwind CSS)

The client-side single page application (SPA) for **FindSeat** built with React 18, Vite, Tailwind CSS, Axios, Lucide Icons, and React Router DOM.

Repository Reference: [https://github.com/Ritikguru41/findseat](https://github.com/Ritikguru41/findseat)

---

## 📱 Page Overview

| Path | Page Component | Purpose |
| :--- | :--- | :--- |
| `/` | `Home.jsx` | Featured movies, genre filtering, search bar, and release updates |
| `/movie/:id` | `MovieDetail.jsx` | Movie overview, duration, genre, trailer preview, and available showtimes |
| `/booking/:showId` | `BookingPage.jsx` | Interactive seat layout (Normal / Premium), real-time lock timer |
| `/payment-summary` | `PaymentSummary.jsx` | Order review, Razorpay checkout modal integration, fallback demo payment |
| `/booking-success` | `BookingSuccess.jsx` | Confirmation screen with downloadable PDF ticket & QR code |
| `/my-bookings` | `MyBookings.jsx` | User booking history & single-click ticket download |
| `/login` | `Login.jsx` | User authentication & OTP login fallback |
| `/register` | `Register.jsx` | New user signup & email OTP verification |
| `/validate-ticket/:id` | `ValidateTicket.jsx` | QR code ticket validation scanner page |
| `/admin` | `AdminDashboard.jsx` | Admin metrics: revenue, bookings, user count, top movies |
| `/admin/movies` | `AdminMovies.jsx` | Add, edit, or delete movies |
| `/admin/cinemas` | `AdminCinemas.jsx` | Manage cinema venues & screen layouts |
| `/admin/shows` | `AdminShows.jsx` | Schedule showtimes for movies in cinemas |
| `/admin/users` | `AdminUsers.jsx` | User accounts list & management |
| `/admin/bookings` | `AdminBookings.jsx` | Complete global bookings registry |

---

## 🔧 Environment Variables (`.env`)

Create or update `.env` in the `frontend/` root:

```env
# Razorpay Key ID
VITE_RAZORPAY_KEY_ID=rzp_test_Sm06AUkqqNZFl1

# Spring Boot Backend API Base URL
VITE_API_URL=http://localhost:8080/api
```

---

## 🚀 Development & Production Build

### Install Dependencies
```bash
npm install
```

### Start Local Development Server
```bash
npm run dev
```
Open `http://localhost:5173` in your browser.

### Build Production Bundle
```bash
npm run build
```
Generates optimized static assets in the `dist/` directory.

### Preview Production Build
```bash
npm run preview
```
