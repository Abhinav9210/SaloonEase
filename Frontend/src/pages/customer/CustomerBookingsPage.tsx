import { useEffect } from 'react'
import { Link } from 'react-router-dom'
import { Calendar, Clock, Scissors } from 'lucide-react'
import { useAppDispatch, useAppSelector } from '../../app/hooks'
import { fetchMyBookings, cancelBooking } from '../../features/booking/bookingSlice'
import BookingStatusBadge from '../../components/common/BookingStatusBadge'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import Sidebar from '../../components/layout/Sidebar'
import Navbar from '../../components/layout/Navbar'

const CustomerBookingsPage = () => {
  const dispatch = useAppDispatch()
  const { myBookings, loading } = useAppSelector(s => s.booking)
  const { sidebarOpen } = useAppSelector(s => s.ui)

  useEffect(() => { dispatch(fetchMyBookings({ page: 0, size: 20 })) }, [])

  const handleCancel = (id: number) => {
    if (confirm('Cancel this booking?')) {
      dispatch(cancelBooking({ id, reason: 'Cancelled by customer' }))
    }
  }

  return (
    <div className="min-h-screen bg-dark-900">
      <Navbar />
      <Sidebar />
      <main className={`pt-16 transition-all duration-300 ${sidebarOpen ? 'lg:pl-60' : 'lg:pl-16'}`}>
        <div className="page-container py-8">
          <div className="mb-6">
            <h1 className="text-2xl font-display font-bold text-white">My Bookings</h1>
            <p className="text-slate-400 text-sm mt-1">Track all your appointments</p>
          </div>

          {loading ? <LoadingSpinner text="Loading bookings..." /> : (
            <div className="space-y-4">
              {myBookings.length === 0 && (
                <div className="glass-card p-12 rounded-2xl text-center">
                  <Calendar size={48} className="mx-auto text-slate-700 mb-4" />
                  <p className="text-white font-semibold mb-1">No bookings yet</p>
                  <p className="text-slate-500 text-sm mb-6">Book your first appointment today!</p>
                  <Link to="/salons" className="btn-primary px-6 py-2.5 text-sm">Browse Salons</Link>
                </div>
              )}
              {myBookings.map(booking => (
                <div key={booking.id} className="glass-card p-5 rounded-2xl animate-fade-in">
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                    <div className="flex items-start gap-4">
                      <div className="w-12 h-12 rounded-xl flex items-center justify-center flex-shrink-0"
                        style={{ background: 'rgba(124,58,237,0.15)', border: '1px solid rgba(124,58,237,0.25)' }}>
                        <Scissors size={22} className="text-primary-400" />
                      </div>
                      <div>
                        <div className="flex items-center gap-2 flex-wrap">
                          <h3 className="text-white font-semibold">{booking.salonName}</h3>
                          <BookingStatusBadge status={booking.status} />
                        </div>
                        <p className="text-sm text-slate-400 mt-0.5">
                          {booking.serviceName} with {booking.barberName}
                        </p>
                        <div className="flex flex-wrap items-center gap-3 mt-1.5">
                          <div className="flex items-center gap-1 text-xs text-slate-500">
                            <Calendar size={11} /> {booking.bookingDate}
                          </div>
                          <div className="flex items-center gap-1 text-xs text-slate-500">
                            <Clock size={11} /> {booking.startTime} – {booking.endTime}
                          </div>
                        </div>
                      </div>
                    </div>
                    <div className="flex items-center gap-3 flex-shrink-0">
                      <div className="text-right">
                        <p className="text-xs text-slate-500">Total</p>
                        <p className="text-white font-bold">₹{booking.totalAmount}</p>
                        <p className="text-xs text-slate-500">Ref: {booking.bookingReference}</p>
                      </div>
                      {(booking.status === 'PENDING' || booking.status === 'APPROVED') && (
                        <button onClick={() => handleCancel(booking.id)} className="btn-danger text-xs py-1.5 px-3">
                          Cancel
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </main>
    </div>
  )
}

export default CustomerBookingsPage
