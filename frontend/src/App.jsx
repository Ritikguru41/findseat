import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider, useAuth } from './context/AuthContext';

import Login          from './pages/Login';
import Register       from './pages/Register';
import Home           from './pages/Home';
import MovieDetail    from './pages/MovieDetail';
import BookingPage    from './pages/BookingPage';      // Step 1 – Seat Selection
import PaymentSummary from './pages/PaymentSummary';  // Steps 2+3+4 – Pay
import BookingSuccess from './pages/BookingSuccess';  // Step 5 – Confirmed
import MyBookings     from './pages/MyBookings';
import AdminDashboard from './pages/admin/AdminDashboard';
import AdminMovies    from './pages/admin/AdminMovies';
import AdminCinemas   from './pages/admin/AdminCinemas';
import AdminShows     from './pages/admin/AdminShows';
import AdminBookings  from './pages/admin/AdminBookings';
import AdminUsers     from './pages/admin/AdminUsers';
import ValidateTicket from './pages/ValidateTicket';
import Navbar         from './components/Navbar';

/* ── Protected Route wrapper ─────────────────────────────────────────────── */
const ProtectedRoute = ({ children, adminOnly = false }) => {
  const { user, loading } = useAuth();
  if (loading) return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <div className="w-8 h-8 border-2 border-brand border-t-transparent rounded-full animate-spin" />
    </div>
  );
  if (!user) return <Navigate to="/login" replace />;
  if (adminOnly && user.role !== 'admin') return <Navigate to="/" replace />;
  return children;
};

/* ── Route tree ──────────────────────────────────────────────────────────── */
const AppRoutes = () => {
  const { user } = useAuth();
  return (
    <>
      <Navbar />
      <Routes>
        {/* ── Auth ─────────────────────────────────────────────────────── */}
        <Route path="/login"    element={user ? <Navigate to={user.role === 'admin' ? '/admin' : '/'} replace /> : <Login />} />
        <Route path="/register" element={user ? <Navigate to="/" replace /> : <Register />} />

        {/* ── Home / movies ────────────────────────────────────────────── */}
        <Route path="/"           element={<ProtectedRoute><Home /></ProtectedRoute>} />
        <Route path="/movies/:id" element={<ProtectedRoute><MovieDetail /></ProtectedRoute>} />

        {/* ── Booking flow ──────────────────────────────────────────────
            Step 1: /book/:showId       → BookingPage    (seat selection)
            Step 2: /payment-summary    → PaymentSummary (summary + payment)
            Step 3: /booking-success    → BookingSuccess (confirmation)
        ─────────────────────────────────────────────────────────────── */}
        <Route path="/book/:showId"     element={<ProtectedRoute><BookingPage /></ProtectedRoute>} />
        <Route path="/payment-summary"  element={<ProtectedRoute><PaymentSummary /></ProtectedRoute>} />
        <Route path="/booking-success"  element={<ProtectedRoute><BookingSuccess /></ProtectedRoute>} />

        {/* Keep old routes as aliases so existing bookmarks don't 404 */}
        <Route path="/booking-summary"  element={<ProtectedRoute><PaymentSummary /></ProtectedRoute>} />
        <Route path="/ticket-success"   element={<ProtectedRoute><BookingSuccess /></ProtectedRoute>} />

        {/* ── User area ─────────────────────────────────────────────── */}
        <Route path="/my-bookings" element={<ProtectedRoute><MyBookings /></ProtectedRoute>} />

        {/* ── Public – QR scan validation ───────────────────────────── */}
        <Route path="/validate/:bookingId" element={<ValidateTicket />} />

        {/* ── Admin area ───────────────────────────────────────────── */}
        <Route path="/admin"          element={<ProtectedRoute adminOnly><AdminDashboard /></ProtectedRoute>} />
        <Route path="/admin/movies"   element={<ProtectedRoute adminOnly><AdminMovies /></ProtectedRoute>} />
        <Route path="/admin/cinemas"  element={<ProtectedRoute adminOnly><AdminCinemas /></ProtectedRoute>} />
        <Route path="/admin/shows"    element={<ProtectedRoute adminOnly><AdminShows /></ProtectedRoute>} />
        <Route path="/admin/bookings" element={<ProtectedRoute adminOnly><AdminBookings /></ProtectedRoute>} />
        <Route path="/admin/users"    element={<ProtectedRoute adminOnly><AdminUsers /></ProtectedRoute>} />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  );
};

/* ── App root ─────────────────────────────────────────────────────────────── */
export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Toaster
          position="top-right"
          toastOptions={{
            style: { background: '#fff', color: '#111', border: '1px solid #eee' },
            success: { iconTheme: { primary: '#e50914', secondary: '#fff' } },
            duration: 4000,
          }}
        />
        <AppRoutes />
      </BrowserRouter>
    </AuthProvider>
  );
}
