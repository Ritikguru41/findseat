import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import {
  CheckCircle, Download, Ticket, Calendar, Clock,
  MapPin, Film, CreditCard, Mail, Home, Loader2,
} from 'lucide-react';
import api from '../api/axios';
import toast from 'react-hot-toast';

/**
 * BookingSuccess — STEP 4: Confirmation Screen
 *
 * Receives booking result via location.state.booking
 */
export default function BookingSuccess() {
  const location = useLocation();
  const navigate  = useNavigate();
  const booking   = location.state?.booking;

  const [downloading,  setDownloading]  = useState(false);
  const [showConfetti, setShowConfetti] = useState(true);

  useEffect(() => {
    if (!booking) {
      navigate('/', { replace: true });
      return;
    }
    // Stop confetti particles after 2.8 s
    const t = setTimeout(() => setShowConfetti(false), 2800);
    return () => clearTimeout(t);
  }, [booking, navigate]);

  if (!booking) return null;

  /* ── Download PDF ────────────────────────────────────────────────────── */
  const downloadTicket = async () => {
    setDownloading(true);
    try {
      const res = await api.get(`/bookings/download/${booking.bookingId}`, {
        responseType: 'blob',
      });
      const url = window.URL.createObjectURL(
        new Blob([res.data], { type: 'application/pdf' })
      );
      const a = document.createElement('a');
      a.href     = url;
      a.download = `FindSeat_${booking.bookingId}.pdf`;
      a.click();
      window.URL.revokeObjectURL(url);
      toast.success('Ticket downloaded!');
    } catch {
      toast.error('Download failed. Try again from My Bookings.');
    } finally {
      setDownloading(false);
    }
  };

  const showDateFormatted = booking.showDate
    ? new Date(booking.showDate).toLocaleDateString('en-IN', {
        weekday: 'long', day: '2-digit', month: 'long', year: 'numeric',
      })
    : '—';

  const CONFETTI_COLORS = ['#e50914', '#22c55e', '#3b82f6', '#a855f7', '#f59e0b', '#06b6d4'];

  return (
    <div className="min-h-screen bg-gradient-to-br from-emerald-50 via-green-50/30 to-teal-50 flex items-center justify-center p-4">

      {/* ── Confetti ─────────────────────────────────────────────────── */}
      {showConfetti && (
        <div className="fixed inset-0 pointer-events-none overflow-hidden z-0" aria-hidden>
          {[...Array(36)].map((_, i) => (
            <div
              key={i}
              className="confetti-particle"
              style={{
                left:            `${(i * 2.8) % 100}%`,
                animationDelay:  `${(i * 0.07) % 1.4}s`,
                background:      CONFETTI_COLORS[i % CONFETTI_COLORS.length],
                width:           `${7 + (i % 5)}px`,
                height:          `${7 + (i % 4)}px`,
                borderRadius:    i % 3 === 0 ? '50%' : '2px',
              }}
            />
          ))}
        </div>
      )}

      <div className="relative z-10 w-full max-w-lg">

        {/* ── Card ─────────────────────────────────────────────────────── */}
        <div className="bg-white rounded-3xl shadow-2xl border border-gray-100 overflow-hidden animate-scale-in">

          {/* Top stripe */}
          <div className="h-2 bg-gradient-to-r from-emerald-400 via-green-500 to-teal-400" />

          {/* Header */}
          <div className="px-8 pt-8 pb-6 text-center">
            <div className="w-20 h-20 bg-gradient-to-br from-green-400 to-emerald-600 rounded-full flex items-center justify-center mx-auto mb-4 shadow-xl shadow-green-200 animate-bounce-once">
              <CheckCircle className="w-11 h-11 text-white" strokeWidth={2.5} />
            </div>
            <h1 className="text-3xl font-black text-gray-900 mb-1">Booking Confirmed!</h1>
            <p className="text-gray-500 text-sm">
              🎉 Enjoy the show,{' '}
              <strong className="text-gray-700">{booking.userName?.split(' ')[0] || 'there'}</strong>!
            </p>
            {booking.userEmail && (
              <div className="inline-flex items-center gap-1.5 mt-3 text-xs text-gray-400 bg-gray-50 border border-gray-200 px-3 py-1.5 rounded-full">
                <Mail size={11} />
                PDF ticket sent to <strong>{booking.userEmail}</strong>
              </div>
            )}
          </div>

          {/* ── Perforated ticket ──────────────────────────────────────── */}
          <div className="mx-0 px-6 mb-6">
            {/* Top perforation */}
            <div className="flex items-center -mx-6 mb-4">
              <div className="w-6 h-6 rounded-full bg-emerald-50 flex-shrink-0" />
              <div className="flex-1 border-t-2 border-dashed border-gray-200" />
              <div className="w-6 h-6 rounded-full bg-emerald-50 flex-shrink-0" />
            </div>

            {/* Ticket body */}
            <div className="bg-gradient-to-br from-slate-50 to-blue-50/40 rounded-2xl border border-gray-200 p-5">

              {/* Movie */}
              <div className="flex items-center gap-3 mb-5 pb-4 border-b border-gray-200">
                <div className="w-9 h-9 bg-brand/10 rounded-xl flex items-center justify-center flex-shrink-0">
                  <Film size={18} className="text-brand" />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="font-black text-gray-900 text-base leading-tight truncate">{booking.movieTitle}</p>
                  {booking.genre && <p className="text-xs text-gray-400">{booking.genre}</p>}
                </div>
              </div>

              {/* Details grid */}
              <div className="grid grid-cols-2 gap-4 mb-5">
                <TicketInfo icon={<MapPin size={13} className="text-brand" />} label="Cinema" value={booking.cinemaName || 'FindSeat Cinemas'} sub={booking.cinemaLocation} />
                <TicketInfo icon={<Ticket size={13} className="text-brand" />} label="Screen" value={`Screen ${booking.screen}`} />
                <TicketInfo icon={<Calendar size={13} className="text-brand" />} label="Date" value={showDateFormatted} />
                <TicketInfo icon={<Clock size={13} className="text-brand" />} label="Time" value={booking.showTime?.slice(0, 5) || '—'} />
              </div>

              {/* Seats */}
              <div className="mb-5 pb-4 border-b border-gray-200">
                <p className="text-[11px] text-gray-400 font-semibold uppercase mb-2">Reserved Seats</p>
                <div className="flex flex-wrap gap-1.5">
                  {booking.seatNumbers?.split(', ').map(sn => (
                    <span
                      key={sn}
                      className="px-2.5 py-1 bg-blue-600 text-white text-xs font-bold rounded-lg"
                    >
                      {sn}
                    </span>
                  ))}
                </div>
              </div>

              {/* IDs + Amount */}
              <div className="flex items-end justify-between">
                <div>
                  <p className="text-[11px] text-gray-400">Booking ID</p>
                  <p className="font-mono text-sm font-black text-brand">{booking.bookingId}</p>
                  {booking.paymentId && (
                    <div className="mt-1">
                      <p className="text-[11px] text-gray-400">Payment ID</p>
                      <p className="font-mono text-[11px] text-gray-500 max-w-[160px] truncate">{booking.paymentId}</p>
                    </div>
                  )}
                </div>
                <div className="text-right">
                  <p className="text-[11px] text-gray-400">Amount Paid</p>
                  <p className="text-2xl font-black text-green-600">₹{booking.totalAmount}</p>
                  <span className="inline-flex items-center gap-1 text-[10px] font-bold text-green-600 bg-green-50 border border-green-200 px-2 py-0.5 rounded-full mt-0.5">
                    <CheckCircle size={9} /> CONFIRMED
                  </span>
                </div>
              </div>
            </div>

            {/* Bottom perforation */}
            <div className="flex items-center -mx-6 mt-4">
              <div className="w-6 h-6 rounded-full bg-emerald-50 flex-shrink-0" />
              <div className="flex-1 border-t-2 border-dashed border-gray-200" />
              <div className="w-6 h-6 rounded-full bg-emerald-50 flex-shrink-0" />
            </div>
          </div>

          {/* ── Buttons ───────────────────────────────────────────────── */}
          <div className="px-6 pb-8 space-y-3">
            <button
              onClick={downloadTicket}
              disabled={downloading}
              id="btn-download-ticket"
              className="btn-primary w-full flex items-center justify-center gap-2 py-4 text-base"
            >
              {downloading
                ? <><Loader2 size={18} className="animate-spin" /> Generating PDF…</>
                : <><Download size={18} /> Download PDF Ticket</>
              }
            </button>

            <div className="grid grid-cols-2 gap-3">
              <button
                onClick={() => navigate('/my-bookings')}
                className="btn-secondary flex items-center justify-center gap-2 text-sm py-3"
              >
                <Ticket size={15} />
                My Bookings
              </button>
              <button
                onClick={() => navigate('/')}
                className="btn-secondary flex items-center justify-center gap-2 text-sm py-3"
              >
                <Home size={15} />
                Home
              </button>
            </div>
          </div>
        </div>

        {/* Footer */}
        <p className="text-center text-xs text-gray-400 mt-4 flex items-center justify-center gap-1">
          <CreditCard size={11} />
          Secured by Razorpay · Booking {booking.bookingId}
        </p>
      </div>
    </div>
  );
}

/* ── Sub-component ──────────────────────────────────────────────────────── */
const TicketInfo = ({ icon, label, value, sub }) => (
  <div>
    <div className="flex items-center gap-1 mb-0.5">
      {icon}
      <p className="text-[10px] text-gray-400 font-semibold uppercase">{label}</p>
    </div>
    <p className="text-sm font-semibold text-gray-800 leading-tight">{value}</p>
    {sub && <p className="text-[11px] text-gray-400">{sub}</p>}
  </div>
);
