import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Search, ArrowRight, Zap, Star, Shield, Clock } from 'lucide-react'
import { useAppDispatch, useAppSelector } from '../../app/hooks'
import { fetchApprovedSalons, fetchTopRated, searchSalons } from '../../features/salon/salonSlice'
import SalonCard from '../../components/common/SalonCard'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import Navbar from '../../components/layout/Navbar'
import Footer from '../../components/layout/Footer'

const FEATURES = [
  { icon: <Zap size={22} />,    title: 'Instant Booking',   desc: 'Book in seconds with real-time availability.' },
  { icon: <Shield size={22} />, title: 'Secure Payments',   desc: 'Pay online safely. Pay the rest in cash.' },
  { icon: <Star size={22} />,   title: 'Verified Reviews',  desc: 'Genuine reviews from real customers.' },
  { icon: <Clock size={22} />,  title: 'Flexible Slots',    desc: 'Pick any time from dynamically generated slots.' },
]

const HomePage = () => {
  const dispatch = useAppDispatch()
  const { salons, topRated, loading, searchResults } = useAppSelector(s => s.salon)
  const [query, setQuery] = useState('')
  const [searched, setSearched] = useState(false)

  useEffect(() => {
    dispatch(fetchApprovedSalons({ page: 0, size: 8 }))
    dispatch(fetchTopRated())
  }, [])

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    if (query.trim()) { dispatch(searchSalons({ query })); setSearched(true) }
  }

  const displaySalons = searched ? searchResults : salons

  return (
    <div className="min-h-screen bg-dark-900">
      <Navbar />

      {/* Hero */}
      <section className="relative pt-28 pb-20 overflow-hidden">
        <div className="glow-orb w-[600px] h-[600px] bg-primary-700 -top-60 -left-60 opacity-15" />
        <div className="glow-orb w-[400px] h-[400px] bg-accent -top-40 right-0 opacity-10" />

        <div className="page-container relative z-10 text-center">
          <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full border border-primary-600/30 bg-primary-600/10 text-primary-400 text-xs font-medium mb-6 animate-fade-in">
            <Zap size={12} /> India's #1 Premium Salon Booking Platform
          </div>
          <h1 className="font-display font-bold text-5xl sm:text-6xl lg:text-7xl text-white leading-tight mb-6 animate-slide-up">
            Your Perfect <span className="text-gradient">Look</span><br />Starts Here
          </h1>
          <p className="text-slate-400 text-lg sm:text-xl max-w-2xl mx-auto mb-10 animate-slide-up">
            Discover top-rated salons, book your favorite barber, and enjoy a premium grooming experience — all with one click.
          </p>

          <form onSubmit={handleSearch} className="flex gap-2 max-w-xl mx-auto mb-8 animate-slide-up">
            <div className="relative flex-1">
              <Search size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-500" />
              <input value={query} onChange={e => setQuery(e.target.value)}
                placeholder="Search salons by name or city..."
                className="input-field pl-11 py-4 text-sm" />
            </div>
            <button type="submit" className="btn-primary px-6 py-4 whitespace-nowrap">Search</button>
          </form>

          <div className="flex items-center justify-center gap-6 text-sm text-slate-400 animate-fade-in">
            {[['500+', 'Salons'], ['10K+', 'Bookings'], ['4.8★', 'Avg Rating']].map(([v, l]) => (
              <div key={l} className="flex items-center gap-1.5">
                <span className="font-bold text-white">{v}</span> {l}
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Top Rated */}
      {topRated.length > 0 && !searched && (
        <section className="page-container pb-16">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h2 className="section-title">⭐ Top Rated Salons</h2>
              <p className="text-slate-500 text-sm mt-1">Highest rated by our community</p>
            </div>
            <Link to="/salons" className="btn-secondary py-2 px-4 text-sm flex items-center gap-1">
              View All <ArrowRight size={14} />
            </Link>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
            {topRated.slice(0, 4).map(salon => <SalonCard key={salon.id} salon={salon} />)}
          </div>
        </section>
      )}

      {/* Salons Grid */}
      <section className="page-container pb-16">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h2 className="section-title">{searched ? `Results for "${query}"` : '🔥 Popular Salons'}</h2>
            <p className="text-slate-500 text-sm mt-1">
              {searched ? `${displaySalons.length} salons found` : 'Discover salons near you'}
            </p>
          </div>
          {searched && (
            <button onClick={() => setSearched(false)} className="btn-secondary py-2 px-4 text-sm">Clear</button>
          )}
        </div>

        {loading ? (
          <LoadingSpinner size="lg" text="Loading salons..." />
        ) : displaySalons.length > 0 ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
            {displaySalons.map(salon => <SalonCard key={salon.id} salon={salon} />)}
          </div>
        ) : (
          <div className="py-20 text-center text-slate-500">No salons found. Try a different search.</div>
        )}

        {!searched && (
          <div className="flex justify-center mt-10">
            <Link to="/salons" className="btn-primary px-8 py-3 flex items-center gap-2">
              Browse All Salons <ArrowRight size={16} />
            </Link>
          </div>
        )}
      </section>

      {/* Features */}
      <section className="page-container pb-20">
        <div className="text-center mb-10">
          <h2 className="section-title">Why Choose SalonEase?</h2>
          <p className="text-slate-500 text-sm mt-2">Everything you need for a premium salon experience</p>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
          {FEATURES.map((f, i) => (
            <div key={i} className="glass-card-hover p-6 text-center rounded-2xl">
              <div className="w-12 h-12 rounded-2xl flex items-center justify-center mx-auto mb-4 text-primary-400"
                style={{ background: 'rgba(124,58,237,0.15)', border: '1px solid rgba(124,58,237,0.25)' }}>
                {f.icon}
              </div>
              <h3 className="text-white font-semibold mb-2">{f.title}</h3>
              <p className="text-slate-500 text-sm leading-relaxed">{f.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Owner CTA */}
      <section className="page-container pb-20">
        <div className="rounded-3xl p-10 text-center relative overflow-hidden"
          style={{ background: 'linear-gradient(135deg, rgba(124,58,237,0.2), rgba(6,182,212,0.1))', border: '1px solid rgba(124,58,237,0.3)' }}>
          <div className="relative z-10">
            <h2 className="font-display font-bold text-3xl text-white mb-3">Own a Salon?</h2>
            <p className="text-slate-400 mb-6 max-w-md mx-auto">
              Join hundreds of salon owners and grow your business with SalonEase's powerful booking platform.
            </p>
            <Link to="/register?role=OWNER" className="btn-primary px-8 py-3 inline-flex items-center gap-2">
              Register Your Salon <ArrowRight size={16} />
            </Link>
          </div>
        </div>
      </section>

      <Footer />
    </div>
  )
}

export default HomePage
