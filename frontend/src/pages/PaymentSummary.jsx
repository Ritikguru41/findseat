import { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';
import {
  Film, MapPin, Calendar, Clock, Armchair, CreditCard,
  ChevronLeft, Tag, Users, Shield, Info, Loader2,
  AlertCircle, RefreshCw, CheckCircle,
} from 'lucide-react';

/**
 * Ensures window.Razorpay is available.
 * If the script wasn't loaded (or is still loading), injects it and waits.
 * Resolves when window.Razorpay is ready, rejects after 10 s timeout.
 */
function ensureRazorpayLoaded() {
  return new Promise((resolve, reject) => {
    // Already loaded — done immediately
    if (window.Razorpay) return resolve();

    // Script already in DOM but still loading — wait for it
    const existing = document.querySelector(
      'script[src*="checkout.razorpay.com"]'
    );
    if (existing) {
      const wait = setInterval(() => {
        if (window.Razorpay) { clearInterval(wait); resolve(); }
      }, 100);
      setTimeout(() => { clearInterval(wait); reject(new Error('Razorpay SDK timed out. Please refresh.')); }, 10000);
      return;
    }

    // Inject script dynamically as a last resort
    const script  = document.createElement('script');
    script.src    = 'https://checkout.razorpay.com/v1/checkout.js';
    script.async  = true;
    script.onload = () => resolve();
    script.onerror = () => reject(new Error('Failed to load Razorpay SDK. Check your internet connection.'));
    document.body.appendChild(script);
  });
}


/**
 * PaymentSummary — STEPS 2 + 3 + 4
 *
 * Responsibilities (ALL in one component):
 *   Step 2 — Show booking summary (movie, cinema, date, seats, price)
 *   Step 3 — Lock seats → Create Razorpay order → Open Razorpay popup
 *   Step 4 — Backend HMAC verify → POST /api/bookings → navigate to /booking-success
 *
 * Receives via location.state:
 *   { show, selectedSeats, totalPrice, normalCount, premiumCount, showId }
 */
export default function PaymentSummary() {
  const location = useLocation();
  const navigate = useNavigate();
  const { user } = useAuth();

  const state = location.state;

  /* ── Guard ───────────────────────────────────────────────────────────── */
  useEffect(() => {
    if (!state?.show || !state?.selectedSeats?.length) {
      navigate('/', { replace: true });
    }
  }, [state, navigate]);

  if (!state?.show || !state?.selectedSeats?.length) return null;

  const { show, selectedSeats, totalPrice, normalCount, premiumCount, showId } = state;

  /* ── Local state ─────────────────────────────────────────────────────── */
  const [paying, setPaying]       = useState(false);
  const [payError, setPayError]   = useState(null);
  const [step, setStep]           = useState('summary'); // 'summary' | 'locking' | 'paying' | 'verifying' | 'booking'

  /* ── Derived ─────────────────────────────────────────────────────────── */
  const normalPrice  = Number(show.price || 200);
  const premiumPrice = normalPrice + 100;

  const showDateFormatted = show.show_date
    ? new Date(show.show_date).toLocaleDateString('en-IN', {
        weekday: 'long', day: '2-digit', month: 'long', year: 'numeric',
      })
    : '—';
  const showTime = show.show_time?.slice(0, 5) || '—';
  const seatIds  = selectedSeats.map(s => s.id);

  /* ── STEP 3+4: Full payment flow ─────────────────────────────────────── */
  const handlePay = async () => {
    if (paying) return;
    setPaying(true);
    setPayError(null);

    try {
      // ── 1. Ensure Razorpay SDK is loaded before anything else ─────────
      await ensureRazorpayLoaded();

      // ── 2. Lock seats (5-minute hold) ─────────────────────────────────
      setStep('locking');
      await api.post('/seats/lock', { showId, seatIds });

      // ── 3. Create Razorpay order on backend ───────────────────────────
      setStep('paying');
      const { data: order } = await api.post('/payments/create-order', {
        amount: totalPrice,
        showId,
        seatIds,
      });

      console.log('[FindSeat] Order created:', { isDemo: order.isDemo, orderId: order.orderId });

      // ── 4a. Demo mode (backend has no valid Razorpay keys) ────────────
      if (order.isDemo) {
        toast('🛠️ Demo mode – simulating payment (configure Razorpay keys for real payments)', { icon: '🧪', duration: 5000 });

        setStep('verifying');
        const { data: verifyData } = await api.post('/payments/verify', {
          razorpay_order_id:   order.orderId,
          razorpay_payment_id: `pay_demo_${Date.now()}`,
          razorpay_signature:  'demo_signature',
          isDemo:              true,
        });
        if (!verifyData.success) throw new Error('Demo verification failed');

        setStep('booking');
        await finalizeBooking(verifyData.paymentId, order.orderId);
        return;
      }

      // ── 4b. Real Razorpay popup ───────────────────────────────────────
      // Use key from backend response; fall back to VITE env var as safety net
      const razorpayKey = order.key ||
        import.meta.env.VITE_RAZORPAY_KEY_ID;

      if (!razorpayKey) {
        throw new Error(
          'Razorpay key is missing. Add VITE_RAZORPAY_KEY_ID to frontend/.env and restart the dev server.'
        );
      }

      console.log('[FindSeat] Opening Razorpay with key:', razorpayKey.slice(0, 12) + '…');

      const options = {
        key:         razorpayKey,
        amount:      order.amount,             // in paise
        currency:    order.currency || 'INR',
        name:        'FindSeat',
        description: `${selectedSeats.length} ticket(s) – ${show.movie_title}`,
        image:       `${window.location.origin}/favicon.svg`,
        order_id:    order.orderId,

        // ── 5. Payment success handler ──────────────────────────────────
        handler: async (response) => {
          try {
            console.log('[FindSeat] Razorpay success, verifying on backend…');
            setStep('verifying');

            const { data: verifyData } = await api.post('/payments/verify', {
              razorpay_order_id:   response.razorpay_order_id,
              razorpay_payment_id: response.razorpay_payment_id,
              razorpay_signature:  response.razorpay_signature,
              isDemo:              false,
            });

            if (!verifyData.success) {
              throw new Error('Payment verification failed on the server.');
            }

            console.log('[FindSeat] Verification passed. Creating booking…');
            setStep('booking');
            await finalizeBooking(verifyData.paymentId, response.razorpay_order_id);
          } catch (err) {
            const msg = err.response?.data?.message || err.message || 'Payment verification failed';
            handlePaymentError(msg, seatIds);
          }
        },

        prefill: {
          name:  user?.name  || '',
          email: user?.email || '',
        },
        theme:  { color: '#e50914' },
        modal: {
          backdropclose: false,
          escape:        false,
          ondismiss: async () => {
            toast('Payment cancelled', { icon: '❌' });
            await api.post('/seats/release', { seatIds }).catch(() => {});
            setPaying(false);
            setStep('summary');
          },
        },
      };

      const rzp = new window.Razorpay(options);
      rzp.on('payment.failed', async (errResp) => {
        const msg = errResp?.error?.description || 'Payment failed';
        handlePaymentError(`Payment failed: ${msg}`, seatIds);
        rzp.close();
      });
      rzp.open();

    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Failed to initiate payment';
      handlePaymentError(msg, seatIds);
    }
  };

  /* ── Error helper ────────────────────────────────────────────────────── */
  const handlePaymentError = async (msg, ids) => {
    toast.error(msg);
    setPayError(msg);
    await api.post('/seats/release', { seatIds: ids }).catch(() => {});
    setPaying(false);
    setStep('summary');
  };

  /* ── 5. Create booking after successful payment ───────────────────────── */
  const finalizeBooking = async (paymentId, razorpay_order_id) => {
    const { data } = await api.post('/bookings', {
      showId,
      seatIds,
      paymentId,
      razorpay_order_id,
      totalAmount: totalPrice,
    });
    toast.success('Booking confirmed! 🎉');
    navigate('/booking-success', {
      state: { booking: data.booking },
      replace: true,
    });
  };

  /* ── Processing overlay (while Razorpay is open / verifying) ────────── */
  if (paying) {
    const stepLabels = {
      locking:   'Securing your seats…',
      paying:    'Opening payment window…',
      verifying: 'Verifying payment…',
      booking:   'Confirming booking…',
    };
    return (
      <div className="min-h-screen bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900 flex flex-col items-center justify-center gap-6 p-6">
        <div className="w-24 h-24 bg-brand/20 rounded-full flex items-center justify-center border border-brand/30">
          <CreditCard className="w-12 h-12 text-brand animate-pulse" />
        </div>
        <div className="text-center">
          <h2 className="text-2xl font-black text-white mb-2">
            {step === 'verifying' ? 'Verifying Payment' :
             step === 'booking'   ? 'Confirming Booking' :
             'Processing Payment'}
          </h2>
          <p className="text-gray-400">{stepLabels[step] || 'Please wait…'}</p>
        </div>
        <div className="flex gap-2">
          {[0, 1, 2].map(i => (
            <div
              key={i}
              className="w-2.5 h-2.5 rounded-full bg-brand animate-bounce"
              style={{ animationDelay: `${i * 0.18}s` }}
            />
          ))}
        </div>
        <p className="text-xs text-gray-600 max-w-xs text-center">
          Do not close or refresh this page. Your seats are reserved for 5 minutes.
        </p>
      </div>
    );
  }

  /* ── Main summary view ───────────────────────────────────────────────── */
  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50/20 to-slate-50">
      <div className="max-w-2xl mx-auto px-4 sm:px-6 py-10">

        {/* ── Header ───────────────────────────────────────────────────── */}
        <div className="flex items-center gap-3 mb-2">
          <button
            onClick={() => navigate(-1)}
            className="w-9 h-9 flex items-center justify-center rounded-full border border-gray-300 hover:border-brand hover:text-brand text-gray-600 transition-all"
          >
            <ChevronLeft size={18} />
          </button>
          <div>
            <h1 className="text-2xl font-black text-gray-900">Payment Summary</h1>
            <p className="text-sm text-gray-500">Review your order before paying</p>
          </div>
        </div>

        {/* ── Step indicator ───────────────────────────────────────────── */}
        <div className="flex items-center justify-center gap-2 mb-8 mt-4">
          {[
            { n: 1, label: 'Select Seats', done: true },
            { n: 2, label: 'Summary', active: true },
            { n: 3, label: 'Pay' },
            { n: 4, label: 'Confirmed' },
          ].map((s, i, arr) => (
            <div key={s.n} className="flex items-center gap-2">
              <div className={`flex items-center gap-1.5 ${s.active ? 'text-brand' : s.done ? 'text-green-600' : 'text-gray-400'}`}>
                <div className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold border-2 ${
                  s.active ? 'bg-brand border-brand text-white' :
                  s.done   ? 'bg-green-100 border-green-500 text-green-700' :
                             'border-gray-300 text-gray-400'
                }`}>
                  {s.done ? <CheckCircle size={13} /> : s.n}
                </div>
                <span className="text-xs font-medium hidden sm:block">{s.label}</span>
              </div>
              {i < arr.length - 1 && <div className="w-6 h-px bg-gray-300" />}
            </div>
          ))}
        </div>

        {/* ── Movie Banner ─────────────────────────────────────────────── */}
        <div className="relative rounded-2xl overflow-hidden mb-6 shadow-lg h-32">
          {show.poster_url && (
            <img
              src={show.poster_url}
              alt={show.movie_title}
              className="w-full h-full object-cover object-top scale-105 opacity-25"
            />
          )}
          <div className="absolute inset-0 bg-gradient-to-r from-gray-900/95 via-gray-900/80 to-gray-900/60" />
          <div className="absolute inset-0 flex items-center gap-5 px-6">
            {show.poster_url && (
              <img
                src={show.poster_url}
                alt={show.movie_title}
                className="w-14 h-20 rounded-xl object-cover shadow-xl border border-white/20 flex-shrink-0"
                onError={e => (e.target.style.display = 'none')}
              />
            )}
            <div>
              <div className="flex items-center gap-2 mb-1">
                <Film size={13} className="text-brand" />
                <span className="text-brand text-xs font-semibold uppercase tracking-wider">
                  {show.genre || 'Movie'}
                </span>
              </div>
              <h2 className="text-lg font-black text-white leading-tight">{show.movie_title}</h2>
              <p className="text-xs text-gray-400 mt-0.5">{show.cinema_name} · Screen {show.screen}</p>
            </div>
          </div>
        </div>

        {/* ── Show Details ─────────────────────────────────────────────── */}
        <div className="bg-white rounded-2xl border border-gray-200 shadow-sm mb-4 overflow-hidden">
          <div className="px-5 py-3 bg-gray-50/80 border-b border-gray-100">
            <p className="text-[11px] font-bold text-gray-400 uppercase tracking-wider">Show Details</p>
          </div>
          <div className="p-5 grid grid-cols-2 gap-4">
            <DetailItem icon={<MapPin size={14} className="text-brand" />} label="Cinema" value={show.cinema_name || 'FindSeat Cinemas'} sub={show.cinema_location} />
            <DetailItem icon={<Calendar size={14} className="text-brand" />} label="Date" value={showDateFormatted} />
            <DetailItem icon={<Clock size={14} className="text-brand" />} label="Time" value={showTime} />
            <DetailItem icon={<Tag size={14} className="text-brand" />} label="Screen" value={`Screen ${show.screen}`} />
          </div>
        </div>

        {/* ── Selected Seats ───────────────────────────────────────────── */}
        <div className="bg-white rounded-2xl border border-gray-200 shadow-sm mb-4 overflow-hidden">
          <div className="px-5 py-3 bg-gray-50/80 border-b border-gray-100 flex items-center justify-between">
            <p className="text-[11px] font-bold text-gray-400 uppercase tracking-wider">Selected Seats</p>
            <div className="flex items-center gap-1 text-xs text-gray-400">
              <Users size={12} />
              {selectedSeats.length} ticket{selectedSeats.length > 1 ? 's' : ''}
            </div>
          </div>
          <div className="p-5">
            <div className="flex flex-wrap gap-2 mb-4">
              {selectedSeats.map(seat => (
                <span
                  key={seat.id}
                  className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm font-bold border ${
                    seat.type === 'premium'
                      ? 'bg-purple-50 border-purple-300 text-purple-700'
                      : 'bg-green-50 border-green-300 text-green-700'
                  }`}
                >
                  <Armchair size={12} />
                  {seat.seat_number}
                  <span className="text-[10px] font-normal opacity-60">
                    {seat.type === 'premium' ? 'PREMIUM' : 'NORMAL'}
                  </span>
                </span>
              ))}
            </div>
            <div className="flex gap-4 text-xs text-gray-400">
              {normalCount > 0 && (
                <span className="flex items-center gap-1">
                  <span className="w-2 h-2 rounded-full bg-green-400 inline-block" />
                  Normal × {normalCount}
                </span>
              )}
              {premiumCount > 0 && (
                <span className="flex items-center gap-1">
                  <span className="w-2 h-2 rounded-full bg-purple-400 inline-block" />
                  Premium × {premiumCount}
                </span>
              )}
            </div>
          </div>
        </div>

        {/* ── Price Breakdown ───────────────────────────────────────────── */}
        <div className="bg-white rounded-2xl border border-gray-200 shadow-sm mb-5 overflow-hidden">
          <div className="px-5 py-3 bg-gray-50/80 border-b border-gray-100">
            <p className="text-[11px] font-bold text-gray-400 uppercase tracking-wider">Price Breakdown</p>
          </div>
          <div className="p-5 space-y-3">
            {normalCount > 0 && (
              <div className="flex justify-between text-sm">
                <span className="text-gray-600 flex items-center gap-2">
                  <span className="w-2 h-2 rounded-full bg-green-400" />
                  Normal × {normalCount}
                </span>
                <span className="font-semibold text-gray-800">
                  ₹{normalPrice} × {normalCount} = <strong>₹{normalPrice * normalCount}</strong>
                </span>
              </div>
            )}
            {premiumCount > 0 && (
              <div className="flex justify-between text-sm">
                <span className="text-gray-600 flex items-center gap-2">
                  <span className="w-2 h-2 rounded-full bg-purple-400" />
                  Premium × {premiumCount}
                </span>
                <span className="font-semibold text-gray-800">
                  ₹{premiumPrice} × {premiumCount} = <strong>₹{premiumPrice * premiumCount}</strong>
                </span>
              </div>
            )}
            <div className="flex justify-between text-sm text-gray-400">
              <span>Convenience Fee</span>
              <span className="text-green-600 font-medium">FREE</span>
            </div>
            <div className="pt-3 border-t-2 border-dashed border-gray-200">
              <div className="flex justify-between items-center">
                <span className="text-lg font-black text-gray-900">Total Amount</span>
                <span className="text-3xl font-black text-brand">₹{totalPrice}</span>
              </div>
            </div>
          </div>
        </div>

        {/* ── Error banner ─────────────────────────────────────────────── */}
        {payError && (
          <div className="flex items-start gap-3 p-4 bg-red-50 border border-red-200 rounded-xl mb-5 animate-slide-up">
            <AlertCircle size={17} className="text-red-500 flex-shrink-0 mt-0.5" />
            <div className="flex-1">
              <p className="text-sm text-red-800 font-semibold">Payment Failed</p>
              <p className="text-xs text-red-600 mt-0.5">{payError}</p>
            </div>
            <button
              onClick={() => setPayError(null)}
              className="flex items-center gap-1 text-xs text-red-600 hover:text-red-800"
            >
              <RefreshCw size={12} /> Dismiss
            </button>
          </div>
        )}

        {/* ── Trust badges ─────────────────────────────────────────────── */}
        <div className="flex items-start gap-3 mb-4 px-4 py-3 bg-blue-50 border border-blue-200 rounded-xl">
          <Shield size={15} className="text-blue-500 flex-shrink-0 mt-0.5" />
          <p className="text-xs text-blue-700 leading-relaxed">
            Secured by <strong>Razorpay</strong>. Booking confirmed <strong>only</strong> after
            server-side payment verification. We never store your card details.
          </p>
        </div>
        <div className="flex items-start gap-3 mb-8 px-4 py-3 bg-amber-50 border border-amber-200 rounded-xl">
          <Info size={15} className="text-amber-500 flex-shrink-0 mt-0.5" />
          <p className="text-xs text-amber-700 leading-relaxed">
            Your seats are <strong>locked for 5 minutes</strong> during payment.
            A PDF ticket and email confirmation will be sent after booking.
          </p>
        </div>

        {/* ── Action Buttons ────────────────────────────────────────────── */}
        <div className="flex gap-3">
          <button
            onClick={() => navigate(-1)}
            disabled={paying}
            className="btn-secondary flex-1 flex items-center justify-center gap-2"
          >
            <ChevronLeft size={17} />
            Back
          </button>
          <button
            onClick={handlePay}
            disabled={paying}
            id="btn-proceed-payment"
            className="btn-primary flex-1 flex items-center justify-center gap-2 py-4 text-base"
          >
            {paying
              ? <><Loader2 size={18} className="animate-spin" /> Processing…</>
              : <><CreditCard size={20} /> Proceed to Payment — ₹{totalPrice}</>
            }
          </button>
        </div>

      </div>
    </div>
  );
}

/* ── Sub-component ──────────────────────────────────────────────────────── */
const DetailItem = ({ icon, label, value, sub }) => (
  <div className="flex items-start gap-2.5">
    <div className="mt-0.5 flex-shrink-0">{icon}</div>
    <div>
      <p className="text-[11px] text-gray-400 mb-0.5">{label}</p>
      <p className="text-sm font-semibold text-gray-900 leading-tight">{value}</p>
      {sub && <p className="text-[11px] text-gray-400">{sub}</p>}
    </div>
  </div>
);
