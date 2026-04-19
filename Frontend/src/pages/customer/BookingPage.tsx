import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { ChevronLeft, ChevronRight, Check, Calendar, Loader2 } from 'lucide-react'
import { format } from 'date-fns'
import { useState } from 'react'
import { useAppDispatch, useAppSelector } from '../../app/hooks'
import { fetchSalonById } from '../../features/salon/salonSlice'
import { createBooking } from '../../features/booking/bookingSlice'
import {
  setSelectedBarber, setSelectedService, setSelectedSlot,
  nextBookingStep, prevBookingStep, resetBookingFlow
} from '../../features/ui/uiSlice'
import { salonApi } from '../../api/salonApi'
import { slotApi } from '../../api/index'
import type { Barber, SalonService, TimeSlot } from '../../types'
import BarberCard from '../../components/common/BarberCard'
import Navbar from '../../components/layout/Navbar'
import { useParams } from 'react-router-dom'

const STEPS = ['Select Barber', 'Select Service', 'Pick Slot', 'Payment', 'Confirm']

const BookingPage = () => {
  const { id } = useParams<{ id: string }>()
  const dispatch = useAppDispatch()
  const navigate = useNavigate()
  const { currentSalon } = useAppSelector(s => s.salon)
  const { bookingStep, selectedBarberId, selectedServiceId, selectedSlotId } = useAppSelector(s => s.ui)
  const { loading } = useAppSelector(s => s.booking)
  const { isAuthenticated } = useAppSelector(s => s.auth)

  const [barbers, setBarbers] = useState<Barber[]>([])
  const [services, setServices] = useState<SalonService[]>([])
  const [slots, setSlots] = useState<TimeSlot[]>([])
  const [selectedDate, setSelectedDate] = useState(format(new Date(), 'yyyy-MM-dd'))
  const [paymentMode, setPaymentMode] = useState<'ONLINE_FULL' | 'ONLINE_FEE_CASH_REMAINING'>('ONLINE_FEE_CASH_REMAINING')
  const [notes, setNotes] = useState('')

  useEffect(() => {
    dispatch(resetBookingFlow())
    if (id) {
      dispatch(fetchSalonById(Number(id)))
      salonApi.getBarbers(Number(id)).then(r => {
        const arr = r.data.data;
        setBarbers((Array.isArray(arr) ? arr : (arr?.content ?? [])).map((b: any) => ({ ...b, isAvailable: b.isAvailable ?? b.available ?? false })));
      })
      salonApi.getServices(Number(id)).then(r => {
        const arr = r.data.data;
        setServices((Array.isArray(arr) ? arr : (arr?.content ?? [])).map((s: any) => ({ ...s, isActive: s.isActive ?? s.active ?? false })));
      })
    }
    if (!isAuthenticated) navigate('/login', { state: { from: { pathname: `/salons/${id}/book` } } })
  }, [id, isAuthenticated])

  useEffect(() => {
    if (selectedBarberId && selectedDate) {
      slotApi.getAvailable(selectedBarberId, selectedDate).then(r => {
        const arr = r.data.data;
        setSlots((Array.isArray(arr) ? arr : (arr?.content ?? [])).map((s: any) => ({ ...s, isBooked: s.isBooked ?? s.booked ?? false, isAvailable: s.isAvailable ?? s.available ?? false })));
      })
    }
  }, [selectedBarberId, selectedDate])

  const selectedBarber = barbers.find(b => b.id === selectedBarberId)
  const selectedService = services.find(s => s.id === selectedServiceId)
  const selectedSlot = slots.find(s => s.id === selectedSlotId)

  const handleConfirmBooking = async () => {
    if (!selectedBarberId || !selectedServiceId || !selectedSlotId || !id) return
    const res = await dispatch(createBooking({
      salonId: Number(id),
      barberId: selectedBarberId,
      serviceId: selectedServiceId,
      slotId: selectedSlotId,
      paymentMode,
      notes,
    }))
    if (createBooking.fulfilled.match(res)) {
      navigate('/dashboard/customer/bookings')
    }
  }

  return (
    <div className="min-h-screen bg-dark-900">
      <Navbar />
      <div className="pt-24 pb-16 page-container">
        <div className="max-w-2xl mx-auto">
          {/* Progress Steps */}
          <div className="flex items-center justify-between mb-8">
            {STEPS.map((step, i) => (
              <div key={i} className="flex items-center gap-1">
                <div className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold transition-all ${
                  i < bookingStep ? 'bg-primary-600 text-white' :
                  i === bookingStep ? 'bg-primary-600/30 text-primary-400 border border-primary-600/60' :
                  'bg-dark-700 text-slate-600'
                }`}>
                  {i < bookingStep ? <Check size={14} /> : i + 1}
                </div>
                <span className={`text-xs hidden sm:block ${i === bookingStep ? 'text-primary-400 font-medium' : 'text-slate-600'}`}>
                  {step}
                </span>
                {i < STEPS.length - 1 && (
                  <div className={`w-8 sm:w-12 h-px mx-1 ${i < bookingStep ? 'bg-primary-600' : 'bg-dark-600'}`} />
                )}
              </div>
            ))}
          </div>

          <div className="glass-card rounded-2xl p-6 min-h-[400px]">
            {/* Step 0: Select Barber */}
            {bookingStep === 0 && (
              <div>
                <h2 className="section-title mb-4">Choose Your Barber</h2>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  {barbers.filter(b => b.isAvailable).map(b => (
                    <BarberCard key={b.id} barber={b}
                      selected={selectedBarberId === b.id}
                      onSelect={() => dispatch(setSelectedBarber(b.id))} />
                  ))}
                </div>
              </div>
            )}

            {/* Step 1: Select Service */}
            {bookingStep === 1 && (
              <div>
                <h2 className="section-title mb-4">Choose a Service</h2>
                <div className="space-y-3">
                  {services.map(s => (
                    <div key={s.id} onClick={() => dispatch(setSelectedService(s.id))}
                      className={`p-4 rounded-xl border cursor-pointer transition-all ${
                        selectedServiceId === s.id
                          ? 'border-primary-500 bg-primary-600/10 shadow-glow'
                          : 'border-white/5 hover:border-white/15 glass-card'
                      }`}>
                      <div className="flex justify-between items-center">
                        <div>
                          <p className="text-white font-semibold">{s.name}</p>
                          {s.description && <p className="text-xs text-slate-500 mt-0.5">{s.description}</p>}
                          <p className="text-xs text-slate-400 mt-1">{s.durationMinutes} minutes</p>
                        </div>
                        <div className="flex items-center gap-2">
                          <p className="text-lg font-bold text-white">₹{s.price}</p>
                          {selectedServiceId === s.id && <Check size={18} className="text-primary-400" />}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Step 2: Pick Slot */}
            {bookingStep === 2 && (
              <div>
                <h2 className="section-title mb-4">Pick a Time Slot</h2>
                <input type="date" value={selectedDate}
                  min={format(new Date(), 'yyyy-MM-dd')}
                  onChange={e => setSelectedDate(e.target.value)}
                  className="input-field mb-4" />
                {slots.length === 0 ? (
                  <div className="text-center py-10 text-slate-500">
                    <Calendar size={40} className="mx-auto mb-2 opacity-30" />
                    No available slots for this date
                  </div>
                ) : (
                  <div className="grid grid-cols-3 sm:grid-cols-4 gap-2">
                    {slots.map(slot => (
                      <button key={slot.id} onClick={() => dispatch(setSelectedSlot(slot.id))}
                        className={`p-2.5 rounded-lg text-center text-xs font-medium transition-all ${
                          selectedSlotId === slot.id
                            ? 'bg-primary-600 text-white border border-primary-500'
                            : 'bg-dark-700 text-slate-400 border border-white/5 hover:border-primary-600/40'
                        }`}>
                        {slot.startTime} – {slot.endTime}
                      </button>
                    ))}
                  </div>
                )}
              </div>
            )}

            {/* Step 3: Payment */}
            {bookingStep === 3 && (
              <div>
                <h2 className="section-title mb-4">Payment Mode</h2>
                <div className="space-y-3 mb-6">
                  {[
                    { value: 'ONLINE_FEE_CASH_REMAINING', label: '💳 Pay Booking Fee Online', desc: `Pay ₹${currentSalon?.minimumBookingFee} now, rest at salon` },
                    { value: 'ONLINE_FULL', label: '✅ Pay Full Amount Online', desc: `Pay ₹${selectedService?.price} now` },
                  ].map(opt => (
                    <div key={opt.value} onClick={() => setPaymentMode(opt.value as typeof paymentMode)}
                      className={`p-4 rounded-xl border cursor-pointer transition-all ${
                        paymentMode === opt.value
                          ? 'border-primary-500 bg-primary-600/10'
                          : 'border-white/5 glass-card hover:border-white/15'
                      }`}>
                      <p className={`font-semibold text-sm ${paymentMode === opt.value ? 'text-primary-300' : 'text-white'}`}>{opt.label}</p>
                      <p className="text-xs text-slate-400 mt-0.5">{opt.desc}</p>
                    </div>
                  ))}
                </div>
                <div>
                  <label className="text-xs text-slate-400 mb-1.5 block">Special Note (optional)</label>
                  <textarea rows={3} value={notes} onChange={e => setNotes(e.target.value)}
                    placeholder="Any special requests..."
                    className="input-field resize-none" />
                </div>
              </div>
            )}

            {/* Step 4: Confirm */}
            {bookingStep === 4 && (
              <div>
                <h2 className="section-title mb-6">Confirm Your Booking</h2>
                <div className="space-y-3">
                  {[
                    ['Salon', currentSalon?.name],
                    ['Barber', selectedBarber?.name],
                    ['Service', `${selectedService?.name} — ₹${selectedService?.price}`],
                    ['Date', selectedDate],
                    ['Time', `${selectedSlot?.startTime} – ${selectedSlot?.endTime}`],
                    ['Payment', paymentMode === 'ONLINE_FULL' ? 'Full Online' : `₹${currentSalon?.minimumBookingFee} now + rest at salon`],
                  ].map(([label, value]) => (
                    <div key={label} className="flex justify-between py-2.5 border-b border-white/5 text-sm">
                      <span className="text-slate-400">{label}</span>
                      <span className="text-white font-medium text-right max-w-[200px]">{value}</span>
                    </div>
                  ))}
                </div>
                <div className="mt-6 p-4 rounded-xl bg-primary-600/10 border border-primary-600/25 text-xs text-slate-400">
                  By confirming, you agree to the booking terms. Your slot will be held for 30 minutes pending salon approval.
                </div>
              </div>
            )}

            {/* Navigation */}
            <div className="flex justify-between mt-8 pt-4 border-t border-white/5">
              <button onClick={() => dispatch(prevBookingStep())}
                disabled={bookingStep === 0}
                className="btn-secondary py-2.5 px-5 text-sm flex items-center gap-2 disabled:opacity-40">
                <ChevronLeft size={16} /> Back
              </button>

              {bookingStep < STEPS.length - 1 ? (
                <button
                  onClick={() => dispatch(nextBookingStep())}
                  disabled={
                    (bookingStep === 0 && !selectedBarberId) ||
                    (bookingStep === 1 && !selectedServiceId) ||
                    (bookingStep === 2 && !selectedSlotId)
                  }
                  className="btn-primary py-2.5 px-5 text-sm flex items-center gap-2 disabled:opacity-40">
                  Next <ChevronRight size={16} />
                </button>
              ) : (
                <button onClick={handleConfirmBooking} disabled={loading}
                  className="btn-primary py-2.5 px-5 text-sm flex items-center gap-2">
                  {loading ? <Loader2 size={16} className="animate-spin" /> : <><Check size={16} /> Confirm Booking</>}
                </button>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default BookingPage
