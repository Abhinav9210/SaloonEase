import { useAppSelector } from '../../app/hooks'
import { Link } from 'react-router-dom'
import Navbar from '../../components/layout/Navbar'
import Sidebar from '../../components/layout/Sidebar'

const CustomerOverview = () => {
  const { user } = useAppSelector(s => s.auth)
  const { sidebarOpen } = useAppSelector(s => s.ui)

  return (
    <div className="min-h-screen bg-dark-900">
      <Navbar />
      <Sidebar />
      <main className={`pt-16 transition-all duration-300 ${sidebarOpen ? 'lg:pl-60' : 'lg:pl-16'}`}>
        <div className="page-container py-8">
          <div className="glass-card p-8 rounded-2xl text-center max-w-lg mx-auto mt-12">
            <div className="w-16 h-16 rounded-2xl flex items-center justify-center text-white font-bold text-2xl mx-auto mb-4"
              style={{ background: 'linear-gradient(135deg,#7c3aed,#06b6d4)' }}>
              {user?.name?.[0]?.toUpperCase()}
            </div>
            <h1 className="text-2xl font-display font-bold text-white mb-1">Welcome, {user?.name}! 👋</h1>
            <p className="text-slate-400 mb-6">Ready to book your next appointment?</p>
            <div className="flex gap-3 justify-center flex-wrap">
              <Link to="/salons" className="btn-primary px-6 py-2.5">Browse Salons</Link>
              <Link to="/dashboard/customer/bookings" className="btn-secondary px-6 py-2.5">My Bookings</Link>
            </div>
          </div>
        </div>
      </main>
    </div>
  )
}

export default CustomerOverview
