import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';
import { Armchair, ChevronLeft, Loader2, Info, ChevronRight } from 'lucide-react';

/**
 * BookingPage — STEP 1: Seat Selection ONLY
 *
 * Responsibilities:
 *   • Load show details + seat map
 *   • Let user toggle seats
 *   • Navigate to /payment-summary with selected seat data
 *
 * NO payment logic here. Payment is handled entirely in PaymentSummary.jsx
 */
export default function BookingPage() {
  const { showId } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();

  const [show, setShow] = useState(null);
  const [seats, setSeats] = useState([]);
  const [selected, setSelected] = useState([]);
  const [loading, setLoading] = useState(true);

  /* ── Fetch show + seat map ───────────────────────────────────────────── */
  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const [showRes, seatRes] = await Promise.all([
        api.get(`/shows/${showId}`),
        api.get(`/seats/show/${showId}`),
      ]);
      setShow(showRes.data);
      setSeats(seatRes.data);
    } catch {
      toast.error('Failed to load show details');
    } finally {
      setLoading(false);
    }
  }, [showId]);

  useEffect(() => { fetchData(); }, [fetchData]);

  /* ── Auto-expire UI-side locks every 5 s ────────────────────────────── */
  useEffect(() => {
    const interval = setInterval(() => {
      setSeats(prev =>
        prev.map(s =>
          s.status === 'locked' &&
          s.lock_expires &&
          new Date(s.lock_expires) < new Date()
            ? { ...s, status: 'available', locked_by: null }
            : s
        )
      );
    }, 5000);
    return () => clearInterval(interval);
  }, []);

  /* ── Seat interactions ───────────────────────────────────────────────── */
  const toggleSeat = seat => {
    if (seat.status === 'booked') return;
    if (seat.status === 'locked' && seat.locked_by !== user?.id) return;
    setSelected(prev =>
      prev.includes(seat.id)
        ? prev.filter(id => id !== seat.id)
        : [...prev, seat.id]
    );
  };

  const getSeatClass = seat => {
    if (seat.status === 'booked') return 'seat-booked';
    if (seat.status === 'locked' && seat.locked_by !== user?.id) return 'seat-locked';
    if (selected.includes(seat.id)) return 'seat-selected';
    if (seat.type === 'premium') return 'seat-premium';
    return 'seat-available';
  };

  /* ── Derived state ───────────────────────────────────────────────────── */
  const rowMap = {};
  seats.forEach(s => {
    if (!rowMap[s.row_label]) rowMap[s.row_label] = [];
    rowMap[s.row_label].push(s);
  });
  const rows = Object.keys(rowMap).sort();

  const selectedSeats = seats.filter(s => selected.includes(s.id));
  const normalCount   = selectedSeats.filter(s => s.type !== 'premium').length;
  const premiumCount  = selectedSeats.filter(s => s.type === 'premium').length;
  const normalPrice   = Number(show?.price || 200);
  const premiumPrice  = normalPrice + 100;
  const totalPrice    = normalCount * normalPrice + premiumCount * premiumPrice;

  /* ── STEP 1 → STEP 2: Navigate to Payment Summary ────────────────────── */
  const handleProceedToSummary = () => {
    if (selected.length === 0) {
      return toast.error('Please select at least one seat');
    }
    navigate('/payment-summary', {
      state: {
        show,           // full show object (has cinema_name, price, etc.)
        selectedSeats,  // array of seat objects
        totalPrice,
        normalCount,
        premiumCount,
        showId,
      },
    });
  };

  /* ── Loading ─────────────────────────────────────────────────────────── */
  if (loading) return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center gap-3">
      <Loader2 className="w-10 h-10 text-brand animate-spin" />
      <p className="text-gray-500 text-sm animate-pulse">Loading seat map…</p>
    </div>
  );

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 py-8">

        {/* ── Header ───────────────────────────────────────────────────── */}
        <div className="flex items-center gap-4 mb-8">
          <button
            onClick={() => navigate(-1)}
            className="w-9 h-9 flex items-center justify-center rounded-full border border-gray-300 hover:border-brand hover:text-brand text-gray-600 transition-all"
          >
            <ChevronLeft size={18} />
          </button>
          <div>
            <h1 className="text-xl font-black text-gray-900">{show?.movie_title}</h1>
            <p className="text-sm text-gray-500">
              {show?.cinema_name && <span className="font-medium text-gray-700 mr-2">{show.cinema_name}</span>}
              {show?.show_date && new Date(show.show_date).toLocaleDateString('en-IN', {
                weekday: 'short', day: '2-digit', month: 'short',
              })}
              {show?.show_time && ` · ${show.show_time.slice(0, 5)}`}
              {show?.screen && ` · Screen ${show.screen}`}
            </p>
          </div>
        </div>

        {/* ── Step indicator ───────────────────────────────────────────── */}
        <div className="flex items-center justify-center gap-2 mb-6">
          {[
            { n: 1, label: 'Select Seats' },
            { n: 2, label: 'Payment Summary' },
            { n: 3, label: 'Pay' },
            { n: 4, label: 'Confirmed' },
          ].map((step, i, arr) => (
            <div key={step.n} className="flex items-center gap-2">
              <div className={`flex items-center gap-1.5 ${step.n === 1 ? 'text-brand' : 'text-gray-400'}`}>
                <div className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold border-2 ${
                  step.n === 1
                    ? 'bg-brand border-brand text-white'
                    : 'border-gray-300 text-gray-400'
                }`}>
                  {step.n}
                </div>
                <span className="text-xs font-medium hidden sm:block">{step.label}</span>
              </div>
              {i < arr.length - 1 && (
                <div className="w-6 h-px bg-gray-300" />
              )}
            </div>
          ))}
        </div>

        {/* ── Screen indicator ─────────────────────────────────────────── */}
        <div className="text-center mb-8">
          <div className="inline-block w-2/3 h-2 bg-gradient-to-r from-transparent via-brand to-transparent rounded-full mb-1.5 opacity-70" />
          <p className="text-xs text-gray-400 tracking-[0.2em] uppercase">All Eyes This Way · Screen</p>
        </div>

        {/* ── Seat Grid ────────────────────────────────────────────────── */}
        <div className="overflow-x-auto pb-4">
          <div className="inline-block min-w-full">
            {rows.map(row => (
              <div key={row} className="flex items-center gap-2 mb-2 justify-center">
                <span className="w-5 text-xs text-gray-400 font-mono text-center">{row}</span>
                <div className="flex gap-1.5">
                  {rowMap[row].map(seat => (
                    <button
                      key={seat.id}
                      id={`seat-${seat.seat_number}`}
                      onClick={() => toggleSeat(seat)}
                      className={`w-8 h-8 flex items-center justify-center transition-all duration-150 ${getSeatClass(seat)}`}
                      title={`${seat.seat_number} · ${seat.type} · ${seat.status}`}
                      disabled={
                        seat.status === 'booked' ||
                        (seat.status === 'locked' && seat.locked_by !== user?.id)
                      }
                    >
                      {seat.seat_num}
                    </button>
                  ))}
                </div>
                <span className="w-5 text-xs text-gray-400 font-mono text-center">{row}</span>
              </div>
            ))}
          </div>
        </div>

        {/* ── Legend ───────────────────────────────────────────────────── */}
        <div className="flex flex-wrap items-center justify-center gap-4 mt-4 mb-8 text-xs text-gray-500">
          <LegendItem color="bg-green-100 border border-green-400" label={`Normal · ₹${normalPrice}`} />
          <LegendItem color="bg-purple-100 border border-purple-400" label={`Premium · ₹${premiumPrice}`} />
          <LegendItem color="bg-blue-600" label="Selected" />
          <LegendItem color="bg-red-500 opacity-80" label="Booked" />
          <LegendItem color="bg-yellow-100 border border-yellow-300" label="Locked" />
        </div>

        {/* ── Selection Summary + CTA ───────────────────────────────────── */}
        {selected.length > 0 && (
          <div className="glass p-5 animate-slide-up">
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">

              {/* Seat summary */}
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <Armchair size={16} className="text-brand" />
                  <span className="text-gray-900 font-semibold">
                    {selected.length} seat{selected.length > 1 ? 's' : ''} selected
                  </span>
                </div>
                <p className="text-sm text-gray-500">
                  {selectedSeats.map(s => s.seat_number).join(', ')}
                </p>
                <div className="flex gap-3 mt-1 text-xs text-gray-400">
                  {normalCount > 0 && <span>Normal × {normalCount} = ₹{normalCount * normalPrice}</span>}
                  {premiumCount > 0 && <span>Premium × {premiumCount} = ₹{premiumCount * premiumPrice}</span>}
                </div>
              </div>

              {/* Total + CTA */}
              <div className="flex items-center gap-4">
                <div className="text-right">
                  <p className="text-xs text-gray-400">Total</p>
                  <p className="text-2xl font-black text-gray-900">₹{totalPrice}</p>
                </div>
                <button
                  onClick={handleProceedToSummary}
                  id="btn-proceed-summary"
                  className="btn-primary flex items-center gap-2 whitespace-nowrap py-3 px-5"
                >
                  Payment Summary
                  <ChevronRight size={17} />
                </button>
              </div>
            </div>

            <div className="mt-3 flex items-center gap-2 text-xs text-gray-400 border-t border-gray-100 pt-3">
              <Info size={12} />
              <span>
                Seats are locked during payment. PDF ticket + email sent after booking confirmation.
              </span>
            </div>
          </div>
        )}

      </div>
    </div>
  );
}

const LegendItem = ({ color, label }) => (
  <div className="flex items-center gap-1.5">
    <div className={`w-4 h-4 rounded ${color}`} />
    <span>{label}</span>
  </div>
);
